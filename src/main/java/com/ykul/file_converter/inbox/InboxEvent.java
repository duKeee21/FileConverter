package com.ykul.file_converter.inbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "inbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboxEvent {

    @Id
    private String eventId;

    private Instant processedAt;
}