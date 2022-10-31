package kalypto.users.model

import kotlinx.serialization.Serializable

@Serializable
data class Stock(val name: String)

@Serializable
data class Watchlist(val list: List<Stock>)

@Serializable
data class WatchlistName(val name: String)

@Serializable
data class WatchlistMap(val watchlistMap: Map<WatchlistName, Watchlist>)

@Serializable
data class UserLists(val userID: String, val userLists: List<WatchlistMap>)