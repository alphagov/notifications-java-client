package uk.gov.service.notify;

import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ReceivedTextMessageTest {

    @Test
    public void testReceivedTextMessage_canCreateObjectFromJson(){
        JSONObject content = new JSONObject();
        String id = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        content.put("id", id);
        content.put("notify_number", "447700900111");
        content.put("user_number", "447700900000");
        content.put("service_id", serviceId);
        content.put("content", "Content of the message from the user");
        content.put("created_at","2016-03-01T08:30:00.000Z");

        ReceivedTextMessage receivedTextMessage = new ReceivedTextMessage(content.toString());
        assertEquals(UUID.fromString(id), receivedTextMessage.getId());
        assertEquals("447700900111", receivedTextMessage.getNotifyNumber());
        assertEquals("447700900000", receivedTextMessage.getUserNumber());
        assertEquals(UUID.fromString(serviceId), receivedTextMessage.getServiceId());
        assertEquals("Content of the message from the user", receivedTextMessage.getContent());
        assertEquals(LocalDateTime.parse("2016-03-01T08:30:00.000Z", DateUtils.DATE_TIME_FORMATTER), receivedTextMessage.getCreatedAt());
    }
}
