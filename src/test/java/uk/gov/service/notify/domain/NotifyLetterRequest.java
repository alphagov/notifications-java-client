package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NotifyLetterRequest {

    private final UUID templateId;
    private final Map<String, ?> personalisation;
    private final String reference;

    public NotifyLetterRequest(@JsonProperty("template_id") UUID templateId,
                               @JsonProperty("personalisation") Map<String, ?> personalisation,
                               @JsonProperty("reference") String reference) throws NotificationClientException {
        this.templateId = templateId;
        this.personalisation = personalisation;
        this.reference = reference;
        // this is easier after java 8 by using List.of() in place
        final List<String> minimumPersonalisationKeys = new ArrayList<>();
        minimumPersonalisationKeys.add("address_line_1");
        minimumPersonalisationKeys.add("address_line_2");
        minimumPersonalisationKeys.add("address_line_3");
        // the current code in main does not perform this check although it is listed in the api docs
        if(Objects.isNull(personalisation)||!personalisation.keySet().containsAll(minimumPersonalisationKeys)) {
            throw new NotificationClientException("need at least the first three lines of the address");
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyLetterRequest that = (NotifyLetterRequest) o;
        return Objects.equals(templateId, that.templateId) && Objects.equals(personalisation, that.personalisation) && Objects.equals(reference, that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateId, personalisation, reference);
    }

    @Override
    public String toString() {
        return "NotifyLetterRequest{" +
                "templateId='" + templateId + '\'' +
                ", personalisation=" + personalisation +
                ", reference='" + reference + '\'' +
                '}';
    }
}
