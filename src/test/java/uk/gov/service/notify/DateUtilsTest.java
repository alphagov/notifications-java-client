package uk.gov.service.notify;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {

    @Test
    public void testParse() {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2016-03-01T08:30:00.000Z", DateUtils.DATE_TIME_FORMATTER);
        assertEquals(LocalDate.of(2016, 3, 1), zonedDateTime.toLocalDate());
        assertEquals(LocalTime.of(8, 30), zonedDateTime.toLocalTime());
    }
}