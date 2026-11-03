package com.ykul.file_converter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykul.file_converter.dto.ConversionRequest;
import com.ykul.file_converter.inbox.InboxEventRepository;
import com.ykul.file_converter.outbox.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileConversionServiceTest {

    @Mock
    private MinioStorageService minio;
    @Mock
    private ConverterService converter;
    @Mock
    private InboxEventRepository inbox;
    @Mock
    private OutboxEventRepository outbox;

    private FileConversionService service;

    @BeforeEach
    void setUp() {
        service = new FileConversionService(minio, converter, inbox, outbox, new ObjectMapper());
    }

    @Test
    void shouldSkipIfEventAlreadyInInbox() {
        ConversionRequest request = new ConversionRequest();
        request.setEventId("test-id");
        request.setFilePath("test.txt");

        when(inbox.existsById("test-id")).thenReturn(true);

        service.process(request);

        verifyNoInteractions(minio);
        verifyNoInteractions(converter);
        verifyNoInteractions(outbox);
    }

    @Test
    void shouldConvertAndUploadSingleFile() throws Exception {
        ConversionRequest request = new ConversionRequest();
        request.setEventId("test-single");
        request.setFilePath("document.txt");

        InputStream fakeStream = new ByteArrayInputStream(new byte[0]);
        byte[] fakePdf = "%PDF-1.4".getBytes();

        when(inbox.existsById("test-single")).thenReturn(false);
        when(minio.downloadFile("document.txt")).thenReturn(fakeStream);
        when(converter.convert(eq("TXT"), any(InputStream.class))).thenReturn(fakePdf);

        service.process(request);

        verify(minio).uploadFile(eq("converted/test-single.pdf"), any(InputStream.class), eq((long) fakePdf.length), eq("application/pdf"));
        verify(outbox).save(any());
        verify(inbox).save(any());
    }
}