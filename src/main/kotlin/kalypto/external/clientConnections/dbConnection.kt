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
val stockInstitutionalOwnership = database.getCollection<StockInstitutionalOwnership>()
val stockInsiderSummary = database.getCollection<StockInsiderSummary>()
val stockFinancials = database.getCollection<StockFinancials>()
val stockFundamentalValuations = database.getCollection<StockFundamentalValuations>()
val stockFundamentals = database.getCollection<StockFundamentals>()
val stockPeerGroup = database.getCollection<StockPeerGroup>()
val stockCompanyInfo = database.getCollection<StockCompanyInfo>()

val mostActive = database.getCollection<MostActive>()
val gainers = database.getCollection<Gainers>()
val losers = database.getCollection<Losers>()
val volume = database.getCollection<Volume>()

val userLists = database.getCollection<UserLists>()