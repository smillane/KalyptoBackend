package kalypto.stocks.service

import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import org.litote.kmongo.*
import org.springframework.stereotype.Component

import kalypto.external.clientConnections.*
import kalypto.stocks.model.*
import kalypto.utils.isToday
import kalypto.utils.updateAfterMarketClose
import kalypto.utils.updateIntervalCheck
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException


@Component
class StockQueriesService(val iexApiService: IEXApiService) {
    private val timePeriod: Long = 1L
    private val upsertTrue = UpdateOptions().upsert(true)
    private val preMarketLosers: String = "preMarketLosers"
    private val preMarketGainers: String = "preMarketGainers"
    private val postMarketLosers: String = "postMarketLosers"
    private val postMarketGainers: String = "postMarketGainers"
    private val mostActive: String = "mostActive"
    private val gainers: String = "gainers"
    private val losers: String = "losers"
    private val volume: String = "volume"
    private val stockSymbol: String = "symbol"
    private val exDate: String = "exDate"

    suspend fun getPreMarketLosers(): List<Map<String, Any>> {
        val preMarketLosersData = iexApiService.getPreMarketLosers().first()
        preMarketLosersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(preMarketLosers, preMarketLosersData)
        return preMarketLosersData
    }

    suspend fun getPreMarketGainers(): List<Map<String, Any>> {
        val preMarketGainersData = iexApiService.getPreMarketGainers().first()
        preMarketGainersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(preMarketGainers, preMarketGainersData)
        return preMarketGainersData
    }

    suspend fun getPostMarketLosers(): List<Map<String, Any>> {
        val postMarketLosersData = iexApiService.getPostMarketLosers().first()
        postMarketLosersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(postMarketLosers, postMarketLosersData)
        return postMarketLosersData
    }

    suspend fun getPostMarketGainers(): List<Map<String, Any>> {
        val postMarketGainersData = iexApiService.getPostMarketGainers().first()
        postMarketGainersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(postMarketGainers, postMarketGainersData)
        return postMarketGainersData
    }

    suspend fun getMostActive(): List<Map<String, Any>> {
        val mostActiveData = iexApiService.getMostActive().first()
        mostActiveData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(mostActive, mostActiveData)
        return mostActiveData
    }

    suspend fun getGainers(): List<Map<String, Any>> {
        val gainersData = iexApiService.getGainers().first()
        gainersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(gainers, gainersData)
        return gainersData
    }

    suspend fun getLosers(): List<Map<String, Any>> {
        val losersData = iexApiService.getLosers().first()
        losersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(losers, losersData)
        return losersData
    }

    suspend fun getVolume(): List<Map<String, Any>> {
        val volumeData = iexApiService.getVolume().first()
        volumeData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(volume, volumeData)
        return volumeData
    }

    suspend fun getAllStockData(stockId: String): Any? {
        if (!dbCheck(stockId, false)) {
            return apiCheck(stockId, false)
        }
        updateDocs(stockId)
        return getDocsFromDB(stockId)
    }

    suspend fun getStockQuote(stockId: String): Any? {
        return getAndUpdateQuote(stockId)
    }

    suspend fun getInsiderTrading(stockId: String): Any? {
        if (!dbCheck(stockId, true)) {
            return apiCheck(stockId, false)
        }
        return stockInsiderTradingCollection.findOne(StockInsiderTrading::symbol eq stockId)!!.docs
    }

    suspend fun getDividends(stockId: String): Any? {
        if (!dbCheck(stockId, true)) {
            return apiCheck(stockId, false)
        }
        return stockPreviousDividendCollection.findOne(StockPreviousDividend::symbol eq stockId)!!.docs
    }

    suspend fun getStockNews(stockId: String): Any {
        if (!dbCheck(stockId, true)) {
            if (basicApiCheck(stockId)) {
                return iexApiService.getStockNews(stockId).first()
            }
        }
        return iexApiService.getStockNews(stockId).first()
    }

    // check in DB if there is a quote for stock, if there isn't, there's nothing else in db
    suspend fun dbCheck(stockId: String, basicQuote: Boolean): Boolean {
        while (basicQuote) {
            val temp = stockQuoteCollection.findOne(StockQuote::symbol eq stockId)
            return temp != null
        }
        val temp = stockStatsBasicCollection.findOne(StockStatsBasic::symbol eq stockId)
        return temp != null
    }

    suspend fun basicApiCheck(stockId: String): Boolean {
        try {
            return iexApiService.getStockQuote(stockId).first().apply {
                stockQuoteCollection.updateOne(
                    StockQuote::symbol eq stockId,
                    set(StockQuote::docs setTo this, StockQuote::lastUpdated setTo Clock.System.now().toString()),
                    upsertTrue
                )
            }.isNotEmpty()
        } catch (e: WebClientResponseException) {
            println(e.statusCode)
            println(e.localizedMessage)
            println(e.statusText)
            return false
//            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.statusText)
        }
    }

    // call api for stock quote, the cheapest api call, if stock doesn't exist, won't cost api call
    // if it exists, will run first run query, and will save that return data into db
    suspend fun apiCheck(stockId: String, basicQuote: Boolean): Any? {
        val stockQuote = iexApiService.getStockQuote(stockId).first()
        if (stockQuote.isEmpty()) return null
        return if (basicQuote) {
            getQuoteAndSave(stockId, stockQuote)
        } else {
            firstRunQueryAndSave(stockId, stockQuote)
        }
    }

    suspend fun getQuoteAndSave(stockId: String, quote: Map<String, Any>): Map<String, Any> {
        return quote.apply {
            stockQuoteCollection.updateOne(
                StockQuote::symbol eq stockId,
                set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo Clock.System.now().toString()),
                upsertTrue
            )
        }
    }

    private suspend fun getAndUpdateQuote(stockId: String): Map<String, Any> {
        return iexApiService.getStockQuote(stockId).first().also {
            stockQuoteCollection.updateOne(
                StockStatsBasic::symbol eq stockId,
                set(StockQuote::docs setTo it, StockQuote::lastUpdated setTo Clock.System.now().toString())
            )
        }
    }

    private suspend fun updateQuote(stockId: String, quote: Map<String, Any>) {
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo Clock.System.now().toString())
        )
    }

    private suspend fun updateDailyList(listId: String, data: List<Map<String, Any>>) {
        dailyListCollection.updateOne(
            DailyLists::listType eq listId,
            set(DailyLists::docs setTo data, DailyLists::lastUpdated setTo Clock.System.now().toString())
        )
    }

    suspend fun firstRunQueryAndSave(stockId: String, quote: Map<String, Any>): Any {
        val currentTime = Clock.System.now().toString()
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime), upsertTrue
        )

        val statsBasic = iexApiService.getStockStatsBasic(stockId).first()
        stockStatsBasicCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime), upsertTrue
        )

        val previousDividends: List<Map<String, Any>> = iexApiService.getStockPreviousTwoYearsDividends(stockId).first()
        previousDividends.reversed().forEach {
            stockPreviousDividendCollection.updateOne(
                StockPreviousDividend::symbol eq stockId,
                push(StockPreviousDividend::docs, it)
            )
        }
        stockPreviousDividendCollection.updateOne(
            StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime, upsertTrue
        )

        val nextDividends: Map<String, Any> = iexApiService.getStockNextDividends(stockId).first()[0]
        stockNextDividendCollection.updateOne(
            StockNextDividend::symbol eq stockId,
            set(
                StockNextDividend::docs setTo nextDividends, StockNextDividend::lastUpdated setTo currentTime,
                StockNextDividend::nextUpdate setTo Instant.parse(nextDividends.getValue(exDate).toString())
                    .toString()
            ), upsertTrue
        )

        val largestTrades: List<Map<String, Any>> = iexApiService.getStockLargestTrades(stockId).first()
        stockLargestTradesCollection.updateOne(
            StockLargestTrades::symbol eq stockId,
            set(StockLargestTrades::docs setTo largestTrades, StockLargestTrades::lastUpdated setTo currentTime),
            upsertTrue
        )

        val insiderTrading: List<Map<String, Any>> = iexApiService.getLast30StockInsiderTrading(stockId).first()
        insiderTrading.reversed().forEach {
            stockInsiderTradingCollection.updateOne(
                StockInsiderTrading::symbol eq stockId,
                push(StockInsiderTrading::docs, it)
            )
        }
        stockInsiderTradingCollection.updateOne(
            StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime, upsertTrue
        )

        val institutionalOwnership: List<Map<String, Any>> =
            iexApiService.getStockInstitutionalOwnership(stockId).first()
        stockInstitutionalOwnershipCollection.updateOne(
            StockInstitutionalOwnership::symbol eq stockId,
            set(
                StockInstitutionalOwnership::docs setTo institutionalOwnership,
                StockInstitutionalOwnership::lastUpdated setTo currentTime
            )
        )

        val insiderSummary: List<Map<String, Any>> = iexApiService.getStockInsiderSummary(stockId).first()
        stockInsiderSummaryCollection.updateOne(
            StockInsiderSummary::symbol eq stockId,
            set(StockInsiderSummary::docs setTo insiderSummary, StockInsiderSummary::lastUpdated setTo currentTime)
        )

        val financials: List<Map<String, Any>> = iexApiService.getStockFinancials(stockId).first()
        stockFinancialsCollection.updateOne(
            StockFinancials::symbol eq stockId,
            push(StockFinancials::docs, financials[0])
        )
        stockFinancialsCollection.updateOne(
            StockFinancials::symbol eq stockId,
            StockFinancials::lastUpdated eq currentTime
        )

        val fundamentalValuations: List<Map<String, Any>> = iexApiService.getStockFundamentalValuations(stockId).first()
        stockFinancialsCollection.updateOne(
            StockFinancials::symbol eq stockId,
            push(StockFinancials::docs, fundamentalValuations[0])
        )
        stockFinancialsCollection.updateOne(
            StockFinancials::symbol eq stockId,
            StockFinancials::lastUpdated eq currentTime
        )

        val fundamentals: List<Map<String, Any>> = iexApiService.getStockFundamentals(stockId).first()
        stockFinancialsCollection.updateOne(
            StockFinancials::symbol eq stockId,
            push(StockFinancials::docs, fundamentals[0])
        )
        stockFinancialsCollection.updateOne(
            StockFinancials::symbol eq stockId,
            StockFinancials::lastUpdated eq currentTime
        )

        val peerGroup: List<Map<String, Any>> = iexApiService.getStockPeerGroup(stockId).first()
        stockPeerGroupCollection.updateOne(
            StockPeerGroup::symbol eq stockId,
            set(StockPeerGroup::docs setTo peerGroup, StockPeerGroup::lastUpdated setTo currentTime)
        )

        val companyInfo: Map<String, Any> = iexApiService.getStockCompanyInfo(stockId).first()[0]
        stockCompanyInfoCollection.updateOne(
            StockCompanyInfo::symbol eq stockId,
            set(StockCompanyInfo::docs setTo companyInfo, StockCompanyInfo::lastUpdated setTo currentTime)
        )

        return ReturnStockData(
            quote = quote,
            basicStats = statsBasic,
            previousDividends = previousDividends,
            nextDividend = nextDividends,
            largestTrades = largestTrades,
            insiderTrading = insiderTrading,
            institutionalOwnership = institutionalOwnership,
            insiderSummary = insiderSummary,
            financials = financials,
            fundamentalValuations = fundamentalValuations,
            fundamentals = fundamentals,
            peerGroup = peerGroup,
            companyInfo = companyInfo
        )
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
            set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime.toString())
        )
    }

    private suspend fun updateDocs(stockId: String, currentTime: Instant) {
        val stockQuote: Map<String, Any> = iexApiService.getStockQuote(stockId).first()
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo stockQuote, StockQuote::lastUpdated setTo currentTime.toString())
        )

        val statsBasic: Map<String, Any> = iexApiService.getStockStatsBasic(stockId).first()
        stockStatsBasicCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime.toString())
        )

        val largestTrades: List<Map<String, Any>> = iexApiService.getStockLargestTrades(stockId).first()
        stockLargestTradesCollection.updateOne(
            StockLargestTrades::symbol eq stockId,
            set(
                StockLargestTrades::docs setTo largestTrades,
                StockLargestTrades::lastUpdated setTo currentTime.toString()
            )
        )
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

        if ((insiderTradingFromDBLastUpdated != null) && !isToday(currentTime, insiderTradingFromDBLastUpdated)) {
            val insiderTradesFromAPI: List<Map<String, Any>> = iexApiService.getStockInsiderTradingFromLastUpdated(
                stockId, insiderTradingFromDBLastUpdated
            ).first()
            insiderTradesFromAPI.reversed().forEach {
                stockInsiderTradingCollection.updateOne(
                    StockInsiderTrading::symbol eq stockId, push(StockInsiderTrading::docs, it)
                )
            }
            stockInsiderTradingCollection.updateOne(
                StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime.toString()
            )
        }
    }

    private suspend fun updateDividends(stockId: String, currentTime: Instant, currNextDividend: StockNextDividend) {
        val newNextDividend: Map<String, Any> = iexApiService.getStockPreviousDividend(stockId).first()[0]
        if (newNextDividend == currNextDividend.docs) {
            updatePreviousDividends(stockId, currentTime, newNextDividend)
            stockNextDividendCollection.updateOne(
                StockNextDividend::symbol eq stockId, set(
                    StockNextDividend::docs setTo newNextDividend,
                    StockNextDividend::nextUpdate setTo Instant.parse(newNextDividend.getValue(exDate).toString())
                        .toString(),
                    StockNextDividend::lastUpdated setTo currentTime.toString()
                )
            )
        }
        if (newNextDividend != currNextDividend.docs) {
            stockNextDividendCollection.updateOne(
                StockNextDividend::symbol eq stockId, set(StockNextDividend::lastUpdated setTo currentTime.toString())
            )
        }
    }

    private suspend fun updatePreviousDividends(stockId: String, currentTime: Instant, lastDividend: Map<String, Any>) {
        stockPreviousDividendCollection.updateOne(
            StockPreviousDividend::symbol eq stockId, push(StockPreviousDividend::docs, lastDividend)
        )
        stockPreviousDividendCollection.updateOne(
            StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime.toString()
        )
    }

    suspend fun getDocsFromDB(stockId: String): ReturnStockData {
        return ReturnStockData(
            quote = stockQuoteCollection.findOne(StockQuote::symbol eq stockId)!!.docs,
            basicStats = stockStatsBasicCollection.findOne(StockStatsBasic::symbol eq stockId)!!.docs,
            previousDividends = stockPreviousDividendCollection.findOne(StockPreviousDividend::symbol eq stockId)!!.docs,
            nextDividend = stockNextDividendCollection.findOne(StockNextDividend::symbol eq stockId)!!.docs,
            largestTrades = stockLargestTradesCollection.findOne(StockLargestTrades::symbol eq stockId)!!.docs,
            insiderTrading = stockInsiderTradingCollection.findOne(StockInsiderTrading::symbol eq stockId)!!.docs,
            institutionalOwnership = stockInstitutionalOwnershipCollection.findOne(StockInstitutionalOwnership::symbol eq stockId)!!.docs,
            insiderSummary = stockInsiderSummaryCollection.findOne(StockInsiderSummary::symbol eq stockId)!!.docs,
            financials = stockFinancialsCollection.findOne(StockFinancials::symbol eq stockId)!!.docs,
            fundamentalValuations = stockFundamentalValuationsCollection.findOne(StockFundamentalValuations::symbol eq stockId)!!.docs,
            fundamentals = stockFundamentalsCollection.findOne(StockFundamentals::symbol eq stockId)!!.docs,
            peerGroup = stockPeerGroupCollection.findOne(StockLargestTrades::symbol eq stockId)!!.docs,
            companyInfo = stockCompanyInfoCollection.findOne(StockCompanyInfo::symbol eq stockId)!!.docs
        )
    }
}