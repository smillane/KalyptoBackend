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

// figure out when certain API calls are updated, adjust code to check on time/last update for that
// UPDATED AT
//quote = 4:30-8
//stats = end of day, 8am, 9am ET
//prev div =
//next div = end of day, 8am, 9am ET
//largest trades = 9:30-4
//insider trading = end of day, UTC (?)


//to update previous dividend, once next dividend is updated, will need to call previous dividend and only return latest, and then add to previous dividend collection