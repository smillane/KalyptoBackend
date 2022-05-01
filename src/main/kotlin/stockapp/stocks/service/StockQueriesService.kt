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
import stockapp.utils.isToday
import stockapp.utils.updateAfterMarketCloseCheck
import stockapp.utils.updateIntervalCheck


@Component
class StockQueriesService(
    val iexApiService: IEXApiService,
) {
    private val returnError: Flow<String> = flowOf("false")
    private val returnNotFound: Flow<String> = flowOf("notFound")
    private val serverError: String = "Error"
    private val mapper = ObjectMapper()
    private val timePeriod: Long = 5L

    suspend fun getStockInformation(stockId: String): Any {
        if (!dbCheck(stockId)) {
            return apiCheck(stockId)
        }
        updateDocs(stockId)
        return getDocsFromDB(stockId)
    }

    suspend fun dbCheck(stockId: String): Boolean {
        stockQuoteCollection.findOne(StockQuote::symbol eq stockId) ?: return false
        return true
    }

    suspend fun apiCheck(stockId: String): Any {
        val stockQuote = iexApiService.GetStockQuote(stockId)
        if (stockQuote == returnError) {
            return false
        }
        if (stockQuote == returnNotFound) {
            return false
        }
        return firstRunQueryAndSave(stockId, stockQuote as JsonNode)
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

    // logic to reduce amount of db/api calls, some api calls only update after market close
    // if stockQuote was updated today, before market close, most up to date
    // if stockQuote was updated today, before market close while market is close, update daily api calls
    // check when nextDividend ex-date is, if is today, add to pastDividends list and query once a day until nextDividend ex-date isn't the same
    //
    suspend fun updateDocs(stockId: String) {
        val currentTime = Clock.System.now()
        val stockQuote = stockQuoteCollection.findOne(StockQuote::symbol eq stockId)
        if (stockQuote != null) {
            if (updateIntervalCheck(currentTime, stockQuote.lastUpdated, timePeriod, true)) {
                updateQuote(stockId, currentTime)
            }
            if (updateIntervalCheck(currentTime, stockQuote.lastUpdated, timePeriod, false)) {
                updateDocs(stockId, currentTime)
            }
            if (updateAfterMarketCloseCheck(currentTime, stockQuote.lastUpdated)) {
                val nextDividends: StockNextDividend? = stockNextDividendCollection.findOne(StockNextDividend::symbol eq stockId)
                if (nextDividends != null) {
                    updateNextDividend(stockId, currentTime, nextDividends)
                }
            }
        }
        updateList(stockId, currentTime)
    }

    private suspend fun updateQuote(stockId: String, currentTime: Instant) {
        val stockQuote = iexApiService.GetStockQuote(stockId)
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime))
    }

    private suspend fun updateDocs(stockId: String, currentTime: Instant) {
        val stockQuote = iexApiService.GetStockQuote(stockId)
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime))
        val statsBasic: JsonNode = mapper.valueToTree(iexApiService.GetStockStatsBasic(stockId))
        stockStatsBasicCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime))
        val largestTrades: JsonNode = mapper.valueToTree(iexApiService.GetStockLargestTrades(stockId))
        stockLargestTradesCollection.updateOne(StockLargestTrades::symbol eq stockId, set(StockLargestTrades::docs setTo largestTrades[0], StockLargestTrades::lastUpdated setTo currentTime))
    }

    suspend fun updateList(stockId: String, currentTime: Instant, nextDividends: JsonNode) {
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, nextDividends))
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime)
    }

    private suspend fun updateNextDividend(stockId: String, currentTime: Instant, nextDividends: StockNextDividend) {
        if (isToday(currentTime, nextDividends.nextUpdate)) {
            updateList(stockId, currentTime, nextDividends.docs as JsonNode)
        }

    }

    fun updateOnIntervalsAndAdd(stockID: String) {

    }

    fun getDocsFromDB(stockId: String) {

    }

    private fun apiResponseOk() {

    }
}