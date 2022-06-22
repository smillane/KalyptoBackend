package stockapp.stocks.service

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.*

@Service
class IEXApiService {

    private val iexToken: String = System.getenv("IEX_PUBLIC_TOKEN")
    private val iexBase: String = "https://sandbox.iexapis.com/stable/"
    private val iexBaseTimeSeries: String = "https://sandbox.iexapis.com/stable/time-series/"

    fun getStockQuote(symbol: String): Flow<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/quote?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockStatsBasic(symbol: String): Flow<Map<String, Any>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/stats?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getLast15StockInsiderTrading(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("insider_transactions/$symbol?last=15&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockInsiderTradingFromLastUpdated(symbol: String, lastUpdate: Instant?): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("insider_transactions/$symbol/?from=$lastUpdate&token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockPreviousTwoYearsDividends(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/dividends/2y?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockPreviousDividend(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("DIVIDENDS/$symbol/?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockNextDividends(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/dividends/next?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun getStockLargestTrades(symbol: String): Flow<List<Map<String, Any>>> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/largest-trades?token=$iexToken")
        .retrieve()
        .bodyToFlow()
}