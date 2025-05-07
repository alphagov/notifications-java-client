package gov.va.vanotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

public class PushRequestTest {

    @Test
    public void shouldThrowIfTemplateIdMissing(){        
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new PushRequest.Builder()
                    .withRecipientIdentifier(identifier)
                    .build();
        });
        assertEquals("Missing templateId", exception.getMessage());
    }

    @Test
    public void shouldThrowIfRecipientIdMissing(){
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new PushRequest.Builder()
                    .withTemplateId("1234")
                    .build();
        });
        assertEquals("Missing ICN recipientIdentifier", exception.getMessage());
    }

    @Test
    public void shouldThrowIfRecipientIdNotICN(){
        Identifier identifier = new Identifier(IdentifierType.VAPROFILEID, "1234");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new PushRequest.Builder()
                    .withTemplateId("1234")
                    .withRecipientIdentifier(identifier)
                    .build();
        });
        assertEquals("recipientIdentifier must be of type ICN", exception.getMessage());
    }

    public static Object[] mobileAppInvalidTestData() {
        return new Object[]{
                "Foo",
                null
        };
    }

    @ParameterizedTest
    @MethodSource("mobileAppInvalidTestData")
    public void shouldThrowIfInvalidMobileApp(String mobileApp) {
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");

        assertThrows(IllegalArgumentException.class, () -> {
            new PushRequest.Builder()
                    .withTemplateId("1234")
                    .withRecipientIdentifier(identifier)
                    .withMobileApp(MobileAppType.from(mobileApp))
                    .build();
        });
    }

    @Test
    public void shouldCreateRequestWithRequiredArguments(){
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");

        PushRequest request = new PushRequest.Builder()
                    .withTemplateId("1234")
                    .withRecipientIdentifier(identifier)
                    .build();
        
        assertEquals("1234", request.getTemplateId());
        assertEquals(identifier, request.getRecipientIdentifier());
    }

    @Test
    public void shouldCreateRequestWithOptionalMobileApp() {
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");
        MobileAppType mobileApp = MobileAppType.VA_FLAGSHIP_APP;

        PushRequest request = new PushRequest.Builder()
                    .withTemplateId("1234")
                    .withRecipientIdentifier(identifier)
                    .withMobileApp(mobileApp)
                    .build();
        
        assertEquals("1234", request.getTemplateId());
        assertEquals(identifier, request.getRecipientIdentifier());
        assertEquals(mobileApp, request.getMobileApp());
    }

    @Test
    public void shouldCreateRequestWithPersonalisation() {
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("name", "Alice");

        PushRequest request = new PushRequest.Builder()
                .withTemplateId("1234")
                .withRecipientIdentifier(identifier)
                .withPersonalisation(personalisation)
                .build();

        assertEquals(personalisation, request.getPersonalisation());
    }
}