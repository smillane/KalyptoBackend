package stockapp.external.clientConnections

import org.litote.kmongo.*
import org.litote.kmongo.coroutine.*
import com.mongodb.ConnectionString
import org.litote.kmongo.reactivestreams.KMongo
import stockapp.stocks.model.*

val connectionString: ConnectionString? = System.getenv("MONGODB_URI")?.let {
    ConnectionString("$it?retryWrites=false")
}

val client = if (connectionString !== null) KMongo.createClient(connectionString).coroutine else KMongo.createClient().coroutine
val database = client.getDatabase(connectionString?.database ?: "StockApp")

val stockQuoteCollection =  database.getCollection<StockQuote>()
val stockStatsBasicCollection =  database.getCollection<StockStatsBasic>()
val stockPreviousDividendCollection =  database.getCollection<StockPreviousDividend>()
val stockNextDividendCollection =  database.getCollection<StockNextDividend>()
val stockLargestTradesCollection =  database.getCollection<StockLargestTrades>()
val stockInsiderTradingCollection =  database.getCollection<StockInsiderTrading>()