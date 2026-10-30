package com.ykul.file_converter.inbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "inbox")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InboxEvent {

    @Id
    private String eventId;

    private Instant processedAt;
}