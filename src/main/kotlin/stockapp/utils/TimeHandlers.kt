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
// if it's been less than the interval time since last update, return false
// if passing extended = true, and it's not extended hours, return false
// if it's not currently a weekday, return false
// if not extended hours, the market is not open and last updated was not during open market hours, return false
fun updateIntervalCheck(currentTime: Instant, lastUpdated: Instant, interval: Long, extended: Boolean): Boolean {
    val diffInMinutes = lastUpdated.until(currentTime, DateTimeUnit.MINUTE, TimeZone.of("EST"))
    if (diffInMinutes < interval) {
        return false
    }
    if (extended and !isExtendedHours(currentTime)) {
        return false
    }
    if (!isWeekday(currentTime)) {
        return false
    }
    if (!extended and !isMarketOpen(currentTime) and !isMarketOpen(lastUpdated)) {
        return false
    }
    return true
}

// check if it was updated on current/previous
// if last updated is not today, or day previous, then return true to update
// if last updated was yesterday and before market close, return true
// if last updated is today, return function updateOnlyAfterClose which then checks if it was updated before market close
// if updated before market close, then should update again due to being after market close
// if updated after market close, return false
fun updateAfterMarketClose(currentTime: Instant, lastUpdated: Instant): Boolean {
    if (lastUpdated.toLocalDateTime(TimeZone.of("EST")).date != currentTime.toLocalDateTime(TimeZone.of("EST")).date &&
        lastUpdated.toLocalDateTime(TimeZone.of("EST")).date != currentTime.toLocalDateTime(TimeZone.of("EST")).date.minus(1, DateTimeUnit.DAY)) {
        return true
    }
    if (lastUpdated.toLocalDateTime(TimeZone.of("EST")).date == currentTime.toLocalDateTime(TimeZone.of("EST")).date.minus(1, DateTimeUnit.DAY)) {
        if (lastUpdated.toLocalDateTime(TimeZone.of("EST")).minute < TimePeriod.END.period) {
            return true
        }
        return false
    }
    if (lastUpdated.toLocalDateTime(TimeZone.of("EST")).date == currentTime.toLocalDateTime(TimeZone.of("EST")).date) {
        return updateOnlyAfterClose(currentTime, lastUpdated)
    }
    return false
}

// return true if current time is after market closed and last updated was before market close
fun updateOnlyAfterClose(currentTime: Instant, lastUpdated: Instant): Boolean {
    if (currentTime.toLocalDateTime(TimeZone.of("EST")).minute > TimePeriod.END.period && lastUpdated.toLocalDateTime(TimeZone.of("EST")).minute < TimePeriod.END.period) {
        return true
    }
    return false
}

// return true if last updated = today
fun isToday(currentTime: Instant, lastUpdated: Instant): Boolean {
    if (lastUpdated.toLocalDateTime(TimeZone.of("EST")).date == currentTime.toLocalDateTime(TimeZone.of("EST")).date) {
        return true
    }
    return false
}


// return true if last updated = today
fun wasYesterday(currentTime: Instant, lastUpdated: Instant): Boolean {
    if (lastUpdated.toLocalDateTime(TimeZone.of("EST")).date == currentTime.toLocalDateTime(TimeZone.of("EST")).date.minus(1, DateTimeUnit.DAY)) {
        return true
    }
    return false
}
// return true if a weekday
fun isWeekday(time: Instant): Boolean {
    val day = time.toLocalDateTime(TimeZone.of("EST")).dayOfWeek.toString()
    if (!Weekends.values().any { it.day == day }) {
        return true
    }
    return false
}

// return true if markets open
fun isMarketOpen(time: Instant): Boolean {
    if (time.toLocalDateTime(TimeZone.of("EST")).minute in TimePeriod.START.period ..TimePeriod.END.period) {
        return true
    }
    return false
}

// return true if was last updated during extended hours
fun isExtendedHours(time: Instant): Boolean {
    val timeInMinutes = time.toLocalDateTime(TimeZone.of("EST")).minute
    if (timeInMinutes in ExtendedTimePeriod.START.period ..TimePeriod.START.period || timeInMinutes in TimePeriod.END.period .. ExtendedTimePeriod.END.period) {
        return true
    }
    return false
}