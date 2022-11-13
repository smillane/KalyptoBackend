package kalypto.stocks.service

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.*

@Service
class IEXApiService {
    private val iexToken: String = System.getenv("IEX_PUBLIC_TOKEN")
    private val iexBase: String = "https://sandbox.iexapis.com/stable/"
    private val iexCore: String = "https://cloud.iexapis.com/v1/"
    private val iexBaseTimeSeries: String = "https://sandbox.iexapis.com/stable/time-series/"

    suspend fun getStockQuote(symbol: String): Map<String, Any> = WebClient
        .create(iexCore)
        .get()
        .uri("data/CORE/QUOTE/$symbol?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockStats(symbol: String): Map<String, Any> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/stats?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockStatsBasic(symbol: String): Map<String, Any> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/stats?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getLast30StockInsiderTrading(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("insider_transactions/$symbol?last=30&token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockInsiderTradingFromLastUpdated(symbol: String, lastUpdate: Instant?): Flow<List<Map<String, Any>>> =
        WebClient
            .create(iexBaseTimeSeries)
            .get()
            .uri("insider_transactions/$symbol/?from=$lastUpdate&token=$iexToken")
            .retrieve()
            .awaitBody()

    suspend fun getStockPeerGroup(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/peers?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockLargestTrades(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/largest-trades?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockPreviousTwoYearsDividends(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("data/CORE/DIVIDENDS/$symbol?range=2y?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockPreviousDividend(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("data/CORE/DIVIDENDS/$symbol?last=1?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockNextDividends(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("data/CORE/DIVIDENDS/$symbol?next=1?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockNews(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/NEWS/$symbol?last=10&token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockFinancials(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/FINANCIALS/$symbol?last=1&token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockFundamentalValuations(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/FUNDAMENTALVALUATIONS/$symbol?last=1&token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockFundamentals(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/FUNDAMENTALS/$symbol?last=1&token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockInsiderSummary(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/INSIDERSUMMARY/$symbol?last=1&token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockInstitutionalOwnership(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/INSTITUTIONALOWNERSHIP/$symbol?last=1&token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getStockCompanyInfo(symbol: String): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/COMPANY/$symbol?last=1&token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getMostActive(): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/mostactive?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getGainers(): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/gainers?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getLosers(): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/losers?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getVolume(): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/iexvolume?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getPreMarketLosers(): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/premarket_losers?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getPreMarketGainers(): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/premarket_gainers?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getPostMarketLosers(): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/postmarket_losers?token=$iexToken")
        .retrieve()
        .awaitBody()

    suspend fun getPostMarketGainers(): List<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/postmarket_gainers?token=$iexToken")
        .retrieve()
        .awaitBody()
}