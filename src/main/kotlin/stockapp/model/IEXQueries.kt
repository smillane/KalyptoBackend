package stockapp.model

class IEXQueries {
    private val IEX_BASE_API: String = "https://sandbox.iexapis.com/stable/"
    private val IEX_BASE_API_TIMES_SERIES: String = "https://sandbox.iexapis.com/stable/time-series/"
    private val IEX_PUBLIC_TOKEN: String = "\${IEX_PUBLIC_TOKEN}"

    fun stockQuote(symbol: String): String {
        return (IEX_BASE_API + "stock/" + symbol + "/quote?token=" + IEX_PUBLIC_TOKEN)
    }

    fun stockStatsBasic(symbol: String): String {
        return (IEX_BASE_API + "stock/" + symbol + "/stats?token=" + IEX_PUBLIC_TOKEN)
    }

    fun stockInsiderTrading(symbol: String, lastUpdated: String): String {
        return (IEX_BASE_API_TIMES_SERIES + "insider_transactions/" + symbol + "?last=10?token=" + IEX_PUBLIC_TOKEN)
    }

    fun stockPreviousDividends(symbol: String): String {
        return (IEX_BASE_API_TIMES_SERIES + "dividends/" + symbol + "?last=4?token=" + IEX_PUBLIC_TOKEN)
    }

    fun stockNextDividends(symbol: String): String {
        return (IEX_BASE_API + "stock/" + symbol + "/dividends/next?token=" + IEX_PUBLIC_TOKEN)
    }

    fun stockLargestTrades(symbol: String): String {
        return (IEX_BASE_API + "stock/" + symbol + "/largest-trades?token=" + IEX_PUBLIC_TOKEN)
    }
}