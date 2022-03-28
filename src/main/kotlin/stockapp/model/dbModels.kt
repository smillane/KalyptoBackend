package stockapp.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("StockQuote")
data class StockQuote(@Id val id: Long, val symbol: String, val lastUpdated: Int, val docs: Any)

@Document("StockStatsBasic")
data class StockStatsBasic(@Id val id: Long, val symbol: String, val lastUpdated: Int, val docs: Any)

@Document("StockPreviousDividend")
data class StockPreviousDividend(@Id val id: Long, val symbol: String, val lastUpdated: Int, val docs: List<Any>)

@Document("StockNextDividend")
data class StockNextDividend(@Id val id: Long, val symbol: String, val nextUpdate: Int, val lastUpdated: Int, val docs: Any)

@Document("StockLargestTrades")
data class StockLargestTrades(@Id val id: Long, val symbol: String, val lastUpdated: Int, val docs: Any)

@Document("StockInsiderTrading")
data class StockInsiderTrading(@Id val id: Long, val symbol: String, val lastUpdated: Int, val docs: List<Any>)
