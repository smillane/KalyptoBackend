package stockapp.users.model

import org.bson.codecs.pojo.annotations.BsonProperty

data class Stock(val name: String)
data class Watchlist(@BsonProperty(useDiscriminator = true) val list: List<Stock>)
data class WatchlistName(val name: String)
data class WatchlistMap(@BsonProperty(useDiscriminator = true) val watchlistMap: Map<WatchlistName, Watchlist>)
data class UserLists(val userID: String, @BsonProperty(useDiscriminator = true) val userLists: List<WatchlistMap>)