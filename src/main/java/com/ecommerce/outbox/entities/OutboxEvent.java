package com.ecommerce.outbox.entities;

import jakarta.persistence.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_context_id", columnList = "context_id"),
                @Index(name = "idx_event_name", columnList = "event_name"),
                @Index(name = "idx_process_name", columnList = "process_name"),
                @Index(name = "idx_status", columnList = "status"),
        }
)
public class OutboxEvent {

    public static final int MAX_TEXT_LENGTH = 255;

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Length(min = 1, max = MAX_TEXT_LENGTH)
    @Column(name = "context_id", nullable = false)
    private String contextId;

    @Length(min = 1, max = MAX_TEXT_LENGTH)
    @Column(name = "process_name", nullable = false)
    private String processName;

    @Length(min = 1, max = MAX_TEXT_LENGTH)
    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxEventStatus status;

    @Length(max = MAX_TEXT_LENGTH)
    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "processed_timestamp")
    private long processedTimestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Version
    private int version;

    public OutboxEvent() {
    }

    public OutboxEvent(
            String contextId,
            String processName,
            String eventName,
            String payload,
            OutboxEventStatus status,
            String statusMessage,
            long processedTimestamp
    ) {
        this.id = UUID.nameUUIDFromBytes(String.format("%s-%s-%s", contextId, processName, status).getBytes(UTF_8));
        this.contextId = contextId;
        this.eventName = eventName;
        this.processName = processName;
        this.payload = payload;
        this.status = status;
        this.statusMessage = statusMessage;
        this.processedTimestamp = processedTimestamp;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public OutboxEventStatus getStatus() {
        return status;
    }

    public void setStatus(OutboxEventStatus status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public long getProcessedTimestamp() {
        return processedTimestamp;
    }

    public void setProcessedTimestamp(long processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutboxEvent that = (OutboxEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OutboxEvent{" +
                "id=" + id +
                ", contextId='" + contextId + '\'' +
                ", processName='" + processName + '\'' +
                ", eventName='" + eventName + '\'' +
                ", payload='" + payload + '\'' +
                ", status=" + status +
                ", statusMessage='" + statusMessage + '\'' +
                ", processedTimestamp=" + processedTimestamp +
                ", createdAt=" + createdAt +
                ", version=" + version +
                '}';
    }

}
