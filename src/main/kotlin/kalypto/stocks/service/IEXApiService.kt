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

    fun getStockQuote(symbol: String): Flow<Map<String, Any>> = WebClient
        .create(iexCore)
        .get()
        .uri("data/CORE/QUOTE/$symbol?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockStats(symbol: String): Flow<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/stats?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockStatsBasic(symbol: String): Flow<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/stats?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getLast30StockInsiderTrading(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("insider_transactions/$symbol?last=30&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockInsiderTradingFromLastUpdated(symbol: String, lastUpdate: Instant?): Flow<List<Map<String, Any>>> =
        WebClient
            .create(iexBaseTimeSeries)
            .get()
            .uri("insider_transactions/$symbol/?from=$lastUpdate&token=$iexToken")
            .retrieve()
            .bodyToFlow()

    fun getStockPeerGroup(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/peers?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockLargestTrades(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/largest-trades?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockPreviousTwoYearsDividends(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("data/CORE/DIVIDENDS/$symbol?range=2y?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockPreviousDividend(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("data/CORE/DIVIDENDS/$symbol?last=1?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockNextDividends(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("data/CORE/DIVIDENDS/$symbol?next=1?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockNews(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/NEWS/$symbol?last=10&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockFinancials(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/FINANCIALS/$symbol?last=1&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockFundamentalValuations(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/FUNDAMENTALVALUATIONS/$symbol?last=1&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockFundamentals(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/FUNDAMENTALS/$symbol?last=1&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockInsiderSummary(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/INSIDERSUMMARY/$symbol?last=1&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockInstitutionalOwnership(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/INSTITUTIONALOWNERSHIP/$symbol?last=1&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockCompanyInfo(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("/data/CORE/COMPANY/$symbol?last=1&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getMostActive(): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/mostactive?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getGainers(): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/gainers?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getLosers(): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/losers?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getVolume(): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/iexvolume?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getPreMarketLosers(): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/premarket_losers?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getPreMarketGainers(): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/premarket_gainers?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getPostMarketLosers(): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/postmarket_losers?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getPostMarketGainers(): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/market/list/postmarket_gainers?token=$iexToken")
        .retrieve()
        .bodyToFlow()
}