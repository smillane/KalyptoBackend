package stockapp.utils

import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

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

    private val minimumInterval: Int = 5

    suspend fun generateCurrentTimeStamp(): Long {
        return Instant.now().epochSecond
    }

    suspend fun isWeekdayCheck(inputDay: String): Boolean {
        val currentDay = Instant.now().epochSecond
        val dayOfWeek = Instant.ofEpochSecond(currentDay)
            .atZone( ZoneId.of("America/New_York"))
            .dayOfWeek
            .getDisplayName( TextStyle.FULL, Locale.US )
        if (Weekends.values().any { it.day == inputDay } ) {
            return true
        }
        return false
    }

    suspend fun lastUpdateQuery() {

    }

    suspend fun updateTimePeriodCheck(lastUpdated: Long): Boolean {
        // add logic to find time diff from last lastUpdated to currentTime
        val lastUpdatedToNow: Int = TODO()
        if (lastUpdatedToNow <= 300 ) {
            return false
        }
        return true
    }
}