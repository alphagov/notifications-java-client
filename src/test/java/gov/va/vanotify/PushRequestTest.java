package gov.va.vanotify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class PushRequestTest {

    @Test
    public void PushRequestRequiresTemplateId(){
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");

        assertThrows(IllegalStateException.class, () -> {
            new PushRequest.Builder()
                    .withRecipientIdentifier(identifier)
                    .build();
        });
    }

    @Test
    public void PushRequestRequiresRecipientId(){
        assertThrows(IllegalStateException.class, () -> {
            new PushRequest.Builder()
                    .withTemplateId("1234")
                    .build();
        });
    }

    @Test
    public void PushRequestRequiresICNRecipientId(){
        Identifier identifier = new Identifier(IdentifierType.VAPROFILEID, "1234");

        assertThrows(IllegalStateException.class, () -> {
            new PushRequest.Builder()
                    .withTemplateId("1234")
                    .withRecipientIdentifier(identifier)
                    .build();
        });
    }

    @Test
    public void PushRequestRequiredArguments(){
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");

        PushRequest request = new PushRequest.Builder()
                    .withTemplateId("1234")
                    .withRecipientIdentifier(identifier)
                    .build();
        
        assertEquals("1234", request.getTemplateId());
        assertEquals(identifier, request.getRecipientIdentifier());
    }

    @Test
    public void PushRequestMobileAppAllowedValues() {
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

    public static Object[] mobileAppInvalidTestData() {
        return new Object[]{
                "Foo",
                null
        };
    }

    @ParameterizedTest
    @MethodSource("mobileAppInvalidTestData")
    public void PushRequestMobileAppInvalidValues(String mobileApp) {
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");

        assertThrows(IllegalArgumentException.class, () -> {
            new PushRequest.Builder()
                    .withTemplateId("1234")
                    .withRecipientIdentifier(identifier)
                    .withMobileApp(MobileAppType.from(mobileApp))
                    .build();
        });
    }
}