package com.ykul.file_converter.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class ConversionRequest {
    private String eventId;
    private String filePath;
}