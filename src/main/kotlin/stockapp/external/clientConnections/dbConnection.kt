package stockapp.external.clientConnections

import org.litote.kmongo.coroutine.*
import com.mongodb.ConnectionString
import org.litote.kmongo.reactivestreams.KMongo

import stockapp.stocks.model.*
import stockapp.users.model.*

val connectionString: ConnectionString = ConnectionString(System.getenv("MONGODB_URI"))
val client = KMongo.createClient(connectionString).coroutine
val database = client.getDatabase(connectionString.database ?: "StockApp")

val stockQuoteCollection =  database.getCollection<StockQuote>()
val stockStatsBasicCollection =  database.getCollection<StockStatsBasic>()
val stockPreviousDividendCollection =  database.getCollection<StockPreviousDividend>()
val stockNextDividendCollection =  database.getCollection<StockNextDividend>()
val stockLargestTradesCollection =  database.getCollection<StockLargestTrades>()
val stockInsiderTradingCollection =  database.getCollection<StockInsiderTrading>()
val stockFinancials =  database.getCollection<StockFinancials>()
val stockInsiderSummary =  database.getCollection<StockInsiderSummary>()
val stockInstitutionalOwnership =  database.getCollection<StockInstitutionalOwnership>()
val stockPeerGroup =  database.getCollection<StockPeerGroup>()
val stockCompanyInfo =  database.getCollection<StockCompanyInfo>()

val userLists =  database.getCollection<UserLists>()