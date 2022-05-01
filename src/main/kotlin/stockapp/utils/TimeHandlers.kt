package stockapp.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.*


private enum class Weekdays(val day: String) {
    MONDAY("MONDAY"),
    TUESDAY("TUESDAY"),
    WEDNESDAY("WEDNESDAY"),
    THURSDAY("THURSDAY"),
    FRIDAY("FRIDAY"),
}

private enum class Weekends(val day: String) {
    SATURDAY("SATURDAY"),
    SUNDAY("SUNDAY")
}

private enum class TimePeriod(val period: Int) {
    START(570),
    END(960)
}

private enum class ExtendedTimePeriod(val period: Int) {
    START(240),
    END(1200)
}

// only update if returns true
fun updateIntervalCheck(currentTime: Instant, lastUpdated: Instant, interval: Long, extended: Boolean): Boolean {
    val diffInMinutes = lastUpdated.until(currentTime, DateTimeUnit.MINUTE, TimeZone.of("EST"))
    if (diffInMinutes < interval) {
        return false
    }
    if (!isWeekdayCheck(lastUpdated)) {
        return false
    }
    if (!isMarketOpen(lastUpdated)) {
        return false
    }
    if (extended and !isExtendedHours(lastUpdated)) {
        return false
    }
    return true
}

// check if it was updated on current day, and if it was, was it before market close, and if market is closed, then return true to update
fun updateAfterMarketCloseCheck(currentTime: Instant, lastUpdated: Instant): Boolean {
    if (lastUpdated.toLocalDateTime(TimeZone.of("EST")).date == currentTime.toLocalDateTime(TimeZone.of("EST")).date
        && lastUpdated.toLocalDateTime(TimeZone.of("EST")).minute < TimePeriod.END.period) {
        return true
    }
    return false
}

fun isWeekdayCheck(time: Instant): Boolean {
    val day = time.toLocalDateTime(TimeZone.of("EST")).dayOfWeek.toString()
    if (Weekends.values().any { it.day == day }) {
        return false
    }
    return true
}

fun isMarketOpen(time: Instant): Boolean {
    if (time.toLocalDateTime(TimeZone.of("EST")).minute in TimePeriod.START.period ..TimePeriod.END.period) {
        return true
    }
    return false
}

fun isExtendedHours(time: Instant): Boolean {
    if (time.toLocalDateTime(TimeZone.of("EST")).minute in ExtendedTimePeriod.START.period ..ExtendedTimePeriod.END.period) {
        return true
    }
    return false
}

fun isToday(currentTime: Instant, lastUpdated: Instant): Boolean {
    if (lastUpdated.toLocalDateTime(TimeZone.of("EST")).date == currentTime.toLocalDateTime(TimeZone.of("EST")).date) {
        return true
    }
    return false
}

