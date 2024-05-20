package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class NotifyEmailRequest {

    private final String emailAddress;
    private final String templateId;
    private final Map<String, ?> personalisation;
    private final String reference;
    private final String emailReplyToId;

    public NotifyEmailRequest(@JsonProperty("email_address") String emailAddress,
                              @JsonProperty("template_id") String templateId,
                              @JsonProperty("personalisation") Map<String, ?> personalisation,
                              @JsonProperty("reference") String reference,
                              @JsonProperty("email_reply_to_id") String emailReplyToId) {
        this.emailAddress = emailAddress;
        this.templateId = templateId;
        this.personalisation = personalisation;
        this.reference = reference;
        this.emailReplyToId = emailReplyToId;
    }

    @JsonProperty("email_address")
    public String getEmailAddress() {
        return emailAddress;
    }

    @JsonProperty("template_id")
    public String getTemplateId() {
        return templateId;
    }

    @JsonProperty("personalisation")
    public Map<String, ?> getPersonalisation() {
        return personalisation;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("email_reply_to_id")
    public String getEmailReplyToId() {
        return emailReplyToId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyEmailRequest request = (NotifyEmailRequest) o;
        return Objects.equals(emailAddress, request.emailAddress) && Objects.equals(templateId, request.templateId) && Objects.equals(personalisation, request.personalisation) && Objects.equals(reference, request.reference) && Objects.equals(emailReplyToId, request.emailReplyToId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress, templateId, personalisation, reference, emailReplyToId);
    }

    @Override
    public String toString() {
        return "NotifyEmailRequest{" +
                "emailAddress='" + emailAddress + '\'' +
                ", templateId='" + templateId + '\'' +
                ", personalisation=" + personalisation +
                ", reference='" + reference + '\'' +
                ", emailReplyToId='" + emailReplyToId + '\'' +
                '}';
    }
}
