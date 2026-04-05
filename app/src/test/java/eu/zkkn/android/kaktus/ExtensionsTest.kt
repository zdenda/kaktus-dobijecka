package eu.zkkn.android.kaktus

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test


class ExtensionsTest {

    @Test
    fun toDateOrNull_validDate_returnsDate() {
        // SimpleDateFormat with ZZZZZ (ISO 8601) requires Java 7+, but let's use a simpler one for the test
        // or ensure the pattern matches what SimpleDateFormat expects in the test environment.
        // The issue is about catching the exception, not the pattern itself.
        val date = "2025-08-16".toDateOrNull("yyyy-MM-dd")
        assertNotNull(date)
    }

    @Test
    fun toDateOrNull_invalidDate_returnsNull() {
        val date = "invalid-date".toDateOrNull("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
        assertNull(date)
    }
}
