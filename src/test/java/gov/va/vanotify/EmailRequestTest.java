package gov.va.vanotify;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static gov.va.vanotify.GsonConfiguration.gsonInstance;
import static org.junit.jupiter.api.Assertions.*;


public class EmailRequestTest {

    public static Object[][] missingArgumentsTestData() {
        return new Object[][]{
                {null, null},
                {"", null}
        };
    }

    @ParameterizedTest
    @MethodSource("missingArgumentsTestData")
    public void emailRequestRequiresEmailAddressOrIdentifier(String emailAddress, Identifier identifier){
        assertThrows(IllegalStateException.class, () -> {
            new EmailRequest.Builder()
                    .withTemplateId(UUID.randomUUID().toString())
                    .withEmailAddress(emailAddress)
                    .withRecipientIdentifier(identifier)
                    .build();
        });
    }

    @Test
    public void emailRequestRequiresTemplateId(){
        assertThrows(IllegalStateException.class, () -> {
            new EmailRequest.Builder().withEmailAddress("user@email.com").build();
        });
    }

    public static Object[][] requiredArgumentsTestData() {
        return new Object[][]{
                {UUID.randomUUID().toString(), "user@email.com", null},
                {UUID.randomUUID().toString(), null, new Identifier(IdentifierType.ICN, "1234")},
        };
    }

    @ParameterizedTest
    @MethodSource("requiredArgumentsTestData")
    public void emailRequestCanBeCreatedWithOnlyRequiredFields(String templateId, String emailAddress, Identifier identifier){
        EmailRequest request = new EmailRequest.Builder()
                .withTemplateId(templateId)
                .withEmailAddress(emailAddress)
                .withRecipientIdentifier(identifier)
                .build();

        assertEquals(emailAddress, request.getEmailAddress());
        assertEquals(templateId, request.getTemplateId());
        assertEquals(identifier, request.getRecipientIdentifier());
        assertNull(request.getBillingCode());
        assertNull(request.getReference());
        assertNull(request.getPersonalisation());
    }

    @Test
    public void emailRequestCanBeConvertedToJson() {
        String templateId = UUID.randomUUID().toString();
        String emailAddress = "user@email.com";
        String reference = "some-reference";
        String billingCode = "some-billing-code";
        Map<String, String> personalisation = new HashMap(){{
            put("foo", "bar");
        }};
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");
        String callbackUrl = "https://example.com/callback";


        EmailRequest request = new EmailRequest.Builder()
                .withTemplateId(templateId)
                .withEmailAddress(emailAddress)
                .withReference(reference)
                .withBillingCode(billingCode)
                .withPersonalisation(personalisation)
                .withRecipientIdentifier(identifier)
                .withCallbackUrl(callbackUrl)
                .build();

        JsonObject actual = gsonInstance.toJsonTree(request).getAsJsonObject();
        assertEquals(templateId, actual.get("template_id").getAsString());
        assertEquals(emailAddress, actual.get("email_address").getAsString());
        assertEquals(reference, actual.get("reference").getAsString());
        assertEquals(billingCode, actual.get("billing_code").getAsString());
        assertEquals(callbackUrl, actual.get("callback_url").getAsString());
        assertEquals("bar", actual.getAsJsonObject("personalisation").get("foo").getAsString());
        assertEquals("ICN", actual.getAsJsonObject("recipient_identifier").get("id_type").getAsString());
        assertEquals(identifier.getValue(), actual.getAsJsonObject("recipient_identifier").get("id_value").getAsString());
    }
}
