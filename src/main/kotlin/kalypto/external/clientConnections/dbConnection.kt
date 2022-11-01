package kalypto.external.clientConnections

import org.litote.kmongo.coroutine.*
import com.mongodb.ConnectionString
import org.litote.kmongo.reactivestreams.KMongo

import kalypto.stocks.model.*
import kalypto.users.model.*

val connectionString: ConnectionString = ConnectionString(System.getenv("MONGODB_URI"))
val client = KMongo.createClient(connectionString).coroutine
val database = client.getDatabase(connectionString.database ?: "StockApp")

val stockQuoteCollection = database.getCollection<StockQuote>()
val stockStatsCollection = database.getCollection<StockStats>()
val stockStatsBasicCollection = database.getCollection<StockStatsBasic>()
val stockPreviousDividendCollection = database.getCollection<StockPreviousDividend>()
val stockNextDividendCollection = database.getCollection<StockNextDividend>()
val stockLargestTradesCollection = database.getCollection<StockLargestTrades>()
val stockInsiderTradingCollection = database.getCollection<StockInsiderTrading>()
val stockInstitutionalOwnershipCollection = database.getCollection<StockInstitutionalOwnership>()
val stockInsiderSummaryCollection = database.getCollection<StockInsiderSummary>()
val stockFinancialsCollection = database.getCollection<StockFinancials>()
val stockFundamentalValuationsCollection = database.getCollection<StockFundamentalValuations>()
val stockFundamentalsCollection = database.getCollection<StockFundamentals>()
val stockPeerGroupCollection = database.getCollection<StockPeerGroup>()
val stockCompanyInfoCollection = database.getCollection<StockCompanyInfo>()

val dailyListCollection = database.getCollection<DailyLists>()

val userLists = database.getCollection<UserLists>()