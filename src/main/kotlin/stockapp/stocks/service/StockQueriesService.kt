package stockapp.stocks.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
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
        if (statsQuote == returnError) {
            return false
        }
        if (statsQuote == returnNotFound) {
            return false
        }
        firstRunQueryAndSave(stockID, statsQuote as JsonNode)
        return true
    }

    suspend fun firstRunQueryAndSave(stockID: String, quote: JsonNode) {
        val current = Clock.System.now()
        val currentTime = Clock.System.now().toString()
        val node = mapper.createObjectNode()
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockID, set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime))
        node.set<JsonNode>("stockQuote", quote)

        val statsBasic: JsonNode = mapper.valueToTree(iexApiService.GetStockStatsBasic(stockID))
        stockStatsBasicCollection.updateOne(StockStatsBasic::symbol eq stockID, set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime))
        node.set<JsonNode>("stockStatsBasic", statsBasic)

        val insiderTrading: JsonNode = mapper.valueToTree(iexApiService.GetLast15StockInsiderTrading(stockID))
        insiderTrading.reversed().forEach { stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockID, push(StockInsiderTrading::docs, it)) }
        stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockID, StockInsiderTrading::lastUpdated eq currentTime)
        node.set<JsonNode>("stockInsiderTrading", insiderTrading)

        val previousDividends: JsonNode = mapper.valueToTree(iexApiService.GetStockPreviousDividends(stockID))
        previousDividends.reversed().forEach { stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockID, push(StockPreviousDividend::docs, it)) }
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockID, StockPreviousDividend::lastUpdated eq currentTime)
        node.set<JsonNode>("stockPreviousDividends", previousDividends)

        val nextDividends: JsonNode = mapper.valueToTree(iexApiService.GetStockNextDividends(stockID))
        stockNextDividendCollection.updateOne(StockNextDividend::symbol eq stockID,
            set(StockNextDividend::docs setTo nextDividends[0], StockNextDividend::lastUpdated setTo currentTime, StockNextDividend::nextUpdate setTo TODO("figure out how to get next dividend date")))
        node.set<JsonNode>("stockNextDividends", nextDividends)

        val largestTrades: JsonNode = mapper.valueToTree(iexApiService.GetStockLargestTrades(stockID))
        stockLargestTradesCollection.updateOne(StockLargestTrades::symbol eq stockID, set(StockLargestTrades::docs setTo largestTrades[0], StockLargestTrades::lastUpdated setTo currentTime))
        node.set<JsonNode>("stockLargestTrades", largestTrades)
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