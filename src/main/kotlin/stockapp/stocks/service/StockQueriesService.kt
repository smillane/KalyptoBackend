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
    private val doesNotExist: String = "Does Not Exist"
    val mapper = ObjectMapper()

    suspend fun getStockInformation(stockID: String) {
        if (!dbCheck(stockID)) {
            return
        }
    }

    suspend fun dbCheck(stockID: String): String {
        TODO("add logic to check db")
        if (apiCheck(stockID) === returnError) {
            return "false"
        }
        if (apiCheck(stockID) === returnNotFound) {
            return doesNotExist
        }
        firstRunQueryAndSave(stockID)
    }

    fun apiCheck(stockID: String): Any {
        return iexApiService.GetStockQuote(stockID)
    }

    suspend fun firstRunQueryAndSave(stockID: String) {
        val node = mapper.createObjectNode();
        val quote = iexApiService.GetStockQuote(stockID).toString()
        val nodeData = mapper.readTree(quote)
        TODO("save stockdata to db")
        node.set<JsonNode>("stockData", nodeData)
        val statsBasic = iexApiService.GetStockStatsBasic(stockID)
        TODO("save StockStatsBasic to db")
        val insiderTrading = iexApiService.GetStockInsiderTrading(stockID)
        TODO("save StockInsiderTrading to db")
        val previousDividends = iexApiService.GetStockPreviousDividends(stockID)
        TODO("save StockPreviousDividends to db")
        val nextDividends = iexApiService.GetStockNextDividends(stockID)
        TODO("save StockNextDividends to db")
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