package com.ykul.file_converter.converter;

import java.io.InputStream;

public interface Converter {
    boolean supports(FileType fileType);

    byte[] convert(InputStream inputStream) throws Exception;
}