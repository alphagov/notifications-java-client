package uk.gov.service.notify;

import java.time.format.DateTimeFormatter;

public class DateUtils {
    /**
     * Date time formatter for ISO date time which includes zone-offset 'Z' for zero.
     */
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
}
