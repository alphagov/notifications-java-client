package gov.va.vanotify;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static gov.va.vanotify.GsonConfiguration.gsonInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SendPushResponseTest {

    @Test
    public void testNotificationResponseForPushResponse(){
        JsonObject postPushReponse = new JsonObject();
        postPushReponse.addProperty("result", "success");

        SendPushResponse response = gsonInstance.fromJson(postPushReponse.toString(), SendPushResponse.class);
        assertEquals("success", response.getResult());
    }
}