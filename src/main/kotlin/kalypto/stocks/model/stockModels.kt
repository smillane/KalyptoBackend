package kalypto.stocks.model

data class StockQuote(val symbol: String, val lastUpdated: String, val docs: Map<String, Any>)
data class StockStats(val symbol: String, val lastUpdated: String, val docs: Map<String, Any>)
data class StockStatsBasic(val symbol: String, val lastUpdated: String, val docs: Map<String, Any>)
data class StockPreviousDividend(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockNextDividend(
    val symbol: String,
    val nextUpdate: String,
    val lastUpdated: String,
    val docs: Map<String, Any>,
)

data class StockLargestTrades(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockInsiderTrading(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockInstitutionalOwnership(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockInsiderSummary(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockFinancials(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockFundamentalValuations(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockFundamentals(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockPeerGroup(val symbol: String, val lastUpdated: String, val docs: List<Map<String, Any>>)
data class StockCompanyInfo(val symbol: String, val lastUpdated: String, val docs: Map<String, Any>)

data class DailyLists(val listType: String, val lastUpdated: String, val docs: List<Map<String, Any>>)

data class ReturnStockData(
    val quote: Map<String, Any>,
    val basicStats: Map<String, Any>,
    val previousDividends: List<Map<String, Any>>,
    val nextDividend: Map<String, Any>,
    val largestTrades: List<Map<String, Any>>,
    val insiderTrading: List<Map<String, Any>>,
    val institutionalOwnership: List<Map<String, Any>>,
    val insiderSummary: List<Map<String, Any>>,
    val financials: List<Map<String, Any>>,
    val fundamentalValuations: List<Map<String, Any>>,
    val fundamentals: List<Map<String, Any>>,
    val peerGroup: List<Map<String, Any>>,
    val companyInfo: Map<String, Any>,
)

// figure out when certain API calls are updated, adjust code to check on time/last update for that
// UPDATED AT
//quote = 4:30-8
//stats = end of day, 8am, 9am ET
//prev div =
//next div = end of day, 8am, 9am ET
//largest trades = 9:30-4
//insider trading = end of day, UTC (?)


//to update previous dividend, once next dividend is updated, will need to call previous dividend and only return latest, and then add to previous dividend collection