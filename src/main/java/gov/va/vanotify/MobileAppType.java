package gov.va.vanotify;

public enum MobileAppType {
    VETEXT,
    VA_FLAGSHIP_APP;

    public static MobileAppType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("MobileAppType cannot be null");
        }
        try {
            return MobileAppType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                "Invalid MobileAppType: " + value + ". Allowed values: VETEXT, VA_FLAGSHIP_APP"
            );
        }
    }
}