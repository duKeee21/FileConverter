package com.ykul.file_converter.integration;

import com.ykul.file_converter.dto.ConversionRequest;
import com.ykul.file_converter.inbox.InboxEventRepository;
import com.ykul.file_converter.outbox.OutboxEventRepository;
import com.ykul.file_converter.outbox.OutboxPublisher;
import com.ykul.file_converter.service.MinioStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@SuppressWarnings("resource")
class FileConversionIntegrationTest extends AbstractIntegrationTest {

    private static final String TOPIC = "file-conversion-requests";

    @Autowired
    private KafkaTemplate<String, ConversionRequest> kafkaTemplate;

    @Autowired
    private InboxEventRepository inboxRepository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private MinioStorageService minioStorageService;

    @MockitoBean
    private OutboxPublisher outboxPublisher;

    @BeforeAll
    static void setUpBucket() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(minio.getS3URL())
                .credentials(minio.getUserName(), minio.getPassword())
                .build();

        if (!client.bucketExists(BucketExistsArgs.builder().bucket("test-bucket").build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket("test-bucket").build());
        }
    }

    @BeforeEach
    void cleanDatabase() {
        inboxRepository.deleteAll();
        outboxRepository.deleteAll();
    }

    @Test
    void shouldConvertTxtFile() throws Exception {
        String fileName = "test.txt";
        String content = "Интеграционный тест";

        minioStorageService.uploadFile(fileName,
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                content.length(), "text/plain");

        String eventId = UUID.randomUUID().toString();
        ConversionRequest request = new ConversionRequest(eventId, fileName);
        kafkaTemplate.send(TOPIC, request);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(inboxRepository.existsById(eventId)).isTrue();
            assertThat(outboxRepository.findAll()).hasSize(1);
        });
    }
}