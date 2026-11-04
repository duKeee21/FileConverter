package com.ykul.file_converter.integration;

import com.ykul.file_converter.outbox.OutboxEvent;
import com.ykul.file_converter.outbox.OutboxEventRepository;
import com.ykul.file_converter.outbox.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@SuppressWarnings("resource")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class OutboxRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Test
    void shouldFindPendingEvents() {
        OutboxEvent firstEvent = new OutboxEvent();
        firstEvent.setEventId(UUID.randomUUID());
        firstEvent.setRequestEventId("req-1");
        firstEvent.setEventType("SUCCESS");
        firstEvent.setPayload("{}");
        firstEvent.setStatus(OutboxStatus.PENDING);
        firstEvent.setCreatedAt(Instant.now().minusSeconds(10));

        OutboxEvent secondEvent = new OutboxEvent();
        secondEvent.setEventId(UUID.randomUUID());
        secondEvent.setRequestEventId("req-2");
        secondEvent.setEventType("ERROR");
        secondEvent.setPayload("{}");
        secondEvent.setStatus(OutboxStatus.PENDING);
        secondEvent.setCreatedAt(Instant.now());

        OutboxEvent sentEvent = new OutboxEvent();
        sentEvent.setEventId(UUID.randomUUID());
        sentEvent.setRequestEventId("req-3");
        sentEvent.setEventType("SUCCESS");
        sentEvent.setPayload("{}");
        sentEvent.setStatus(OutboxStatus.SENT);
        sentEvent.setCreatedAt(Instant.now());

        outboxRepository.save(sentEvent);
        outboxRepository.save(secondEvent);
        outboxRepository.save(firstEvent);

        List<OutboxEvent> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        assertThat(pendingEvents).hasSize(2);
        assertThat(pendingEvents.get(0).getRequestEventId()).isEqualTo("req-1");
        assertThat(pendingEvents.get(1).getRequestEventId()).isEqualTo("req-2");
    }
}