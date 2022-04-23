package stockapp.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import org.springframework.stereotype.Component
import kotlinx.datetime.*

@Component
class TimeHandlers {
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

    private enum class TimePeriod(val period: String) {
        START("9:30"),
        END("4:00")
    }

    private enum class ExtendedTimePeriod(val period: String) {
        START("7:00"),
        END("20:00")
    }

    suspend fun lastUpdateQuery(currentTime: Instant, lastUpdated: Instant) {
        val diffInMinutes = lastUpdated.until(currentTime, DateTimeUnit.MINUTE, TimeZone.of("EST"))
    }

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

    fun isWeekdayCheck(time: Instant): Boolean {
        val day = time.toLocalDateTime(TimeZone.of("EST")).dayOfWeek.toString()
        if (Weekdays.values().any { it.name == day }) {
            return true
        }
        return false
    }

    fun isMarketOpen(currentTime: Instant): Boolean {
        if (currentTime.toLocalDateTime(TimeZone.of("EST")).minute in 570..960) {
            return true
        }
        return false
    }

    fun isExtendedHours(currentTime: Instant): Boolean {
        if (currentTime.toLocalDateTime(TimeZone.of("EST")).minute in 240..1200) {
            return true
        }
        return false
    }

//    val period: DateTimePeriod = instantInThePast.periodUntil(Clock.System.now(), TimeZone.of("EST"))
}
