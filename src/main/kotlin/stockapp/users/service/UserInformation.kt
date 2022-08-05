package stockapp.users.service

import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.*
import stockapp.external.clientConnections.userLists
import stockapp.users.model.*

class UserInformation {
    private val upsertTrue = UpdateOptions().upsert(true)

    suspend fun getUsersLists(userId: String): UserLists? {
        return userLists.findOne(UserLists::userID eq userId)
    }

    suspend fun addWatchlist(userId: String, listName: String) {
        userLists.updateOne(UserLists::userID eq userId, set(UserLists::userLists / Watchlist::watchlist / WatchlistName::name setTo listName), upsertTrue)
    }

    suspend fun addStockToWatchlist(userId: String, watchlist: String, stock: String) {
        userLists.updateOne(and(UserLists::userID eq userId, UserLists::userLists / Watchlist::watchlist / WatchlistName::name eq watchlist), set(UserLists::userLists / Watchlist::watchlist / Stock::name setTo stock), upsertTrue)
    }

    suspend fun deleteStockFromWatchlist(userId: String, watchlist: String, stock: String) {
        userLists.deleteOne(UserLists::userID eq userId, Watchlist::watchlist / WatchlistName::name eq watchlist, UserLists::userLists / Watchlist::watchlist / Stock::name eq stock)
    }

    suspend fun updateWatchlistName(userId: String, watchlist: String) {
        userLists.updateOne(and(UserLists::userID eq userId, UserLists::userLists / Watchlist::watchlist / WatchlistName::name eq watchlist), set(UserLists::userLists / Watchlist::watchlist / WatchlistName::name setTo watchlist))
    }

    suspend fun updateStockPosition(userId: String, watchlist: String, stock: String, position: Int) {
        userLists.updateOne(and(UserLists::userID eq userId , UserLists::userLists / Watchlist::watchlist / WatchlistName::name eq watchlist, UserLists::userLists / Watchlist::watchlist / Stock::name eq stock, UserLists::userLists / Watchlist::watchlist / Stock::name eq stock), set(UserLists::userLists / Watchlist::watchlist / Rank::rank setTo position), upsertTrue)
    }

    suspend fun deleteWatchlist(userId: String, watchlist: String) {
        userLists.deleteOne(UserLists::userID eq userId, UserLists::userLists / Watchlist::watchlist / WatchlistName::name eq watchlist)
    }
}