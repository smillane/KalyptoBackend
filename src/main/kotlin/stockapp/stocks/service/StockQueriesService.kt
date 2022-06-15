package stockapp.stocks.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import stockapp.utils.updateAfterMarketClose
import stockapp.utils.updateIntervalCheck
import stockapp.utils.wasYesterday


@Component
class StockQueriesService(
    val iexApiService: IEXApiService,
) {
    private val returnError: Flow<String> = flowOf("false")
    private val returnNotFound: Flow<String> = flowOf("notFound")
    private val mapper = ObjectMapper()
    private val timePeriod: Long = 5L

    suspend fun getAllStockData(stockId: String): Any {
        if (!dbCheck(stockId)) {
            return apiCheck(stockId)
        }
        updateDocs(stockId)
        return getDocsFromDB(stockId)
    }

    // check in DB if there is a quote for stock, if there isn't, there's nothing else in db
    // maybe use a diff model to check, as might have quote for certain stocks displayed/updated, but nothing else
    suspend fun dbCheck(stockId: String): Boolean {
        stockStatsBasicCollection?.findOne(StockQuote::symbol eq stockId) ?: return false
        return true
    }

    // call api for stock quote, the cheapest api call, if stock doesn't exist, won't cost api call
    // if it exists, will run first run query, and will save that return data into db
    suspend fun apiCheck(stockId: String): Any {
        val stockQuote = iexApiService.getStockQuote(stockId)
        if (stockQuote == returnError) {
            return false
        }
        if (stockQuote == returnNotFound) {
            return false
        }
        return firstRunQueryAndSave(stockId, stockQuote as JsonNode)
    }

    suspend fun firstRunQueryAndSave(stockId: String, quote: JsonNode): ReturnStockData {
        val currentTime = Clock.System.now()
        stockQuoteCollection?.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime))

        val statsBasic: JsonNode = mapper.valueToTree(iexApiService.getStockStatsBasic(stockId))
        stockStatsBasicCollection?.updateOne(StockStatsBasic::symbol eq stockId, set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime))

        val insiderTrading: List<JsonNode> = mapper.valueToTree(iexApiService.getLast15StockInsiderTrading(stockId))
        insiderTrading.reversed().forEach { stockInsiderTradingCollection?.updateOne(StockInsiderTrading::symbol eq stockId, push(StockInsiderTrading::docs, it)) }
        stockInsiderTradingCollection?.updateOne(StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime)

        val previousDividends: List<JsonNode> = mapper.valueToTree(iexApiService.getStockPreviousTwoYearsDividends(stockId))
        previousDividends.reversed().forEach { stockPreviousDividendCollection?.updateOne(StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, it)) }
        stockPreviousDividendCollection?.updateOne(StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime)

        val nextDividends: JsonNode = mapper.valueToTree(iexApiService.getStockNextDividends(stockId))
        stockNextDividendCollection?.updateOne(StockNextDividend::symbol eq stockId,
            set(StockNextDividend::docs setTo nextDividends, StockNextDividend::lastUpdated setTo currentTime,
                StockNextDividend::nextUpdate setTo Instant.parse(nextDividends.findValue("exDate").textValue())))

        val largestTrades: JsonNode = mapper.valueToTree(iexApiService.getStockLargestTrades(stockId))
        stockLargestTradesCollection?.updateOne(StockLargestTrades::symbol eq stockId, set(StockLargestTrades::docs setTo largestTrades[0], StockLargestTrades::lastUpdated setTo currentTime))

        return ReturnStockData(quote = quote, stats = statsBasic, insiderTrading = insiderTrading, previousDividends = previousDividends, nextDividend = nextDividends, largestTrades = largestTrades)
    }

    // logic to reduce amount of db/api calls, some api calls only update after market close
    // if stockQuote was updated today, before market close, most up to date
    // if stockQuote was updated today, before market close while market is close, update daily api calls
    // check when nextDividend ex-date is, if is today, add to pastDividends list and query once a day until nextDividend ex-date isn't the same
    //
    suspend fun updateDocs(stockId: String) {
        val currentTime = Clock.System.now()
        val stockQuote = stockQuoteCollection?.findOne(StockQuote::symbol eq stockId)
        if (stockQuote != null) {
            if (updateIntervalCheck(currentTime, stockQuote.lastUpdated, timePeriod, true)) {
                updateQuote(stockId, currentTime)
            }
            if (updateIntervalCheck(currentTime, stockQuote.lastUpdated, timePeriod, false)) {
                updateDocs(stockId, currentTime)
            }
            if (updateAfterMarketClose(currentTime, stockQuote.lastUpdated)) {
                updateAfterHours(stockId, currentTime)
            }
        }
    }

    private suspend fun updateQuote(stockId: String, currentTime: Instant) {
        val stockQuote: JsonNode = mapper.valueToTree(iexApiService.getStockQuote(stockId))
        stockQuoteCollection?.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime))
    }

    private suspend fun updateDocs(stockId: String, currentTime: Instant) {
        val stockQuote: JsonNode = mapper.valueToTree(iexApiService.getStockQuote(stockId))
        stockQuoteCollection?.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime))
        val statsBasic: JsonNode = mapper.valueToTree(iexApiService.getStockStatsBasic(stockId))
        stockStatsBasicCollection?.updateOne(StockStatsBasic::symbol eq stockId, set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime))
        val largestTrades: JsonNode = mapper.valueToTree(iexApiService.getStockLargestTrades(stockId))
        stockLargestTradesCollection?.updateOne(StockLargestTrades::symbol eq stockId, set(StockLargestTrades::docs setTo largestTrades, StockLargestTrades::lastUpdated setTo currentTime))
    }

    private suspend fun updateAfterHours(stockId: String, currentTime: Instant) {
        val nextDividends: StockNextDividend? = stockNextDividendCollection?.findOne(StockNextDividend::symbol eq stockId)
        if (nextDividends != null) {
            updateDividends(stockId, currentTime, nextDividends)
        }
        val insiderTradingFromDB: StockInsiderTrading? =
            stockInsiderTradingCollection?.findOne(StockInsiderTrading::symbol eq stockId)
        if (insiderTradingFromDB != null) {
            if (!isToday(currentTime, insiderTradingFromDB.lastUpdated)) {
                val insiderTradesFromAPI: JsonNode = mapper.valueToTree(iexApiService.getStockInsiderTradingFromLastUpdated(stockId, insiderTradingFromDB.lastUpdated))
                stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId, push(StockInsiderTrading::docs, insiderTradesFromAPI))
                stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime)
            }
        }
    }

    private suspend fun updateDividends(stockId: String, currentTime: Instant, nextDividends: StockNextDividend) {
        if (wasYesterday(currentTime, nextDividends.nextUpdate) and !isToday(currentTime, nextDividends.lastUpdated)) {
            val previousDividend: List<JsonNode> = mapper.valueToTree(iexApiService.getStockPreviousDividend(stockId))
            updatePreviousDividends(stockId, currentTime, previousDividend[0])
            stockNextDividendCollection?.updateOne(StockNextDividend::symbol eq stockId, StockNextDividend::lastUpdated eq currentTime)
        }
        if (!isToday(currentTime, nextDividends.lastUpdated)) {
            val nextDivFromApi: JsonNode = mapper.valueToTree(iexApiService.getStockNextDividends(stockId))
            if (nextDividends.docs != nextDivFromApi) {
                stockNextDividendCollection?.updateOne(
                    StockNextDividend::symbol eq stockId, set(StockNextDividend::docs setTo nextDivFromApi,
                        StockNextDividend::nextUpdate setTo Instant.parse(nextDivFromApi.findValue("exDate").textValue()),
                        StockNextDividend::lastUpdated setTo currentTime))
            }
        }
        else {
            stockNextDividendCollection?.updateOne(StockNextDividend::symbol eq stockId, StockNextDividend::lastUpdated eq currentTime)
        }
    }

    private suspend fun updatePreviousDividends(stockId: String, currentTime: Instant, lastDividend: JsonNode) {
        stockPreviousDividendCollection?.updateOne(StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, lastDividend))
        stockPreviousDividendCollection?.updateOne(StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime)
    }

    suspend fun getDocsFromDB(stockId: String): ReturnStockData {
        val quote: JsonNode = stockQuoteCollection!!.findOne(StockStatsBasic::symbol eq stockId)!!.docs
        val statsBasic: JsonNode = stockStatsBasicCollection!!.findOne(StockStatsBasic::symbol eq stockId)!!.docs
        val previousDividends: List<JsonNode> = stockPreviousDividendCollection?.findOne(StockPreviousDividend::symbol eq stockId)!!.docs
        val nextDividends: JsonNode = stockNextDividendCollection!!.findOne(StockNextDividend::symbol eq stockId)!!.docs
        val largestTrades: JsonNode = stockLargestTradesCollection!!.findOne(StockLargestTrades::symbol eq stockId)!!.docs
        val insiderTrading: List<JsonNode> = stockInsiderTradingCollection!!.findOne(StockInsiderTrading::symbol eq stockId)!!.docs

        return ReturnStockData(quote = quote, stats = statsBasic, insiderTrading = insiderTrading, previousDividends = previousDividends, nextDividend = nextDividends, largestTrades = largestTrades)
    }
}