package kalypto.users.model

import kotlinx.serialization.Serializable

@Serializable
data class UserLists(val userID: String, val position: Int, val watchlistName: String, val watchlist: List<String>)