package uk.gov.service.notify;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

public class Notification {
    private UUID id;
    private String reference;
    private String emailAddress;
    private String phoneNumber;
    private String line1;
    private String line2;
    private String line3;
    private String line4;
    private String line5;
    private String line6;
    private String postcode;
    private String notificationType;
    private String status;
    private UUID templateId;
    private int templateVersion;
    private String templateUri;
    private String body;
    private String subject;
    private DateTime createdAt;
    private DateTime sentAt;
    private DateTime completedAt;
    private DateTime estimatedDelivery;
    private String createdByName;

    public Notification(String content){
        JSONObject responseBodyAsJson = new JSONObject(content);
        build(responseBodyAsJson);
    }

    public Notification(org.json.JSONObject data){
        build(data);
    }

    private void build(JSONObject data) {
        id = UUID.fromString(data.getString("id"));
        reference = data.isNull("reference") ? null : data.getString("reference");
        emailAddress = data.isNull("email_address") ? null : data.getString("email_address");
        phoneNumber = data.isNull("phone_number") ? null : data.getString("phone_number");
        line1 = data.isNull("line_1") ? null : data.getString("line_1");
        line2 = data.isNull("line_2") ? null : data.getString("line_2");
        line3 = data.isNull("line_3") ? null : data.getString("line_3");
        line4 = data.isNull("line_4") ? null : data.getString("line_4");
        line5 = data.isNull("line_5") ? null : data.getString("line_5");
        line6 = data.isNull("line_6") ? null : data.getString("line_6");
        postcode = data.isNull("postcode") ? null : data.getString("postcode");
        notificationType = data.getString("type");
        JSONObject template = data.getJSONObject("template");
        templateId = UUID.fromString(template.getString("id"));
        templateVersion = template.getInt("version");
        templateUri = template.getString("uri");
        body = data.getString("body");
        subject = data.isNull("subject") ? null : data.getString("subject");
        status = data.getString("status");
        createdAt = new DateTime(data.getString("created_at"));
        sentAt =  data.isNull("sent_at") ? null : new DateTime(data.getString("sent_at"));
        completedAt = data.isNull("completed_at") ? null : new DateTime(data.getString("completed_at"));
        estimatedDelivery = data.isNull("estimated_delivery") ? null : new DateTime(data.getString("estimated_delivery"));
        createdByName = data.isNull("created_by_name") ? null : data.getString("created_by_name");
    }

    public UUID getId() {
        return id;
    }

    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    public Optional<String> getEmailAddress() {
        return Optional.ofNullable(emailAddress);
    }

    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    public Optional<String> getLine1() {
        return Optional.ofNullable(line1);
    }

    public Optional<String> getLine2() {
        return Optional.ofNullable(line2);
    }

    public Optional<String> getLine3() {
        return Optional.ofNullable(line3);
    }

    public Optional<String> getLine4() {
        return Optional.ofNullable(line4);
    }

    public Optional<String> getLine5() {
        return Optional.ofNullable(line5);
    }

    public Optional<String> getLine6() {
        return Optional.ofNullable(line6);
    }

    public Optional<String> getPostcode() {
        return Optional.ofNullable(postcode);
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getStatus() {
        return status;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public int getTemplateVersion() {
        return templateVersion;
    }

    public String getTemplateUri(){
        return templateUri;
    }

    public String getBody() {
        return body;
    }

    public Optional<String> getSubject() {
        return Optional.ofNullable(subject);
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public Optional<DateTime> getSentAt() {
        return Optional.ofNullable(sentAt);
    }

    public Optional<DateTime> getCompletedAt() {
        return Optional.ofNullable(completedAt);
    }

    public Optional<String> getCreatedByName() {
        return Optional.ofNullable(createdByName);
    }

    /**
     * estimatedDelivery is only present on letters
     */
    public Optional<DateTime> getEstimatedDelivery() {
        return Optional.ofNullable(estimatedDelivery);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", line1='" + line1 + '\'' +
                ", line2='" + line2 + '\'' +
                ", line3='" + line3 + '\'' +
                ", line4='" + line4 + '\'' +
                ", line5='" + line5 + '\'' +
                ", line6='" + line6 + '\'' +
                ", postcode='" + postcode + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", status='" + status + '\'' +
                ", templateId=" + templateId +
                ", templateVersion=" + templateVersion +
                ", templateUri='" + templateUri + '\'' +
                ", body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", createdAt=" + createdAt +
                ", sentAt=" + sentAt +
                ", completedAt=" + completedAt +
                ", estimatedDelivery=" + estimatedDelivery +
                ", createdByName=" + createdByName +
                '}';
    }
}
