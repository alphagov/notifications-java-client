package gov.va.vanotify;

import org.junit.jupiter.api.Test;

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
    public void PushRequestRequiredArguments(){
        Identifier identifier = new Identifier(IdentifierType.ICN, "1234");

        PushRequest request = new PushRequest.Builder()
                    .withTemplateId("1234")
                    .withRecipientIdentifier(identifier)
                    .build();
        
        assertEquals("1234", request.getTemplateId());
        assertEquals(identifier, request.getRecipientIdentifier());
    }
}