package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyTemplate {

    private final UUID id;
    private final String name;
    private final String type;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final int version;
    private final String createdBy;
    private final String body;
    private final String subject;
    private final String letterContactBlock;

    public NotifyTemplate(@JsonProperty("id") UUID id,
                          @JsonProperty("name") String name,
                          @JsonProperty("type") String type,
                          @JsonProperty("created_at") ZonedDateTime createdAt,
                          @JsonProperty("updated_at") ZonedDateTime updatedAt,
                          @JsonProperty("version") int version,
                          @JsonProperty("created_by") String createdBy,
                          @JsonProperty("body") String body,
                          @JsonProperty("subject") String subject,
                          @JsonProperty("letter_contact_block") String letterContactBlock) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
        this.createdBy = createdBy;
        this.body = body;
        this.subject = subject;
        this.letterContactBlock = letterContactBlock;
    }

    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("created_at")
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("updated_at")
    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("version")
    public int getVersion() {
        return version;
    }

    @JsonProperty("created_by")
    public String getCreatedBy() {
        return createdBy;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    @JsonProperty("letter_contact_block")
    public String getLetterContactBlock() {
        return letterContactBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyTemplate that = (NotifyTemplate) o;
        return version == that.version && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(type, that.type) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(createdBy, that.createdBy) && Objects.equals(body, that.body) && Objects.equals(subject, that.subject) && Objects.equals(letterContactBlock, that.letterContactBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, createdAt, updatedAt, version, createdBy, body, subject, letterContactBlock);
    }

    @Override
    public String toString() {
        return "NotifyTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                ", createdBy='" + createdBy + '\'' +
                ", body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", letterContactBlock='" + letterContactBlock + '\'' +
                '}';
    }
}
