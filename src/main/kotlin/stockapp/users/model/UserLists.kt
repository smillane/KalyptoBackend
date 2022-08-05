package stockapp.users.model

import org.bson.codecs.pojo.annotations.BsonProperty

data class Rank(val rank: Int)
data class Stock(val name: String)
data class WatchlistName(val name: String)
data class Watchlist(@BsonProperty(useDiscriminator = true) val watchlist: Map<WatchlistName, Map<Stock, Rank>>)
data class UserLists(val userID: String, @BsonProperty(useDiscriminator = true) val userLists: List<Watchlist>)