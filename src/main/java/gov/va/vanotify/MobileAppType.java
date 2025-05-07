package gov.va.vanotify;

import java.util.Arrays;

/**
 * Enumeration of supported mobile app types for VA Notify push notifications.
 * 
 * This enum currently includes a single value to match the external API contract,
 * but is designed to allow future expansion without breaking compatibility.
 */
public enum MobileAppType {
    /**
     * Represents the VA Flagship mobile application.
     */
    VA_FLAGSHIP_APP;

    /**
     * Comma-separated string of all allowed enum values.
     * Used for error messages and validation documentation.
     */
    public static final String ALLOWED_VALUES = Arrays.toString(MobileAppType.values());

    /**
     * Parses a string to return the matching {@link MobileAppType} enum constant.
     * 
     * This method is case-insensitive and ignores leading/trailing whitespace.
     *
     * @param value the string representation of the enum constant (e.g. "VA_FLAGSHIP_APP")
     * @return the corresponding {@link MobileAppType}
     * @throws IllegalArgumentException if the input is null or does not match any enum constant
     */
    public static MobileAppType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("MobileAppType cannot be null");
        }
        try {
            return MobileAppType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                "Invalid MobileAppType: " + value + ". Allowed values: " + ALLOWED_VALUES
            );
        }
    }
}