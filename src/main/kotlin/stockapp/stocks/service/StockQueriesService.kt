package stockapp.stocks.service

import org.springframework.stereotype.Component

@Component
class StockQueriesService {
    private val doesNotExist: String = "false"
    private val weekdays: Array<String> = arrayOf("Mon", "Tues", "Wed", "Thurs", "Fri")
    private val weekends: Array<String> = arrayOf("Sat", "Sun")
    private val timePeriods: Map<String, Map<String, String>> = mapOf("default" to mapOf("start" to "9:30", "end" to "16:00"), "basicQuote" to mapOf("start" to "7:00", "end" to "20:00"))

    fun getStockInformation(stockID: String) {
        if (!dbCheck(stockID)) {
            return
        }
    }

    private fun dbCheck(stockID: String): Boolean {
        if (!apiCheck(stockID)) {
            return false
        }
        firstRunQueryAndSave(stockID)
        return true
    }

    private fun apiCheck(stockID: String): Boolean {
        if (TODO()) {
            return false
    }
        return true
    }

    fun firstRunQueryAndSave(stockID: String) {

    }

    fun updateAndReplace(stockID: String) {

    }

    fun updateOnIntervalsAndAdd(stockID: String) {

    }

    fun findAndReturn(stockID: String) {

    }

    fun updateIntervalCheck() {

    }

    fun updateAndAddToList() {

    }

    fun getDocsFromDB() {

    }

    fun updateDocsInDB() {

    }

    fun updateListInDB() {

    }
}