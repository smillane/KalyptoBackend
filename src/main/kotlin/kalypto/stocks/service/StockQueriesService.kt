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
import kotlinx.coroutines.*
import org.springframework.web.reactive.function.client.WebClientResponseException


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

    suspend fun getPreMarketLosers(): List<Map<String, Any>> = coroutineScope {
        val preMarketLosersData = async(start = CoroutineStart.LAZY) { iexApiService.getPreMarketLosers() }.await()
        preMarketLosersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(preMarketLosers, preMarketLosersData)
        return@coroutineScope preMarketLosersData
    }

    suspend fun getPreMarketGainers(): List<Map<String, Any>> = coroutineScope {
        val preMarketGainersData = async(start = CoroutineStart.LAZY) { iexApiService.getPreMarketGainers() }.await()
        preMarketGainersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(preMarketGainers, preMarketGainersData)
        return@coroutineScope preMarketGainersData
    }

    suspend fun getPostMarketLosers(): List<Map<String, Any>> = coroutineScope {
        val postMarketLosersData = async(start = CoroutineStart.LAZY) { iexApiService.getPostMarketLosers() }.await()
        postMarketLosersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(postMarketLosers, postMarketLosersData)
        return@coroutineScope postMarketLosersData
    }

    suspend fun getPostMarketGainers(): List<Map<String, Any>> = coroutineScope {
        val postMarketGainersData = async(start = CoroutineStart.LAZY) { iexApiService.getPostMarketGainers() }.await()
        postMarketGainersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(postMarketGainers, postMarketGainersData)
        return@coroutineScope postMarketGainersData
    }

    suspend fun getMostActive(): List<Map<String, Any>> = coroutineScope {
        val mostActiveData = async(start = CoroutineStart.LAZY) { iexApiService.getMostActive() }.await()
        mostActiveData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(mostActive, mostActiveData)
        return@coroutineScope mostActiveData
    }

    suspend fun getGainers(): List<Map<String, Any>> = coroutineScope {
        val gainersData = async(start = CoroutineStart.LAZY) { iexApiService.getGainers() }.await()
        gainersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(gainers, gainersData)
        return@coroutineScope gainersData
    }

    suspend fun getLosers(): List<Map<String, Any>> = coroutineScope {
        val losersData = async(start = CoroutineStart.LAZY) { iexApiService.getLosers() }.await()
        losersData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(losers, losersData)
        return@coroutineScope losersData
    }

    suspend fun getVolume(): List<Map<String, Any>> = coroutineScope {
        val volumeData = async(start = CoroutineStart.LAZY) { iexApiService.getVolume() }.await()
        volumeData.forEach { updateQuote(stockSymbol, it) }
        updateDailyList(volume, volumeData)
        return@coroutineScope volumeData
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

    suspend fun getStockNews(stockId: String): List<Map<String, Any>> = coroutineScope {
        return@coroutineScope async(start = CoroutineStart.LAZY) { iexApiService.getStockNews(stockId).first() }.await()
    }

    suspend fun getDocsFromDB(stockId: String): ReturnStockData = coroutineScope {
        return@coroutineScope ReturnStockData(
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

    suspend fun basicApiCheck(stockId: String): Boolean {
        return try {
            iexApiService.getStockQuote(stockId).apply {
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
            false
            //            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.statusText)
        }
    }

    // check in DB if there is a quote for stock, if there isn't, there's nothing else in db
    private suspend fun dbCheck(stockId: String, basicQuote: Boolean): Boolean {
        while (basicQuote) {
            val temp = stockQuoteCollection.findOne(StockQuote::symbol eq stockId)
            return temp != null
        }
        val temp = stockStatsBasicCollection.findOne(StockStatsBasic::symbol eq stockId)
        return temp != null
    }

    // call api for stock quote, the cheapest api call, if stock doesn't exist, won't cost api call
    // if it exists, will run first run query, and will save that return data into db
    private suspend fun apiCheck(stockId: String, basicQuote: Boolean): Any? {
        val stockQuote = iexApiService.getStockQuote(stockId)
        if (stockQuote.isEmpty()) return null
        return if (basicQuote) {
            getQuoteAndSave(stockId, stockQuote)
        } else {
            firstRunQueryAndSave(stockId, stockQuote)
        }
    }

    private suspend fun getQuoteAndSave(stockId: String, quote: Map<String, Any>): Map<String, Any> {
        return quote.apply {
            stockQuoteCollection.updateOne(
                StockQuote::symbol eq stockId,
                set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo Clock.System.now().toString()),
                upsertTrue
            )
        }
    }

    private suspend fun getAndUpdateQuote(stockId: String): Map<String, Any> = coroutineScope {
        return@coroutineScope async(start = CoroutineStart.LAZY) { iexApiService.getStockQuote(stockId) }.await().also {
            stockQuoteCollection.updateOne(
                StockStatsBasic::symbol eq stockId,
                set(StockQuote::docs setTo it, StockQuote::lastUpdated setTo Clock.System.now().toString())
            )
        }
    }

    private suspend fun updateQuote(
        stockId: String,
        quote: Map<String, Any>,
        currentTime: String? = Clock.System.now().toString(),
    ) {
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime)
        )
    }

    private suspend fun updateDailyList(listId: String, data: List<Map<String, Any>>) {
        dailyListCollection.updateOne(
            DailyLists::listType eq listId,
            set(DailyLists::docs setTo data, DailyLists::lastUpdated setTo Clock.System.now().toString())
        )
    }

    private suspend fun firstRunQueryAndSave(stockId: String, quote: Map<String, Any>): ReturnStockData =
        coroutineScope {
            val currentTime = Clock.System.now().toString()
            updateQuote(stockId, quote, currentTime)

            val statsBasic =
                async(start = CoroutineStart.LAZY) { iexApiService.getStockStatsBasic(stockId) }.await().also {
                    stockStatsBasicCollection.updateOne(
                        StockStatsBasic::symbol eq stockId,
                        set(StockStatsBasic::docs setTo it, StockStatsBasic::lastUpdated setTo currentTime), upsertTrue
                    )
                }

            val previousDividends: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockPreviousTwoYearsDividends(stockId).first()
            }.await().also {
                it.reversed().forEach { eachDividend ->
                    stockPreviousDividendCollection.updateOne(
                        StockPreviousDividend::symbol eq stockId,
                        push(StockPreviousDividend::docs, eachDividend)
                    )
                }
            }
            stockPreviousDividendCollection.updateOne(
                StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime, upsertTrue
            )

            val nextDividends: Map<String, Any> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockNextDividends(
                    stockId
                )[0]
            }.await().also {
                stockNextDividendCollection.updateOne(
                    StockNextDividend::symbol eq stockId,
                    set(
                        StockNextDividend::docs setTo it, StockNextDividend::lastUpdated setTo currentTime,
                        StockNextDividend::nextUpdate setTo Instant.parse(it.getValue(exDate).toString())
                            .toString()
                    ), upsertTrue
                )
            }


            val largestTrades: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockLargestTrades(
                    stockId
                )
            }.await().also {
                stockLargestTradesCollection.updateOne(
                    StockLargestTrades::symbol eq stockId,
                    set(StockLargestTrades::docs setTo it, StockLargestTrades::lastUpdated setTo currentTime),
                    upsertTrue
                )
            }

            val insiderTrading: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getLast30StockInsiderTrading(
                    stockId
                ).first()
            }.await().also {
                it.reversed().forEach { eachTransaction ->
                    stockInsiderTradingCollection.updateOne(
                        StockInsiderTrading::symbol eq stockId,
                        push(StockInsiderTrading::docs, eachTransaction)
                    )
                }
            }
            stockInsiderTradingCollection.updateOne(
                StockInsiderTrading::symbol eq stockId, StockInsiderTrading::lastUpdated eq currentTime, upsertTrue
            )

            val institutionalOwnership: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockInstitutionalOwnership(
                    stockId
                )
            }.await().also {
                stockInstitutionalOwnershipCollection.updateOne(
                    StockInstitutionalOwnership::symbol eq stockId,
                    set(
                        StockInstitutionalOwnership::docs setTo it,
                        StockInstitutionalOwnership::lastUpdated setTo currentTime
                    )
                )
            }

            val insiderSummary: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockInsiderSummary(
                    stockId
                )
            }.await().also {
                stockInsiderSummaryCollection.updateOne(
                    StockInsiderSummary::symbol eq stockId,
                    set(StockInsiderSummary::docs setTo it, StockInsiderSummary::lastUpdated setTo currentTime)
                )
            }

            val financials: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockFinancials(
                    stockId
                )
            }.await().also {
                stockFinancialsCollection.updateOne(
                    StockFinancials::symbol eq stockId,
                    push(StockFinancials::docs, it[0])
                )
            }
            stockFinancialsCollection.updateOne(
                StockFinancials::symbol eq stockId,
                StockFinancials::lastUpdated eq currentTime
            )

            val fundamentalValuations: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockFundamentalValuations(
                    stockId
                )
            }.await().also {
                stockFundamentalValuationsCollection.updateOne(
                    StockFinancials::symbol eq stockId,
                    push(StockFinancials::docs, it[0])
                )
            }
            stockFundamentalValuationsCollection.updateOne(
                StockFinancials::symbol eq stockId,
                StockFinancials::lastUpdated eq currentTime
            )

            val fundamentals: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockFundamentals(
                    stockId
                )
            }.await().also {
                stockFundamentalsCollection.updateOne(
                    StockFinancials::symbol eq stockId,
                    push(StockFinancials::docs, it[0])
                )
            }
            stockFundamentalsCollection.updateOne(
                StockFinancials::symbol eq stockId,
                StockFinancials::lastUpdated eq currentTime
            )

            val peerGroup: List<Map<String, Any>> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockPeerGroup(
                    stockId
                )
            }.await().also {
                stockPeerGroupCollection.updateOne(
                    StockPeerGroup::symbol eq stockId,
                    set(StockPeerGroup::docs setTo it, StockPeerGroup::lastUpdated setTo currentTime)
                )
            }

            val companyInfo: Map<String, Any> = async(start = CoroutineStart.LAZY) {
                iexApiService.getStockCompanyInfo(
                    stockId
                )[0]
            }.await().also {
                stockCompanyInfoCollection.updateOne(
                    StockCompanyInfo::symbol eq stockId,
                    set(StockCompanyInfo::docs setTo it, StockCompanyInfo::lastUpdated setTo currentTime)
                )
            }

            return@coroutineScope ReturnStockData(
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


    // look at when some api calls update, some only update at 6pm, 6am, 11pm, etc.
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

    private suspend fun updateExtended(stockId: String, currentTime: Instant) = coroutineScope {
        val quote = async(start = CoroutineStart.LAZY) { iexApiService.getStockQuote(stockId) }.await()
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime.toString())
        )
    }

    private suspend fun updateDocs(stockId: String, currentTime: Instant) = coroutineScope {
        val quote = async(start = CoroutineStart.LAZY) { iexApiService.getStockQuote(stockId) }.await()
        stockQuoteCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockQuote::docs setTo quote, StockQuote::lastUpdated setTo currentTime.toString())
        )


        val statsBasic = async(start = CoroutineStart.LAZY) { iexApiService.getStockStatsBasic(stockId) }.await()
        stockStatsBasicCollection.updateOne(
            StockStatsBasic::symbol eq stockId,
            set(StockStatsBasic::docs setTo statsBasic, StockStatsBasic::lastUpdated setTo currentTime.toString())
        )

        val largestTrades = async(start = CoroutineStart.LAZY) { iexApiService.getStockLargestTrades(stockId) }.await()
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
        val newNextDividend: Map<String, Any> = iexApiService.getStockPreviousDividend(stockId)[0]
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
            StockPreviousDividend::symbol eq stockId,
            push(StockPreviousDividend::docs, lastDividend)
        )
        stockPreviousDividendCollection.updateOne(
            StockPreviousDividend::symbol eq stockId, StockPreviousDividend::lastUpdated eq currentTime.toString()
        )
    }
}