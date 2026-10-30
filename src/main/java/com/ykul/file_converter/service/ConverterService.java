package com.ykul.file_converter.service;

import com.ykul.file_converter.converter.Converter;
import com.ykul.file_converter.converter.FileType;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class ConverterService {

    private final List<Converter> converters;

    public ConverterService(List<Converter> converters) {
        this.converters = converters;
    }

    public byte[] convert(String fileTypeString, InputStream inputStream) throws Exception {
        FileType fileType = FileType.fromExtension(fileTypeString);

        return converters.stream()
                .filter(c -> c.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неподдерживаемый тип файла: " + fileTypeString))
                .convert(inputStream);
    }
}