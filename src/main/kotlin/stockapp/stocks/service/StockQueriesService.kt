package stockapp.stocks.service

import kotlinx.coroutines.flow.*
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
    private val timePeriod: Long = 5L

    suspend fun getAllStockData(stockId: String): Any {
        if (!dbCheck(stockId)) {
            return apiCheck(stockId, false)
        }
        updateDocs(stockId)
        return getDocsFromDB(stockId)
    }

    suspend fun getStockQuote(stockId: String): Any {
        if (!dbCheck(stockId)) {
            return apiCheck(stockId, true)
        }
        return updateQuote(stockId, Clock.System.now())
    }

    // check in DB if there is a quote for stock, if there isn't, there's nothing else in db
    suspend fun dbCheck(stockId: String): Boolean {
        stockStatsBasicCollection.findOne(StockQuote::symbol eq stockId) ?: return false
        return true
    }

    // call api for stock quote, the cheapest api call, if stock doesn't exist, won't cost api call
    // if it exists, will run first run query, and will save that return data into db
    suspend fun apiCheck(stockId: String, basicQuote: Boolean): Any {
        val stockQuote = iexApiService.getStockQuote(stockId).first()
        return if (basicQuote) {
            getQuoteAndSave(stockId, stockQuote)
        } else {
            firstRunQueryAndSave(stockId, stockQuote)
        }
    }

    suspend fun getQuoteAndSave(stockId: String, quote: Map<String, Any>) {
        val currentTime = Clock.System.now()
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime))
    }

    private suspend fun updateQuote(stockId: String, currentTime: Instant): Map<String, Any> {
        val stockQuote: Map<String, Any> = iexApiService.getStockQuote(stockId).first()
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime))
        return stockQuote
    }

    suspend fun firstRunQueryAndSave(stockId: String, quote: Map<String, Any>): ReturnStockData {
        val currentTime = Clock.System.now()
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime))

        val statsBasic = iexApiService.getStockStatsBasic(stockId).first()
        stockStatsBasicCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime))

        val insiderTrading: List<Map<String, Any>> = iexApiService.getLast15StockInsiderTrading(stockId).first()
        insiderTrading.reversed().forEach { stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId, push(StockInsiderTrading::docs, it)) }
        stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime)

        val previousDividends: List<Map<String, Any>> = iexApiService.getStockPreviousTwoYearsDividends(stockId).first()
        previousDividends.reversed().forEach { stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, it)) }
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime)

        val nextDividends: Map<String, Any> = iexApiService.getStockNextDividends(stockId).first()[0]
        stockNextDividendCollection.updateOne(StockNextDividend::symbol eq stockId,
                set(StockNextDividend::docs setTo nextDividends, StockNextDividend::lastUpdated setTo currentTime,
                        StockNextDividend::nextUpdate setTo Instant.parse(nextDividends.getValue("exDate").toString())))

        val largestTrades: List<Map<String, Any>> = iexApiService.getStockLargestTrades(stockId).first()
        stockLargestTradesCollection.updateOne(StockLargestTrades::symbol eq stockId, set(StockLargestTrades::docs setTo largestTrades, StockLargestTrades::lastUpdated setTo currentTime))

        return ReturnStockData(quote = quote, stats = statsBasic, insiderTrading = insiderTrading, previousDividends = previousDividends, nextDividend = nextDividends, largestTrades = largestTrades)
    }

    // logic to reduce amount of db/api calls, some api calls only update after market close
    // if stockQuote was updated today, before market close, most up to date
    // if stockQuote was updated today, before market close while market is close, update daily api calls
    // check when nextDividend ex-date is, if is today, add to pastDividends list and query once a day until nextDividend ex-date isn't the same
    //
    suspend fun updateDocs(stockId: String) {
        val currentTime = Clock.System.now()
        val lastUpdated = stockStatsBasicCollection.findOne(StockStatsBasic::symbol eq stockId)!!.lastUpdated
        if (updateIntervalCheck(currentTime, lastUpdated, timePeriod, true)) {
                updateExtended(stockId, currentTime)
        }
        if (updateIntervalCheck(currentTime, lastUpdated, timePeriod, false)) {
            updateDocs(stockId, currentTime)
        }
        if (updateAfterMarketClose(currentTime, lastUpdated)) {
            updateAfterHours(stockId, currentTime)
        }
    }

    private suspend fun updateExtended(stockId: String, currentTime: Instant) {
        val stockQuote: Map<String, Any> = iexApiService.getStockQuote(stockId).first()
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime))
    }

    private suspend fun updateDocs(stockId: String, currentTime: Instant) {
        val stockQuote: Map<String, Any> = iexApiService.getStockQuote(stockId).first()
        stockQuoteCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime))
        val statsBasic: Map<String, Any> = iexApiService.getStockStatsBasic(stockId).first()
        stockStatsBasicCollection.updateOne(StockStatsBasic::symbol eq stockId, set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime))
        val largestTrades: List<Map<String, Any>> = iexApiService.getStockLargestTrades(stockId).first()
        stockLargestTradesCollection.updateOne(StockLargestTrades::symbol eq stockId, set(StockLargestTrades::docs setTo largestTrades, StockLargestTrades::lastUpdated setTo currentTime))
    }

    private suspend fun updateAfterHours(stockId: String, currentTime: Instant) {
        val nextDividends: StockNextDividend? = stockNextDividendCollection.findOne(StockNextDividend::symbol eq stockId)
        if (nextDividends != null) {
            updateDividends(stockId, currentTime, nextDividends)
        }
        val insiderTradingFromDB: StockInsiderTrading? =
                stockInsiderTradingCollection.findOne(StockInsiderTrading::symbol eq stockId)
        if (insiderTradingFromDB != null) {
            if (!isToday(currentTime, insiderTradingFromDB.lastUpdated)) {
                val insiderTradesFromAPI: List<Map<String, Any>> = iexApiService.getStockInsiderTradingFromLastUpdated(stockId, insiderTradingFromDB.lastUpdated).first()
                insiderTradesFromAPI.reversed().forEach { stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId, push(StockInsiderTrading::docs, it)) }
                stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime)
            }
        }
    }

    private suspend fun updateDividends(stockId: String, currentTime: Instant, nextDividends: StockNextDividend) {
        if (wasYesterday(currentTime, nextDividends.nextUpdate) and !isToday(currentTime, nextDividends.lastUpdated)) {
            val previousDividend: List<Map<String, Any>> = iexApiService.getStockPreviousDividend(stockId).first()
            updatePreviousDividends(stockId, currentTime, previousDividend[0])
            stockNextDividendCollection.updateOne(StockNextDividend::symbol eq stockId, StockNextDividend::lastUpdated eq currentTime)
        }
        if (!isToday(currentTime, nextDividends.lastUpdated)) {
            val nextDivFromApi: Map<String, Any> = iexApiService.getStockNextDividends(stockId).first()[0]
            if (nextDividends.docs != nextDivFromApi) {
                stockNextDividendCollection.updateOne(
                        StockNextDividend::symbol eq stockId, set(StockNextDividend::docs setTo nextDivFromApi,
                        StockNextDividend::nextUpdate setTo Instant.parse(nextDivFromApi.getValue("exDate").toString()),
                        StockNextDividend::lastUpdated setTo currentTime))
            }
        } else {
            stockNextDividendCollection.updateOne(StockNextDividend::symbol eq stockId, StockNextDividend::lastUpdated eq currentTime)
        }
    }

    private suspend fun updatePreviousDividends(stockId: String, currentTime: Instant, lastDividend: Map<String, Any>) {
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, lastDividend))
        stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime)
    }

    suspend fun getDocsFromDB(stockId: String): ReturnStockData {
        val quote: Map<String, Any> = stockQuoteCollection.findOne(StockStatsBasic::symbol eq stockId)!!.docs
        val statsBasic: Map<String, Any> = stockStatsBasicCollection.findOne(StockStatsBasic::symbol eq stockId)!!.docs
        val previousDividends: List<Map<String, Any>> = stockPreviousDividendCollection.findOne(StockPreviousDividend::symbol eq stockId)!!.docs
        val nextDividends: Map<String, Any> = stockNextDividendCollection.findOne(StockNextDividend::symbol eq stockId)!!.docs
        val largestTrades: List<Map<String, Any>> = stockLargestTradesCollection.findOne(StockLargestTrades::symbol eq stockId)!!.docs
        val insiderTrading: List<Map<String, Any>> = stockInsiderTradingCollection.findOne(StockInsiderTrading::symbol eq stockId)!!.docs

        return ReturnStockData(quote = quote, stats = statsBasic, insiderTrading = insiderTrading, previousDividends = previousDividends, nextDividend = nextDividends, largestTrades = largestTrades)
    }
}