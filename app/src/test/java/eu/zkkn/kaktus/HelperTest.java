package eu.zkkn.kaktus;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import eu.zkkn.android.kaktus.Helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class HelperTest {

    @Test
    public void parseFbDate_correct() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(Helper.parseFbDate("2017-02-05T13:30:59+0000"));

        assertEquals(2017, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.FEBRUARY, calendar.get(Calendar.MONTH));
        assertEquals(5, calendar.get(Calendar.DATE));
        assertEquals(13, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, calendar.get(Calendar.MINUTE));
        assertEquals(59, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void parseFbDate_malformed() {
        Date before = new Date();
        Date date = Helper.parseFbDate("");
        Date after = new Date();

        assertNotNull(date);
        // it must be in interval between "before" and "after"
        assertTrue(before.getTime() <= date.getTime());
        assertTrue(after.getTime() >= date.getTime());
    }

}
