package eu.zkkn.android.kaktus

import androidx.core.net.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun String.toDateOrNull(pattern: String): Date? {
    val dateFormat = SimpleDateFormat(pattern, Locale.US)
    return try {
        dateFormat.parse(this)
    } catch (_: ParseException) {
        null
    }
}
