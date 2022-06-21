package stockapp.external.clientConnections

import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.*
import com.mongodb.ConnectionString
import org.litote.kmongo.reactivestreams.KMongo

import stockapp.stocks.model.*

val connectionString: ConnectionString = ConnectionString(System.getenv("MONGODB_URI"))
val client = connectionString.let { KMongo.createClient(it).coroutine }
val database = client.getDatabase(connectionString.database ?: "StockApp")

val stockQuoteCollection =  database.getCollection<StockQuote>()
val stockStatsBasicCollection =  database.getCollection<StockStatsBasic>()
val stockPreviousDividendCollection =  database.getCollection<StockPreviousDividend>()
val stockNextDividendCollection =  database.getCollection<StockNextDividend>()
val stockLargestTradesCollection =  database.getCollection<StockLargestTrades>()
val stockInsiderTradingCollection =  database.getCollection<StockInsiderTrading>()