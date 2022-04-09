package stockapp.model

data class StockQuote(val symbol: String, val lastUpdated: Int, val docs: Any)

data class StockStatsBasic(val symbol: String, val lastUpdated: Int, val docs: Any)

data class StockPreviousDividend(val symbol: String, val lastUpdated: Int, val docs: List<Any>)

data class StockNextDividend(val symbol: String, val nextUpdate: Int, val lastUpdated: Int, val docs: Any)

data class StockLargestTrades(val symbol: String, val lastUpdated: Int, val docs: Any)

data class StockInsiderTrading(val symbol: String, val lastUpdated: Int, val docs: List<Any>)
