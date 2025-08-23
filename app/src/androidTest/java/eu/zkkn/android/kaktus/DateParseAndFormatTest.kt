package eu.zkkn.android.kaktus

import android.os.Build
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@SmallTest
class DateParseAndFormatTest {

    @Test
    fun stringToDate() {
        val dateString = "2025-08-16T17:05:59+02:00"

        val expectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"))
        expectedCalendar.clear()
        expectedCalendar.set(2025, Calendar.AUGUST, 16, 17, 5, 59)
        expectedCalendar.set(Calendar.MILLISECOND, 0)
        val expectedDate: Date = expectedCalendar.time

        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US).parse(dateString)
        assertEquals(expectedDate, date)

        // Check also with another patterns for time zome
        assertEquals(expectedDate,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(dateString))
        assertEquals(expectedDate,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZZ", Locale.US).parse(dateString))
    }

    @Test
    fun stringToDate_CET() {
        val dateString = "2025-01-02T03:04:05+01:00"

        val expectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"))
        expectedCalendar.clear()
        expectedCalendar.set(2025, Calendar.JANUARY, 2, 3, 4, 5)
        expectedCalendar.set(Calendar.MILLISECOND, 0)
        val expectedDate: Date = expectedCalendar.time

        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US).parse(dateString)
        assertEquals(expectedDate, date)

        // Check also with another patterns for time zome
        assertEquals(expectedDate,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(dateString))
        assertEquals(expectedDate,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZZ", Locale.US).parse(dateString))
    }

    @Test
    fun stringToDate_EST() {
        val dateString = "2025-01-02T03:04:05-05:00"

        val expectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("America/Toronto"))
        expectedCalendar.clear()
        expectedCalendar.set(2025, Calendar.JANUARY, 2, 3, 4, 5)
        expectedCalendar.set(Calendar.MILLISECOND, 0)
        val expectedDate: Date = expectedCalendar.time

        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US).parse(dateString)
        assertEquals(expectedDate, date)
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun stringToDate_javaTime() {
        val dateString = "2025-08-16T17:05:59+02:00"

        val expectedZonedDateTime: ZonedDateTime = ZonedDateTime.of(
            2025, Month.AUGUST.value, 16, 17, 5, 59, 0,
            ZoneOffset.ofHours(2)
        )

        val zonedDateTime = ZonedDateTime.parse(dateString)
        assertEquals(expectedZonedDateTime, zonedDateTime)

        val zonedDateTimeFormatter = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        assertEquals(expectedZonedDateTime, zonedDateTimeFormatter)
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun stringToDate_javaTime_CET() {
        val dateString = "2025-01-02T03:04:05+01:00"

        val expectedZonedDateTime: ZonedDateTime = ZonedDateTime.of(
            2025, Month.JANUARY.value, 2, 3, 4, 5, 0,
            ZoneOffset.ofHours(1)
        )

        val zonedDateTime = ZonedDateTime.parse(dateString)
        assertEquals(expectedZonedDateTime, zonedDateTime)

        val zonedDateTimeFormatter = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        assertEquals(expectedZonedDateTime, zonedDateTimeFormatter)
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun stringToDate_javaTime_EST() {
        val dateString = "2025-01-02T03:04:05-05:00"

        val expectedZonedDateTime: ZonedDateTime = ZonedDateTime.of(
            2025, Month.JANUARY.value, 2, 3, 4, 5, 0,
            ZoneOffset.ofHours(-5)
        )

        val zonedDateTime = ZonedDateTime.parse(dateString)
        assertEquals(expectedZonedDateTime, zonedDateTime)

        val zonedDateTimeFormatter = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        assertEquals(expectedZonedDateTime, zonedDateTimeFormatter)
    }

    @Test
    fun dateToString() {
        val timeZone = TimeZone.getTimeZone("Europe/Prague")
        val calendar = Calendar.getInstance(timeZone)
        calendar.clear()
        calendar.set(2025, Calendar.AUGUST, 16, 17, 5, 59)
        calendar.set(Calendar.MILLISECOND, 0)
        val date: Date = calendar.time

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        dateFormat.timeZone = timeZone
        val stringDate = dateFormat.format(date)

        val expectedString = "2025-08-16T17:05:59+02:00"

        assertEquals(expectedString, stringDate)

        // Check also with another pattern for time zome
        assertEquals(
            expectedString,
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZZ", Locale.US).run {
                this.timeZone = timeZone
                format(date)
            })
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun dateToString_javaTime() {
        val date: ZonedDateTime = ZonedDateTime.of(2025, Month.AUGUST.value, 16, 17, 5, 59, 0,
            ZoneId.of("Europe/Prague"))

        val dateString = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        assertEquals("2025-08-16T17:05:59+02:00", dateString)
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun dateToString_javaTime_CET() {
        val date: ZonedDateTime = ZonedDateTime.of(2025, Month.JANUARY.value, 16, 17, 5, 59, 0,
            ZoneId.of("Europe/Prague"))

        val dateString = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        assertEquals("2025-01-16T17:05:59+01:00", dateString)
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun dateToString_javaTime_EST() {
        val date: ZonedDateTime = ZonedDateTime.of(2025, Month.JANUARY.value, 16, 17, 5, 59, 0,
            ZoneId.of("America/Toronto"))

        val dateString = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        assertEquals("2025-01-16T17:05:59-05:00", dateString)
    }

}
