package com.ykul.file_converter.consumer;

import com.ykul.file_converter.dto.ConversionRequest;
import com.ykul.file_converter.service.FileConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileConversionConsumer {

    private final FileConversionService service;

    @KafkaListener(topics = "file-conversion-requests", groupId = "file-converter-group")
    public void onMessage(ConversionRequest request) {
        service.process(request);
    }
}