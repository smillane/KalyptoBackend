package stockapp.stocks.service

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.*

@Service
class IEXApiService {

    @Value("\${IEX_PUBLIC_TOKEN}")
    lateinit var iexToken: String

    private val iexBase: String = "https://sandbox.iexapis.com/stable/"
    private val iexBaseTimeSeries: String = "https://sandbox.iexapis.com/stable/time-series/"
    private val returnError: Flow<String> = flowOf("false")
    private val returnNotFound: Flow<String> = flowOf("notFound")

    fun GetStockQuote(symbol: String): Flow<Any> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/quote?token=$iexToken")
        .exchangeToFlow { response ->
            if (response.statusCode() == HttpStatus.OK) {
                return@exchangeToFlow response.bodyToFlow()
            }
            if (response.statusCode().is4xxClientError) {
                return@exchangeToFlow returnNotFound;
            }
            if (response.statusCode().is5xxServerError) {
                return@exchangeToFlow returnError;
            }
            else {
                return@exchangeToFlow returnError;
            }}

    fun GetStockStatsBasic(symbol: String): Flow<JsonNode> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/stats?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetLast15StockInsiderTrading(symbol: String): Flow<JsonNode> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("insider_transactions/$symbol?last=15?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetStockInsiderTradingFromLastUpdated(symbol: String, lastUpdate: String): Flow<JsonNode> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("insider_transactions/$symbol/from$lastUpdate?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetStockPreviousDividends(symbol: String): Flow<JsonNode> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/dividends/2y?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetStockNextDividends(symbol: String): Flow<JsonNode> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/dividends/next?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetStockLargestTrades(symbol: String): Flow<JsonNode> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/largest-trades?token=$iexToken")
        .retrieve()
        .bodyToFlow()
}