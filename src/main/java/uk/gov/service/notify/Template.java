package uk.gov.service.notify;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

public class Template {
    private UUID id;
    private String name;
    private String templateType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private int version;
    private String body;
    private String subject;
    private Map<String, Object> personalisation;
    private String letterContactBlock;


    public Template(String content){
        JSONObject responseBodyAsJson = new JSONObject(content);
        build(responseBodyAsJson);

    }

    public Template(org.json.JSONObject data){
        build(data);

    }

    private void build(JSONObject data) {
        id = UUID.fromString(data.getString("id"));
        name = data.getString("name");
        templateType = data.getString("type");
        createdAt = LocalDateTime.parse(data.getString("created_at"), DateUtils.DATE_TIME_FORMATTER);
        updatedAt = data.isNull("updated_at") ? null : LocalDateTime.parse(data.getString("updated_at"), DateUtils.DATE_TIME_FORMATTER);
        version = data.getInt("version");
        body = data.getString("body");
        subject = data.isNull("subject") ? null : data.getString("subject");
        letterContactBlock = data.isNull("letter_contact_block") ? null : data.getString("letter_contact_block");
        personalisation = data.isNull("personalisation") ? null :
                JsonUtils.jsonToMap(data.getJSONObject("personalisation"));
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Optional<LocalDateTime> getUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Optional<String> getSubject() {
        return Optional.ofNullable(subject);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Optional<String> getLetterContactBlock() {
        return Optional.ofNullable(letterContactBlock);
    }

    public void setLetterContactBlock(String letterContactBlock) {
        this.letterContactBlock = letterContactBlock;
    }

    public Optional<Map<String, Object>> getPersonalisation() {
        return Optional.ofNullable(personalisation);
    }

    public void setPersonalisation(Map<String, Object> personalisation) {
        this.personalisation = personalisation;
    }

    @Override
    public String toString() {
        return "Template{" +
                "id=" + id +
                ", templateType='" + templateType + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", version=" + version +
                ", body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", letterContactBlock='" + letterContactBlock + '\'' +
                ", personalisation='" + personalisation + '\'' +
                '}';
    }
}
