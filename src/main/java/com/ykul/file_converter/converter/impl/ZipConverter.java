package com.ykul.file_converter.converter.impl;

import com.ykul.file_converter.converter.Converter;
import com.ykul.file_converter.converter.ConverterService;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipConverter implements Converter {

    private final ConverterService converterService;

    public ZipConverter(@Lazy ConverterService converterService) {
        this.converterService = converterService;
    }

    @Override
    public boolean supports(String fileType) {
        return "ZIP".equalsIgnoreCase(fileType);
    }

    @Override
    public byte[] convert(InputStream inputStream) throws Exception {
        List<byte[]> pdfParts = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String type = getFileExtension(entry.getName());

                byte[] entryBytes = zis.readAllBytes();

                try (InputStream entryStream = new ByteArrayInputStream(entryBytes)) {
                    byte[] convertedPdf = converterService.convert(type, entryStream);
                    pdfParts.add(convertedPdf);
                }
            }
        }

        if (pdfParts.isEmpty()) {
            throw new IllegalArgumentException("ZIP-архив пуст или не содержит поддерживаемых файлов");
        }

        if (pdfParts.size() == 1) {
            return pdfParts.getFirst();
        }

        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
        merger.setDestinationStream(mergedOutput);

        for (byte[] pdfPart : pdfParts) {
            merger.addSource(new RandomAccessReadBuffer(pdfPart));
        }

        merger.mergeDocuments(null);

        return mergedOutput.toByteArray();
    }

    private String getFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot == -1 ? "" : name.substring(dot + 1).toUpperCase();
    }
}