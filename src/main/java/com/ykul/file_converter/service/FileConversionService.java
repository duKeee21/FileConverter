package com.ykul.file_converter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykul.file_converter.converter.ConverterService;
import com.ykul.file_converter.dto.ConversionRequest;
import com.ykul.file_converter.inbox.InboxEvent;
import com.ykul.file_converter.inbox.InboxEventRepository;
import com.ykul.file_converter.outbox.OutboxEvent;
import com.ykul.file_converter.outbox.OutboxEventRepository;
import com.ykul.file_converter.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileConversionService {

    private final MinioStorageService minio;
    private final ConverterService converter;
    private final InboxEventRepository inbox;
    private final OutboxEventRepository outbox;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(ConversionRequest request) {
        String eventId = request.getEventId();

        if (inbox.existsById(eventId)) {
            return;
        }

        try {
            String ext = extractExtension(request.getFilePath());
            List<String> outputPaths = new ArrayList<>();

            if ("ZIP".equalsIgnoreCase(ext)) {
                try (InputStream in = minio.downloadFile(request.getFilePath());
                     ZipInputStream zis = new ZipInputStream(in)) {

                    ZipEntry entry;
                    int fileIndex = 0;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.isDirectory()) {
                            continue;
                        }

                        String entryExt = extractExtension(entry.getName());
                        byte[] fileBytes = zis.readAllBytes();

                        byte[] pdfBytes;
                        try (InputStream entryStream = new ByteArrayInputStream(fileBytes)) {
                            pdfBytes = converter.convert(entryExt, entryStream);
                        } catch (Exception e) {
                            log.warn("Пропущен файл '{}' из ZIP-архива: {}", entry.getName(), e.getMessage());
                            continue;
                        }

                        String cleanName = extractBaseName(entry.getName());
                        String outputPath = "converted/" + eventId + "_" + fileIndex++ + "_" + cleanName + ".pdf";

                        minio.uploadFile(outputPath,
                                new ByteArrayInputStream(pdfBytes),
                                pdfBytes.length,
                                "application/pdf");

                        outputPaths.add(outputPath);
                    }
                }
            } else {
                byte[] pdfBytes;
                try (InputStream in = minio.downloadFile(request.getFilePath())) {
                    pdfBytes = converter.convert(ext, in);
                }

                String outputPath = "converted/" + eventId + ".pdf";
                minio.uploadFile(outputPath,
                        new ByteArrayInputStream(pdfBytes),
                        pdfBytes.length,
                        "application/pdf");

                outputPaths.add(outputPath);
            }

            if (outputPaths.isEmpty()) {
                throw new IllegalArgumentException("Не удалось сконвертировать ни один файл");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("originalEventId", eventId);
            payload.put("outputFilePaths", outputPaths);
            saveOutbox(eventId, "SUCCESS", payload);
            inbox.save(new InboxEvent(eventId, Instant.now()));

        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при обработке {}", eventId, e);

            Map<String, Object> payload = new HashMap<>();
            payload.put("originalEventId", eventId);
            payload.put("errorMessage", String.valueOf(e.getMessage()));
            saveOutbox(eventId, "ERROR", payload);
            inbox.save(new InboxEvent(eventId, Instant.now()));
        }
    }

    private void saveOutbox(String originalEventId, String type, Map<String, Object> payload) {
        try {
            UUID resultEventId = UUID.randomUUID();
            payload.put("eventId", resultEventId.toString());

            OutboxEvent event = new OutboxEvent();
            event.setEventId(resultEventId);
            event.setRequestEventId(originalEventId);
            event.setEventType(type);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setStatus(OutboxStatus.PENDING);
            event.setCreatedAt(Instant.now());
            outbox.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сохранить outboxEvent", e);
        }
    }

    private String extractExtension(String path) {
        int dot = path.lastIndexOf('.');
        if (dot == -1 || dot == path.length() - 1) {
            throw new IllegalArgumentException("Нет расширения в пути: " + path);
        }
        return path.substring(dot + 1).toUpperCase();
    }

    private String extractBaseName(String fileName) {
        int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        String name = (lastSlash == -1) ? fileName : fileName.substring(lastSlash + 1);
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? name : name.substring(0, dot);
    }
}