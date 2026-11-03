package com.ykul.file_converter.converter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileTypeTest {

    @Test
    void shouldReturnCorrectEnumFromExtension() {
        assertThat(FileType.fromExtension("txt")).isEqualTo(FileType.TXT);
        assertThat(FileType.fromExtension("PNG")).isEqualTo(FileType.PNG);
        assertThat(FileType.fromExtension("zip")).isEqualTo(FileType.ZIP);
    }

    @Test
    void shouldReturnUnknownForInvalidExtension() {
        assertThat(FileType.fromExtension("exe")).isEqualTo(FileType.UNKNOWN);
        assertThat(FileType.fromExtension(null)).isEqualTo(FileType.UNKNOWN);
        assertThat(FileType.fromExtension("")).isEqualTo(FileType.UNKNOWN);
    }
}