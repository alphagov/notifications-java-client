package gov.va.vanotify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MobileAppTypeTest {

    @Test
    void testFromValidValue() {
        assertEquals(MobileAppType.VA_FLAGSHIP_APP, MobileAppType.from("VA_FLAGSHIP_APP"));
    }

    @Test
    void testFromValidValueWithLowerCase() {
        assertEquals(MobileAppType.VA_FLAGSHIP_APP, MobileAppType.from("va_flagship_app"));
    }

    @Test
    void testFromValidValueWithWhitespace() {
        assertEquals(MobileAppType.VA_FLAGSHIP_APP, MobileAppType.from("  VA_FLAGSHIP_APP  "));
    }

    @Test
    void testFromNullThrows() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> MobileAppType.from(null));
        assertEquals("MobileAppType cannot be null", ex.getMessage());
    }

    @Test
    void testFromInvalidThrows() {
        String invalidValue = "INVALID_APP";
        Exception ex = assertThrows(IllegalArgumentException.class, () -> MobileAppType.from(invalidValue));
        assertTrue(ex.getMessage().contains("Invalid MobileAppType: INVALID_APP"));
        assertTrue(ex.getMessage().contains("VA_FLAGSHIP_APP")); // From ALLOWED_VALUES
    }

    @Test
    void testAllowedValuesConstantIncludesEnumName() {
        assertTrue(MobileAppType.ALLOWED_VALUES.contains("VA_FLAGSHIP_APP"));
    }
}