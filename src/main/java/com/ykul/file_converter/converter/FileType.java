package com.ykul.file_converter.converter;

public enum FileType {
    TXT,
    PNG,
    JPG,
    ZIP,
    UNKNOWN;

    public static FileType fromExtension(String extension) {
        if (extension == null || extension.isBlank())
            return UNKNOWN;
        try {
            return FileType.valueOf(extension.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return UNKNOWN;
        }
    }
}
