package uk.gov.service.notify;

import org.joda.time.DateTime;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class NotificationTest {

    @Test
    public void testEmailNotification_canCreateObjectFromJson() {
        JSONObject content = new JSONObject();
        String id = UUID.randomUUID().toString();
        content.put("id", id);
        content.put("reference", "client_reference");
        content.put("email_address", "some@address.com");
        content.put("phone_number", null);
        content.put("line_1", null);
        content.put("line_2", null);
        content.put("line_3", null);
        content.put("line_4", null);
        content.put("line_5", null);
        content.put("line_6", null);
        content.put("postcode", null);
        content.put("type", "email");
        content.put("status", "delivered");
        JSONObject template = new JSONObject();
        String templateId = UUID.randomUUID().toString();
        template.put("id", templateId);
        template.put("version", 1);
        template.put("uri", "https://api.notifications.service.gov.uk/templates/" + templateId);
        content.put("template", template);
        content.put("body", "Body of the message");
        content.put("subject", "Subject of the message");
        content.put("created_at", "2016-03-01T08:30:00.000Z");
        content.put("sent_at", "2016-03-01T08:30:03.000Z");
        content.put("completed_at", "2016-03-01T08:30:43.000Z");
        content.put("estimated_delivery", "2016-03-03T16:00:00.000Z");
        content.put("created_by_name", "John Doe");
        Notification notification = new Notification(content.toString());
        assertEquals(UUID.fromString(id), notification.getId());
        assertEquals(Optional.of("client_reference"), notification.getReference());
        assertEquals(UUID.fromString(templateId), notification.getTemplateId());
        assertEquals("email", notification.getNotificationType());
        assertEquals(Optional.of("some@address.com"), notification.getEmailAddress());
        assertEquals(Optional.<String>empty(), notification.getPhoneNumber());
        assertEquals(Optional.<String>empty(), notification.getLine1());
        assertEquals(Optional.<String>empty(), notification.getLine2());
        assertEquals(Optional.<String>empty(), notification.getLine3());
        assertEquals(Optional.<String>empty(), notification.getLine4());
        assertEquals(Optional.<String>empty(), notification.getLine5());
        assertEquals(Optional.<String>empty(), notification.getLine6());
        assertEquals(Optional.<String>empty(), notification.getPostcode());
        assertEquals(UUID.fromString(templateId), notification.getTemplateId());
        assertEquals(1, notification.getTemplateVersion());
        assertEquals("https://api.notifications.service.gov.uk/templates/" + templateId, notification.getTemplateUri());
        assertEquals("Body of the message", notification.getBody());
        assertEquals(Optional.of("Subject of the message"), notification.getSubject());
        assertEquals(new DateTime("2016-03-01T08:30:00.000Z"), notification.getCreatedAt());
        assertEquals(Optional.of(new DateTime("2016-03-01T08:30:03.000Z")), notification.getSentAt());
        assertEquals(Optional.of(new DateTime("2016-03-01T08:30:43.000Z")), notification.getCompletedAt());
        assertEquals(Optional.of(new DateTime("2016-03-03T16:00:00.000Z")), notification.getEstimatedDelivery());
        assertEquals(Optional.of("John Doe"), notification.getCreatedByName());

    }

    @Test
    public void testSmsNotification_canCreateObjectFromJson() {
        JSONObject content = new JSONObject();
        String id = UUID.randomUUID().toString();
        content.put("id", id);
        content.put("reference", "client_reference");
        content.put("email_address", null);
        content.put("phone_number", "+447111111111");
        content.put("line_1", null);
        content.put("line_2", null);
        content.put("line_3", null);
        content.put("line_4", null);
        content.put("line_5", null);
        content.put("line_6", null);
        content.put("postcode", null);
        content.put("type", "sms");
        content.put("status", "delivered");
        JSONObject template = new JSONObject();
        String templateId = UUID.randomUUID().toString();
        template.put("id", templateId);
        template.put("version", 1);
        template.put("uri", "https://api.notifications.service.gov.uk/templates/" + templateId);
        content.put("template", template);
        content.put("body", "Body of the message");
        content.put("subject", null);
        content.put("created_at", "2016-03-01T08:30:00.000Z");
        content.put("sent_at", "2016-03-01T08:30:03.000Z");
        content.put("completed_at", "2016-03-01T08:30:43.000Z");
        content.put("estimated_delivery", "2016-03-03T16:00:00.000Z");
        content.put("created_by_name", "John Doe");

        Notification notification = new Notification(content.toString());
        assertEquals(UUID.fromString(id), notification.getId());
        assertEquals(Optional.of("client_reference"), notification.getReference());
        assertEquals(UUID.fromString(templateId), notification.getTemplateId());
        assertEquals("sms", notification.getNotificationType());

        assertEquals(Optional.of("+447111111111"), notification.getPhoneNumber());
        assertEquals(Optional.<String>empty(), notification.getEmailAddress());
        assertEquals(Optional.<String>empty(), notification.getLine1());
        assertEquals(Optional.<String>empty(), notification.getLine2());
        assertEquals(Optional.<String>empty(), notification.getLine3());
        assertEquals(Optional.<String>empty(), notification.getLine4());
        assertEquals(Optional.<String>empty(), notification.getLine5());
        assertEquals(Optional.<String>empty(), notification.getLine6());
        assertEquals(Optional.<String>empty(), notification.getPostcode());
        assertEquals(UUID.fromString(templateId), notification.getTemplateId());
        assertEquals(1, notification.getTemplateVersion());
        assertEquals("https://api.notifications.service.gov.uk/templates/" + templateId, notification.getTemplateUri());
        assertEquals("Body of the message", notification.getBody());
        assertEquals(Optional.empty(), notification.getSubject());
        assertEquals(new DateTime("2016-03-01T08:30:00.000Z"), notification.getCreatedAt());
        assertEquals(Optional.of(new DateTime("2016-03-01T08:30:03.000Z")), notification.getSentAt());
        assertEquals(Optional.of(new DateTime("2016-03-01T08:30:43.000Z")), notification.getCompletedAt());
        assertEquals(Optional.of(new DateTime("2016-03-03T16:00:00.000Z")), notification.getEstimatedDelivery());
        assertEquals(Optional.of("John Doe"), notification.getCreatedByName());

    }


    @Test
    public void testLetterNotification_canCreateObjectFromJson() {
        JSONObject content = new JSONObject();
        String id = UUID.randomUUID().toString();
        content.put("id", id);
        content.put("reference", "client_reference");
        content.put("email_address", null);
        content.put("phone_number", null);
        content.put("line_1", "the queen");
        content.put("line_2", "buckingham palace");
        content.put("line_3", null);
        content.put("line_4", null);
        content.put("line_5", null);
        content.put("line_6", null);
        content.put("postcode", "sw1 1aa");
        content.put("type", "letter");
        content.put("status", "delivered");
        JSONObject template = new JSONObject();
        String templateId = UUID.randomUUID().toString();
        template.put("id", templateId);
        template.put("version", 1);
        template.put("uri", "https://api.notifications.service.gov.uk/templates/" + templateId);
        content.put("template", template);
        content.put("body", "Body of the message");
        content.put("subject", null);
        content.put("created_at", "2016-03-01T08:30:00.000Z");
        content.put("sent_at", "2016-03-01T08:30:03.000Z");
        content.put("completed_at", "2016-03-01T08:30:43.000Z");
        content.put("estimated_delivery", "2016-03-03T16:00:00.000Z");
        content.put("created_by_name", "John Doe");

        Notification notification = new Notification(content.toString());
        assertEquals(UUID.fromString(id), notification.getId());
        assertEquals(Optional.of("client_reference"), notification.getReference());
        assertEquals(UUID.fromString(templateId), notification.getTemplateId());
        assertEquals("letter", notification.getNotificationType());

        assertEquals(Optional.<String>empty(), notification.getPhoneNumber());
        assertEquals(Optional.<String>empty(), notification.getEmailAddress());
        assertEquals(Optional.of("the queen"), notification.getLine1());
        assertEquals(Optional.of("buckingham palace"), notification.getLine2());
        assertEquals(Optional.<String>empty(), notification.getLine3());
        assertEquals(Optional.<String>empty(), notification.getLine4());
        assertEquals(Optional.<String>empty(), notification.getLine5());
        assertEquals(Optional.<String>empty(), notification.getLine6());
        assertEquals(Optional.of("sw1 1aa"), notification.getPostcode());
        assertEquals(UUID.fromString(templateId), notification.getTemplateId());
        assertEquals(1, notification.getTemplateVersion());
        assertEquals("https://api.notifications.service.gov.uk/templates/" + templateId, notification.getTemplateUri());
        assertEquals("Body of the message", notification.getBody());
        assertEquals(Optional.empty(), notification.getSubject());
        assertEquals(new DateTime("2016-03-01T08:30:00.000Z"), notification.getCreatedAt());
        assertEquals(Optional.of(new DateTime("2016-03-01T08:30:03.000Z")), notification.getSentAt());
        assertEquals(Optional.of(new DateTime("2016-03-01T08:30:43.000Z")), notification.getCompletedAt());
        assertEquals(Optional.of(new DateTime("2016-03-03T16:00:00.000Z")), notification.getEstimatedDelivery());
        assertEquals(Optional.of("John Doe"), notification.getCreatedByName());
    }
}
