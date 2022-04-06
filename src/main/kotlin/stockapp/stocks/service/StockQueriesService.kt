package stockapp.stocks.service

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class StockQueriesService(
    val iexApiService: IEXApiService
) {

    fun getStockInformation(stockID: String) {
        if (!dbCheck(stockID)) {
            return
        }
    }

    private fun dbCheck(stockID: String): Boolean {
//        if (!apiCheck(stockID)) {
//            return false
//        }
        firstRunQueryAndSave(stockID)
        return true
    }

    fun apiCheck(stockID: String): Flow<JsonNode> {
        return iexApiService.GetStockQuote(stockID)
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