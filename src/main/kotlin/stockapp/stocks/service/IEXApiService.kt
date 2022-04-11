package stockapp.stocks.service

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

    fun GetStockQuote(symbol: String): Flow<String> = WebClient
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

    fun GetStockStatsBasic(symbol: String): Flow<String> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/stats?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetStockInsiderTrading(symbol: String): Flow<String> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("insider_transactions/$symbol?last=10?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetStockPreviousDividends(symbol: String): Flow<String> = WebClient
        .create(iexBaseTimeSeries)
        .get()
        .uri("dividends/$symbol?last=8?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetStockNextDividends(symbol: String): Flow<String> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/dividends/next?token=$iexToken")
        .retrieve()
        .bodyToFlow()

    fun GetStockLargestTrades(symbol: String): Flow<String> = WebClient
        .create(iexBase)
        .get()
        .uri("stock/$symbol/largest-trades?token=$iexToken")
        .retrieve()
        .bodyToFlow()
}