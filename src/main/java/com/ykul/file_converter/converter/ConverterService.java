package com.ykul.file_converter.converter;

import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.List;

@Service
public class ConverterService {

    private final List<Converter> converters;

    public ConverterService(List<Converter> converters) {
        this.converters = converters;
    }

    public byte[] convert(String fileType, InputStream inputStream) throws Exception {
        return converters.stream()
                .filter(c -> c.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неподдерживаемый тип файла: " + fileType))
                .convert(inputStream);
    }
}