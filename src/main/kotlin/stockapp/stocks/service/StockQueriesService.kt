package stockapp.stocks.service

import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import org.litote.kmongo.*
import org.springframework.stereotype.Component

import stockapp.external.clientConnections.*
import stockapp.stocks.model.*
import stockapp.utils.isToday
import stockapp.utils.updateAfterMarketClose
import stockapp.utils.updateIntervalCheck


@Component
class StockQueriesService(val iexApiService: IEXApiService) {
    private val timePeriod: Long = 1L
    private val upsertTrue = UpdateOptions().upsert(true)

    suspend fun getAllStockData(stockId: String): Any {
        if (!dbCheck(stockId, false)) {
            return apiCheck(stockId, false)
        }
        updateDocs(stockId)
        return getDocsFromDB(stockId)
    }

    suspend fun getStockQuote(stockId: String): Any {
        if (!dbCheck(stockId, true)) {
            return apiCheck(stockId, true)
        }
        return updateQuote(stockId)
    }

    suspend fun getInsiderTrading(stockId: String): Any {
        if (!dbCheck(stockId, true)) {
            return apiCheck(stockId, false)
        }
        return stockInsiderTradingCollection.findOne(StockInsiderTrading::symbol eq stockId)!!.docs
    }

    suspend fun getDividends(stockId: String): Any {
        if (!dbCheck(stockId, true)) {
            return apiCheck(stockId, false)
        }
        return stockPreviousDividendCollection.findOne(StockPreviousDividend::symbol eq stockId)!!.docs
    }

    suspend fun getNews(stockId: String): Any {
        if (!dbCheck(stockId, true)) {
            basicApiCheck(stockId)
        }
        return iexApiService.getStockNews(stockId).first()
    }

    // check in DB if there is a quote for stock, if there isn't, there's nothing else in db
    suspend fun dbCheck(stockId: String, basicQuote: Boolean): Boolean {
        while (basicQuote) {
            val temp = stockQuoteCollection.findOne(StockStatsBasic::symbol eq stockId)
            return temp != null
        }
        val temp = stockStatsBasicCollection.findOne(StockStatsBasic::symbol eq stockId)
        return temp != null
    }

    suspend fun basicApiCheck(stockId: String): Boolean {
        val stockQuote = iexApiService.getStockQuote(stockId).first()
        return !stockQuote.isNullOrEmpty()
    }

    // call api for stock quote, the cheapest api call, if stock doesn't exist, won't cost api call
    // if it exists, will run first run query, and will save that return data into db
    suspend fun apiCheck(stockId: String, basicQuote: Boolean): Any {
        val stockQuote = iexApiService.getStockQuote(stockId).first()
        if (stockQuote.isNullOrEmpty()) return false
        return if (basicQuote) {
            getQuoteAndSave(stockId, stockQuote)
        } else {
            firstRunQueryAndSave(stockId, stockQuote)
        }
    }

    suspend fun getQuoteAndSave(stockId: String, quote: Map<String, Any>): Map<String, Any> {
        return quote.apply {
            stockQuoteCollection.updateOne(
                StockStatsBasic::symbol eq stockId,
                set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo Clock.System.now().toString()),
                upsertTrue)
        }
    }

    private suspend fun updateQuote(stockId: String): Map<String, Any> {
        return iexApiService.getStockQuote(stockId).first().also {
            stockQuoteCollection.updateOne(
                StockStatsBasic::symbol eq stockId,
                set(StockQuote::docs setTo it, StockQuote::lastUpdated setTo Clock.System.now().toString()))
        }
    }

    suspend fun firstRunQueryAndSave(stockId: String, quote: Map<String, Any>): ReturnStockData {
        val currentTime = Clock.System.now().toString()
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime), upsertTrue)

        val statsBasic = iexApiService.getStockStatsBasic(stockId).first()
        stockStatsBasicCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime), upsertTrue)

        val insiderTrading: List<Map<String, Any>> = iexApiService.getLast15StockInsiderTrading(stockId).first()
        insiderTrading.reversed().forEach {
            stockInsiderTradingCollection.updateOne(StockInsiderTrading::symbol eq stockId,
                push(StockInsiderTrading::docs, it))
        }
        stockInsiderTradingCollection.updateOne(
            StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime, upsertTrue)

        val previousDividends: List<Map<String, Any>> = iexApiService.getStockPreviousTwoYearsDividends(stockId).first()
        previousDividends.reversed().forEach {
            stockPreviousDividendCollection.updateOne(StockPreviousDividend::symbol eq stockId,
                push(StockPreviousDividend::docs, it))
        }
        stockPreviousDividendCollection.updateOne(
            StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime, upsertTrue)

        val nextDividends: Map<String, Any> = iexApiService.getStockNextDividends(stockId).first()[0]
        stockNextDividendCollection.updateOne(
            StockNextDividend::symbol eq stockId,
            set(StockNextDividend::docs setTo nextDividends, StockNextDividend::lastUpdated setTo currentTime,
                StockNextDividend::nextUpdate setTo Instant.parse(nextDividends.getValue("exDate").toString())
                    .toString()), upsertTrue)

        val largestTrades: List<Map<String, Any>> = iexApiService.getStockLargestTrades(stockId).first()
        stockLargestTradesCollection.updateOne(
            StockLargestTrades::symbol eq stockId,
            set(StockLargestTrades::docs setTo largestTrades, StockLargestTrades::lastUpdated setTo currentTime),
            upsertTrue)

        val financials: List<Map<String, Any>> = iexApiService.getStockFinancials(stockId).first()
        stockFinancials.updateOne(StockFinancials::symbol eq stockId, push(StockFinancials::docs, financials[0]))
        stockFinancials.updateOne(StockFinancials::symbol eq stockId, StockFinancials::lastUpdated eq currentTime)

        val insiderSummary: List<Map<String, Any>> = iexApiService.getStockInsiderSummary(stockId).first()
        stockInsiderSummary.updateOne(StockInsiderSummary::symbol eq stockId,
            set(StockInsiderSummary::docs setTo insiderSummary, StockInsiderSummary::lastUpdated setTo currentTime))

        val institutionalOwnership: List<Map<String, Any>> =
            iexApiService.getStockInstitutionalOwnership(stockId).first()
        stockInstitutionalOwnership.updateOne(StockInstitutionalOwnership::symbol eq stockId,
            set(StockInstitutionalOwnership::docs setTo institutionalOwnership,
                StockInstitutionalOwnership::lastUpdated setTo currentTime))

        val peerGroup: List<Map<String, Any>> = iexApiService.getStockPeerGroup(stockId).first()
        stockPeerGroup.updateOne(StockPeerGroup::symbol eq stockId,
            set(StockPeerGroup::docs setTo peerGroup, StockPeerGroup::lastUpdated setTo currentTime))

        val companyInfo: List<Map<String, Any>> = iexApiService.getStockCompanyInfo(stockId).first()
        stockCompanyInfo.updateOne(StockCompanyInfo::symbol eq stockId,
            set(StockCompanyInfo::docs setTo companyInfo, StockCompanyInfo::lastUpdated setTo currentTime))

        return ReturnStockData(
            quote = quote, stats = statsBasic, insiderTrading = insiderTrading,
            previousDividends = previousDividends, nextDividend = nextDividends, largestTrades = largestTrades,
            financials = financials, insiderSummary = insiderSummary, institutionalOwnership = institutionalOwnership,
            peerGroup = peerGroup, companyInfo = companyInfo)
    }

    // logic to reduce amount of db/api calls, some api calls only update after market close
    // if stockQuote was updated today, before market close, most up to date
    // if stockQuote was updated today, before market close while market is close, update daily api calls
    // check when nextDividend ex-date is, if is today, add to pastDividends list and query once a day until nextDividend ex-date isn't the same


    // look at when some api calls update, some only update at 6pm, 6am, 11pm, etc
    // some only update on fridays (such as oil?)


    suspend fun updateDocs(stockId: String) {
        val currentTime = Clock.System.now()
        val lastUpdated =
            stockStatsBasicCollection.findOne(StockStatsBasic::symbol eq stockId)!!.lastUpdated.toInstant()
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
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime.toString()))
    }

    private suspend fun updateDocs(stockId: String, currentTime: Instant) {
        val stockQuote: Map<String, Any> = iexApiService.getStockQuote(stockId).first()
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime.toString()))

        val statsBasic: Map<String, Any> = iexApiService.getStockStatsBasic(stockId).first()
        stockStatsBasicCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime.toString()))

        val largestTrades: List<Map<String, Any>> = iexApiService.getStockLargestTrades(stockId).first()
        stockLargestTradesCollection.updateOne(
            StockLargestTrades::symbol eq stockId,
            set(StockLargestTrades::docs setTo largestTrades,
                StockLargestTrades::lastUpdated setTo currentTime.toString()))
    }


    // check api docs to see if I can use queries such as insider-trades from last date such as now with new apis
    // also check the same for institutional trades
    private suspend fun updateAfterHours(stockId: String, currentTime: Instant) {
        val nextDividends: StockNextDividend? =
            stockNextDividendCollection.findOne(StockNextDividend::symbol eq stockId)
        if (nextDividends != null) {
            updateDividends(stockId, currentTime, nextDividends)
        }
        val insiderTradingFromDBLastUpdated: Instant? =
            stockInsiderTradingCollection.findOne(StockInsiderTrading::symbol eq stockId)?.lastUpdated?.toInstant()
        if (insiderTradingFromDBLastUpdated != null) {
            if (!isToday(currentTime, insiderTradingFromDBLastUpdated)) {
                val insiderTradesFromAPI: List<Map<String, Any>> = iexApiService.getStockInsiderTradingFromLastUpdated(
                    stockId, insiderTradingFromDBLastUpdated).first()

                insiderTradesFromAPI.reversed().forEach {
                    stockInsiderTradingCollection.updateOne(
                        StockInsiderTrading::symbol eq stockId, push(StockInsiderTrading::docs, it))
                }
                stockInsiderTradingCollection.updateOne(
                    StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime.toString())
            }
        }
    }

    private suspend fun updateDividends(stockId: String, currentTime: Instant, currNextDividend: StockNextDividend) {
        val newNextDividend: Map<String, Any> = iexApiService.getStockPreviousDividend(stockId).first()[0]
        if (newNextDividend == currNextDividend.docs) {
            updatePreviousDividends(stockId, currentTime, newNextDividend)
            stockNextDividendCollection.updateOne(
                StockNextDividend::symbol eq stockId, set(StockNextDividend::docs setTo newNextDividend,
                    StockNextDividend::nextUpdate setTo Instant.parse(newNextDividend.getValue("exDate").toString())
                        .toString(),
                    StockNextDividend::lastUpdated setTo currentTime.toString()))
        }
        if (newNextDividend != currNextDividend.docs) {
            stockNextDividendCollection.updateOne(
                StockNextDividend::symbol eq stockId, set(StockNextDividend::lastUpdated setTo currentTime.toString()))
        }
    }

    private suspend fun updatePreviousDividends(stockId: String, currentTime: Instant, lastDividend: Map<String, Any>) {
        stockPreviousDividendCollection.updateOne(
            StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, lastDividend))
        stockPreviousDividendCollection.updateOne(
            StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime.toString())
    }

    suspend fun getDocsFromDB(stockId: String): ReturnStockData {
        return ReturnStockData(
            quote = stockQuoteCollection.findOne(StockStatsBasic::symbol eq stockId)!!.docs,
            stats = stockStatsBasicCollection.findOne(StockStatsBasic::symbol eq stockId)!!.docs,
            insiderTrading = stockInsiderTradingCollection.findOne(StockInsiderTrading::symbol eq stockId)!!.docs,
            previousDividends = stockPreviousDividendCollection.findOne(StockPreviousDividend::symbol eq stockId)!!.docs,
            nextDividend = stockNextDividendCollection.findOne(StockNextDividend::symbol eq stockId)!!.docs,
            largestTrades = stockLargestTradesCollection.findOne(StockLargestTrades::symbol eq stockId)!!.docs,
            financials = stockFinancials.findOne(StockLargestTrades::symbol eq stockId)!!.docs,
            insiderSummary = stockInsiderSummary.findOne(StockLargestTrades::symbol eq stockId)!!.docs,
            institutionalOwnership = stockInstitutionalOwnership.findOne(StockLargestTrades::symbol eq stockId)!!.docs,
            peerGroup = stockPeerGroup.findOne(StockLargestTrades::symbol eq stockId)!!.docs,
            companyInfo = stockCompanyInfo.findOne(StockLargestTrades::symbol eq stockId)!!.docs)
    }
}