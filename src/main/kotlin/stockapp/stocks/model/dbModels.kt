package stockapp.stocks.model

data class StockQuote(val symbol: String, val lastUpdated: String, val docs: Any)

data class StockStatsBasic(val symbol: String, val lastUpdated: String, val docs: Any)

data class StockPreviousDividend(val symbol: String, val lastUpdated: String, val docs: List<Any>)

data class StockNextDividend(val symbol: String, val nextUpdate: String, val lastUpdated: String, val docs: Any)

data class StockLargestTrades(val symbol: String, val lastUpdated: String, val docs: Any)

data class StockInsiderTrading(val symbol: String, val lastUpdated: String, val docs: List<Any>)