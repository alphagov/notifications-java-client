package gov.va.vanotify;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

public class PushBroadcastRequestTest {

    @Test
    public void shouldThrowIfTemplateIdMissing(){
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new PushBroadcastRequest.Builder()
                    .build();
        });
        assertEquals("Missing templateId", exception.getMessage());
    }

    @Test
    public void shouldThrowIfTopicSIDIsMissing() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new PushBroadcastRequest.Builder()
                    .withTemplateId("1234")
                    .build();
        });
        assertEquals("Missing broadcast topic_sid", exception.getMessage());
    }

    @Test
    public void shouldThrowIfTopicSIDIsEmpty()  {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new PushBroadcastRequest.Builder()
                    .withTopicSID("")
                    .withTemplateId("1234")
                    .build();
        });
        assertEquals("Missing broadcast topic_sid", exception.getMessage());
    }

    @Test
    public void shouldCreateRequestWithTopicSID() {
        String topicSid = "TOPIC123";

        PushBroadcastRequest request = new PushBroadcastRequest.Builder()
                .withTopicSID(topicSid)
                .withTemplateId("1234")
                .build();

        assertNotNull(request);
        assertEquals(topicSid, request.getTopicSID());
    }

    @Test
    public void shouldCreateRequestWithPersonalisation() {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("name", "Alice");

        PushBroadcastRequest request = new PushBroadcastRequest.Builder()
                .withTopicSID("TOPIC123")
                .withTemplateId("1234")
                .withPersonalisation(personalisation)
                .build();

        assertEquals(personalisation, request.getPersonalisation());
    }
}