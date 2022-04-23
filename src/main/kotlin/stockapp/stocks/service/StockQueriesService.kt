package stockapp.stocks.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.*
import org.litote.kmongo.eq
import org.litote.kmongo.push
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.springframework.stereotype.Component

import stockapp.external.clientConnections.*
import stockapp.stocks.model.*
import stockapp.utils.TimeHandlers


@Component
class StockQueriesService(
    val iexApiService: IEXApiService,
    val timeHandlers: TimeHandlers,
) {
    private val returnError: Flow<String> = flowOf("false")
    private val returnNotFound: Flow<String> = flowOf("notFound")
    private val serverError: String = ("Error")
    val mapper = ObjectMapper()

    suspend fun getStockInformation(stockId: String): Any {
        if (!dbCheck(stockId)) {
            return apiCheck(stockId)
        }
        updateDocs(stockId)
        return getDocsFromDB(stockId)
    }

    suspend fun dbCheck(stockID: String): Boolean {
        stockQuoteCollection.findOne(StockQuote::symbol eq stockID) ?: return false
        return true
    }

    suspend fun apiCheck(stockID: String): Any {
        val statsQuote = iexApiService.GetStockQuote(stockID)
        if (statsQuote == returnError) {
            return false
        }
        if (statsQuote == returnNotFound) {
            return false
        }
        return firstRunQueryAndSave(stockID, statsQuote as JsonNode)
    }

    suspend fun firstRunQueryAndSave(stockId: String, quote: JsonNode): JsonNode {
        val currentTime = Clock.System.now()
        val node = mapper.createObjectNode()
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime))
        node.set<JsonNode>("stockQuote", quote)

        val statsBasic: JsonNode = mapper.valueToTree(iexApiService.GetStockStatsBasic(stockId))
        stockStatsBasicCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime))
        node.set<JsonNode>("stockStatsBasic", statsBasic)

        val insiderTrading: JsonNode = mapper.valueToTree(iexApiService.GetLast15StockInsiderTrading(stockId))
        insiderTrading.reversed().forEach { stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId, push(StockInsiderTrading::docs, it)) }
        stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime)
        node.set<JsonNode>("stockInsiderTrading", insiderTrading)

        val previousDividends: JsonNode = mapper.valueToTree(iexApiService.GetStockPreviousDividends(stockId))
        previousDividends.reversed().forEach { stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, it)) }
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime)
        node.set<JsonNode>("stockPreviousDividends", previousDividends)

        val nextDividends: JsonNode = mapper.valueToTree(iexApiService.GetStockNextDividends(stockId))
        stockNextDividendCollection.updateOne(StockNextDividend::symbol eq stockId,
            set(StockNextDividend::docs setTo nextDividends[0], StockNextDividend::lastUpdated setTo currentTime,
                StockNextDividend::nextUpdate setTo Instant.parse(nextDividends.findValue("exDate").textValue())))
        node.set<JsonNode>("stockNextDividends", nextDividends)

        val largestTrades: JsonNode = mapper.valueToTree(iexApiService.GetStockLargestTrades(stockId))
        stockLargestTradesCollection.updateOne(StockLargestTrades::symbol eq stockId, set(StockLargestTrades::docs setTo largestTrades[0], StockLargestTrades::lastUpdated setTo currentTime))
        node.set<JsonNode>("stockLargestTrades", largestTrades)

        return mapper.treeToValue(node)
    }

    suspend fun updateDocs(stockId: String) {
        val currentTime = Clock.System.now()
        updateListInDB(stockId, currentTime)
        updateDocInDB(stockId, currentTime)
    }

    fun updateAndReplace(stockId: String, currentTimeStamp: Instant) {

    }

    fun updateDocInDB(stockId: String, currentTimeStamp: Instant) {

    }

    suspend fun updateListInDB(stockId: String, currentTimeStamp: Instant) {
        val previousDividends: JsonNode = mapper.valueToTree(iexApiService.GetStockPreviousDividends(stockId))
        previousDividends.reversed().forEach { stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, it)) }
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTimeStamp)
    }

    fun updateOnIntervalsAndAdd(stockID: String) {

    }

    fun updateIntervalCheck(currentTime: Instant, lastUpdated: Instant, interval: Long): Boolean {
        val diffInMinutes = lastUpdated.until(currentTime, DateTimeUnit.MINUTE, TimeZone.of("EST"))
        if (diffInMinutes >= interval) {
            return true
        }
        return false
    }

    fun getDocsFromDB(stockId: String) {

    }

    private fun apiResponseOk() {

    }
}