package stockapp.stocks.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.litote.kmongo.eq
import org.litote.kmongo.push
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.springframework.stereotype.Component
import stockapp.external.clientConnections.*
import stockapp.stocks.model.*


@Component
class StockQueriesService(
    val iexApiService: IEXApiService,
) {
    private val returnError: Flow<String> = flowOf("false")
    private val returnNotFound: Flow<String> = flowOf("notFound")
    val mapper = ObjectMapper()

    suspend fun getStockInformation(stockID: String): Any {
        if (!dbCheck(stockID)) {
            if (!apiCheck(stockID)) {
                return false
            }
            firstRunQueryAndSave(stockID)
        }
        findAndReturn(stockID)
        return "todo"
    }

    suspend fun dbCheck(stockID: String): Boolean {
        stockQuoteCollection.findOne(StockQuote::symbol eq stockID) ?: return false
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
        val currentTime = TODO()
        val node = mapper.createObjectNode()
        val quote = iexApiService.GetStockQuote(stockID).toString()
        val nodeData = mapper.readTree(quote)
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockID, set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime))
        node.set<JsonNode>("stockData", nodeData)

        val statsBasic = iexApiService.GetStockStatsBasic(stockID).toString()
        stockStatsBasicCollection.updateOne(StockStatsBasic::symbol eq stockID, set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime))

        val insiderTrading = iexApiService.GetStockInsiderTrading(stockID).toString()
        stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockID, push(StockInsiderTrading::docs, insiderTrading))
        stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockID, StockInsiderTrading::lastUpdated eq currentTime)

        val previousDividends = iexApiService.GetStockPreviousDividends(stockID).toString()
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockID, push(StockPreviousDividend::docs, previousDividends))
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockID, StockPreviousDividend::lastUpdated eq currentTime)

        val nextDividends = iexApiService.GetStockNextDividends(stockID).toString()
        stockNextDividendCollection.updateOne(StockNextDividend::symbol eq stockID,
            set(StockNextDividend::docs setTo nextDividends, StockNextDividend::lastUpdated setTo currentTime, StockNextDividend::nextUpdate setTo TODO("figure out how to get next dividend date")))

        val largestTrades = iexApiService.GetStockLargestTrades(stockID).toString()
        stockLargestTradesCollection.updateOne(StockLargestTrades::symbol eq stockID, set(StockLargestTrades::docs setTo largestTrades, StockLargestTrades::lastUpdated setTo currentTime))
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