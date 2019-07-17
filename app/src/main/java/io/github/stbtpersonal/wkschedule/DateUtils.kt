package io.github.stbtpersonal.wkschedule

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    val epoch = Date()

    init {
        this.iso8601Format.timeZone = TimeZone.getTimeZone("UTC")
        this.epoch.time = 0
    }

    fun toIso8601(date: Date): String {
        return this.iso8601Format.format(date)
    }

    fun fromIso8601(dateString: String): Date {
        return this.iso8601Format.parse(dateString)!!
    }
}