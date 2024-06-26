package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NotifySmsRequest {
    private final String phoneNumber;
    private final UUID templateId;
    private final Map<String, ?> personalisation;
    private final String reference;
    private final UUID smsSenderId;

    public NotifySmsRequest(@JsonProperty("phone_number") String phoneNumber,
                            @JsonProperty("template_id") UUID templateId,
                            @JsonProperty("personalisation") Map<String, ?> personalisation,
                            @JsonProperty("reference") String reference,
                            @JsonProperty("sms_sender_id") UUID smsSenderId) {
        this.phoneNumber = phoneNumber;
        this.templateId = templateId;
        this.personalisation = personalisation;
        this.reference = reference;
        this.smsSenderId = smsSenderId;
    }

    @JsonProperty("phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonProperty("temnplate_id")
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

    @JsonProperty("sms_sender_id")
    public UUID getSmsSenderId() {
        return smsSenderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifySmsRequest that = (NotifySmsRequest) o;
        return Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(templateId, that.templateId) && Objects.equals(personalisation, that.personalisation) && Objects.equals(reference, that.reference) && Objects.equals(smsSenderId, that.smsSenderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, templateId, personalisation, reference, smsSenderId);
    }

    @Override
    public String toString() {
        return "NotifySmsRequest{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", templateId='" + templateId + '\'' +
                ", personalisation=" + personalisation +
                ", reference='" + reference + '\'' +
                ", smsSenderId='" + smsSenderId + '\'' +
                '}';
    }
}
