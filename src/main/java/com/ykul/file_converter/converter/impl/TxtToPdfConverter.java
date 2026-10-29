package com.ykul.file_converter.converter.impl;

import com.ykul.file_converter.converter.Converter;
import com.ykul.file_converter.converter.FileType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class TxtToPdfConverter implements Converter {

    @Override
    public boolean supports(FileType fileType) {

        return fileType == FileType.TXT;
    }

    @Override
    public byte[] convert(InputStream inputStream) throws Exception {
        try (inputStream) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                PDType0Font font;
                try (InputStream fontStream = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
                    if (fontStream == null) {
                        throw new IllegalStateException("Шрифт не найден в classpath");
                    }
                    font = PDType0Font.load(document, fontStream);
                }

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    cs.setFont(font, 12);
                    cs.beginText();
                    cs.newLineAtOffset(50, 750);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        cs.showText(line);
                        cs.newLineAtOffset(0, -15);
                    }
                    cs.endText();
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.save(out);
                return out.toByteArray();
            }
        }
    }
}