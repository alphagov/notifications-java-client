package uk.gov.service.notify;

import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TemplateTest {

    @Test
    public void testTemplate_canCreateObjectFromJson() {
        JSONObject content = new JSONObject();
        String id = UUID.randomUUID().toString();
        content.put("id", id);
        content.put("name", "my template");
        content.put("type", "email");
        content.put("created_at", "2017-05-01T08:30:00.000Z");
        content.put("updated_at", "2017-05-01T08:34:00.000Z");
        content.put("version", 3);
        content.put("body", "The body of the template. For ((name)) eyes only.");
        content.put("subject", "Private email");

        JSONObject required = new JSONObject();
        required.put("required", true);

        JSONObject personalisation = new JSONObject();
        personalisation.put("placeholder", required);
        personalisation.put("conditional", required);

        content.put("personalisation", personalisation);

        Template template = new Template(content.toString());
        assertEquals(UUID.fromString(id), template.getId());
        assertEquals("my template", template.getName());
        assertEquals("email", template.getTemplateType());
        assertEquals(ZonedDateTime.parse("2017-05-01T08:30:00.000Z"), template.getCreatedAt());
        assertEquals(Optional.of(ZonedDateTime.parse("2017-05-01T08:34:00.000Z")), template.getUpdatedAt());
        assertEquals(3, template.getVersion());
        assertEquals("The body of the template. For ((name)) eyes only.", template.getBody());
        assertEquals(Optional.of("Private email"), template.getSubject());

        Map<String, Object> expectedPersonalisation = new HashMap<>();
        Map<String, Object> expectedPersonalisationProperty = new HashMap<>();
        expectedPersonalisationProperty.put("required", true);
        expectedPersonalisation.put("placeholder", expectedPersonalisationProperty);
        expectedPersonalisation.put("conditional", expectedPersonalisationProperty);

        assertEquals(Optional.of(expectedPersonalisation), template.getPersonalisation());
    }



    @Test
    public void testTemplate_canCreateObjectFromJsonWithOptionals() {
        JSONObject content = new JSONObject();
        String id = UUID.randomUUID().toString();
        content.put("id", id);
        content.put("name", "my template");
        content.put("type", "email");
        content.put("created_at", "2017-05-01T08:30:00.000Z");
        content.put("updated_at", null);
        content.put("version", 3);
        content.put("body", "The body of the template. For ((name)) eyes only.");
        content.put("subject", null);
        content.put("personalisation", null);

        Template template = new Template(content.toString());
        assertEquals(UUID.fromString(id), template.getId());
        assertEquals("my template", template.getName());
        assertEquals("email", template.getTemplateType());
        assertEquals(ZonedDateTime.parse("2017-05-01T08:30:00.000Z"), template.getCreatedAt());
        assertEquals(Optional.empty(), template.getUpdatedAt());
        assertEquals(3, template.getVersion());
        assertEquals("The body of the template. For ((name)) eyes only.", template.getBody());
        assertEquals(Optional.empty(), template.getSubject());
        assertEquals(Optional.empty(), template.getPersonalisation());
    }

}
