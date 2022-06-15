package stockapp.stocks.model

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.datetime.Instant

data class StockQuote(val symbol: String, val lastUpdated: Instant, val docs: JsonNode)
data class StockStatsBasic(val symbol: String, val lastUpdated: Instant, val docs: JsonNode)
data class StockPreviousDividend(val symbol: String, val lastUpdated: Instant, val docs: List<JsonNode>)
data class StockNextDividend(val symbol: String, val nextUpdate: Instant, val lastUpdated: Instant, val docs: JsonNode)
data class StockLargestTrades(val symbol: String, val lastUpdated: Instant, val docs: JsonNode)
data class StockInsiderTrading(val symbol: String, val lastUpdated: Instant, val docs: List<JsonNode>)
data class ReturnStockData(val quote: JsonNode, val stats: JsonNode, val previousDividends: List<JsonNode>, val nextDividend: JsonNode, val largestTrades: JsonNode, val insiderTrading: List<JsonNode>)