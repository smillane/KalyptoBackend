package stockapp.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

class DBConfiguration {

    @Document("StockQuote")
    data class StockQuote(
        @Id
        val id: String? = null,
        var symbol: String,
        var docs: Object,
    )

    @Document("StockStatsBasic")
    data class StockStatsBasic(
        @Id
        val id: String? = null,
        var symbol: String,
        var docs: Object,
    )

    @Document("StockInsiderTrading")
    data class StockInsiderTrading(
        @Id
        val id: String? = null,
        var symbol: String,
        @Field("List of Insider Transactions")
        var docs: List<Object>,
    )

    @Document("StockLargestTrades")
    data class StockLargestTrades(
        @Id
        val id: String? = null,
        var symbol: String,
        var docs: Object,
    )

    @Document("StockNextDividend")
    data class StockNextDividend(
        @Id
        val id: String? = null,
        var symbol: String,
        var docs: Object,
    )

    @Document("StockPreviousDividends")
    data class StockPreviousDividends(
        @Id
        val id: String? = null,
        var symbol: String,
        @Field("List of Previous Dividends")
        var docs: List<Object>,
    )
}