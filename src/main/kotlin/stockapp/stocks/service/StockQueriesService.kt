package stockapp.stocks.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json
import org.springframework.stereotype.Component


@Component
class StockQueriesService(
    val iexApiService: IEXApiService,
) {
    private val returnError: Flow<String> = flowOf("false")
    private val returnNotFound: Flow<String> = flowOf("notFound")
    val mapper = ObjectMapper()

    suspend fun getStockInformation(stockID: String): String {
        if (!dbCheck(stockID)) {
            if (!apiCheck(stockID)) {
                return "false"
            }
            updateDocs(stockID)
        }
        return "temp"
    }

    suspend fun dbCheck(stockID: String): Boolean {
        TODO("add logic to check db")
        if (TODO("NOT IN DB")) {
            return false
        }
        return true
    }

    suspend fun apiCheck(stockID: String): Boolean {
        val statsQuote = iexApiService.GetStockQuote(stockID)
        if (statsQuote === returnError) {
            return false
        }
        if (statsQuote === returnNotFound) {
            return false
        }
        firstRunQueryAndSave(stockID)
        return true
    }

    suspend fun firstRunQueryAndSave(stockID: String) {
        val node = mapper.createObjectNode();
        val quote = iexApiService.GetStockQuote(stockID).toString()
        val nodeData = mapper.readTree(quote)
        TODO("save stockdata to db")
        node.set<JsonNode>("stockData", nodeData)
        val statsBasic = iexApiService.GetStockStatsBasic(stockID).toString()
        TODO("save StockStatsBasic to db")
        val insiderTrading = iexApiService.GetStockInsiderTrading(stockID).toString()
        TODO("save StockInsiderTrading to db")
        val previousDividends = iexApiService.GetStockPreviousDividends(stockID).toString()
        TODO("save StockPreviousDividends to db")
        val nextDividends = iexApiService.GetStockNextDividends(stockID).toString()
        TODO("save StockNextDividends to db")
    }

    fun updateDocs(stockID: String) {

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