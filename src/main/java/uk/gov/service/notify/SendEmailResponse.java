package uk.gov.service.notify;

import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

public class SendEmailResponse {
    private final UUID notificationId;
    private final String reference;
    private final UUID templateId;
    private final int templateVersion;
    private final String templateUri;
    private final String body;
    private final String subject;
    private final String fromEmail;


    public SendEmailResponse(String response) {
        JSONObject data = new JSONObject(response);
        notificationId = UUID.fromString(data.getString("id"));
        reference = data.isNull("reference") ? null : data.getString("reference");
        JSONObject content = data.getJSONObject("content");
        body = content.getString("body");
        fromEmail = content.isNull("from_email") ? null : content.getString("from_email");
        subject = content.getString("subject");
        JSONObject template = data.getJSONObject("template");
        templateId = UUID.fromString(template.getString("id"));
        templateVersion = template.getInt("version");
        templateUri = template.getString("uri");
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public int getTemplateVersion() {
        return templateVersion;
    }

    public String getTemplateUri() {
        return templateUri;
    }

    public String getBody() {
        return body;
    }

    public String getSubject() {
        return subject;
    }

    public Optional<String> getFromEmail() {
        return Optional.ofNullable(fromEmail);
    }

    @Override
    public String toString() {
        return "SendEmailResponse{" +
                "notificationId=" + notificationId +
                ", reference=" + reference +
                ", templateId=" + templateId +
                ", templateVersion=" + templateVersion +
                ", templateUri='" + templateUri + '\'' +
                ", body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", fromEmail=" + fromEmail +
                '}';
    }
}
