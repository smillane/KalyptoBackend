package stockapp.stocks.model

data class CryptoQuote(val symbol: String, val lastUpdated: String, val docs: Map<String, Any>)
data class CryptoPrice(val symbol: String, val lastUpdated: String, val docs: Map<String, Any>)