package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NotifyEmailRequest {

    private final String emailAddress;
    private final UUID templateId;
    private final Map<String, ?> personalisation;
    private final String reference;
    private final UUID emailReplyToId;
    private final URI oneClickUnsubscribeURL;

    public NotifyEmailRequest(@JsonProperty("email_address") String emailAddress,
                              @JsonProperty("template_id") UUID templateId,
                              @JsonProperty("personalisation") Map<String, ?> personalisation,
                              @JsonProperty("reference") String reference,
                              @JsonProperty("email_reply_to_id") UUID emailReplyToId,
                              @JsonProperty("one_click_unsubscribe_url") URI oneClickUnsubscribeURL) {
        this.emailAddress = emailAddress;
        this.templateId = templateId;
        this.personalisation = personalisation;
        this.reference = reference;
        this.emailReplyToId = emailReplyToId;
        this.oneClickUnsubscribeURL = oneClickUnsubscribeURL;
    }

    @JsonProperty("email_address")
    public String getEmailAddress() {
        return emailAddress;
    }

    @JsonProperty("template_id")
    public UUID getTemplateId() {
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
    public UUID getEmailReplyToId() {
        return emailReplyToId;
    }

    @JsonProperty("one_click_unsubscribe_url")
    public URI getOneClickUnsubscribeURL() {
        return oneClickUnsubscribeURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyEmailRequest request = (NotifyEmailRequest) o;
        return Objects.equals(emailAddress, request.emailAddress) && Objects.equals(templateId, request.templateId) && Objects.equals(personalisation, request.personalisation) && Objects.equals(reference, request.reference) && Objects.equals(emailReplyToId, request.emailReplyToId) && Objects.equals(oneClickUnsubscribeURL, request.oneClickUnsubscribeURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress, templateId, personalisation, reference, emailReplyToId, oneClickUnsubscribeURL);
    }

    @Override
    public String toString() {
        return "NotifyEmailRequest{" +
                "emailAddress='" + emailAddress + '\'' +
                ", templateId='" + templateId + '\'' +
                ", personalisation=" + personalisation +
                ", reference='" + reference + '\'' +
                ", emailReplyToId='" + emailReplyToId + '\'' +
                ", oneClickUnsubscribeURL=" + oneClickUnsubscribeURL +
                '}';
    }
}
