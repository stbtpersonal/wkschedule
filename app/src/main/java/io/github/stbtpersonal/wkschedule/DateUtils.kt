package io.github.stbtpersonal.wkschedule

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    init {
        this.iso8601Format.timeZone = TimeZone.getTimeZone("UTC")
    }

    fun toIso8601(date: Date): String {
        return this.iso8601Format.format(date)
    }
}