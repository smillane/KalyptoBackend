package stockapp.stocks.model

import kotlinx.datetime.Instant

data class StockQuote(val symbol: String, val lastUpdated: Instant, val docs: Any)
data class StockStatsBasic(val symbol: String, val lastUpdated: Instant, val docs: Any)
data class StockPreviousDividend(val symbol: String, val lastUpdated: Instant, val docs: List<Any>)
data class StockNextDividend(val symbol: String, val nextUpdate: Instant, val lastUpdated: Instant, val docs: Any)
data class StockLargestTrades(val symbol: String, val lastUpdated: Instant, val docs: Any)
data class StockInsiderTrading(val symbol: String, val lastUpdated: Instant, val docs: List<Any>)