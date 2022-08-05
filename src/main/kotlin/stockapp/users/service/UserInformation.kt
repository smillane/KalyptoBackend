package stockapp.users.service

import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import stockapp.external.clientConnections.userLists
import stockapp.users.model.*

class UserInformation {
    private val upsertTrue = UpdateOptions().upsert(true)

    fun getUsersLists(userId: String): CoroutineFindPublisher<UserLists> {
        return userLists.find(UserLists::userID eq userId)
    }

    suspend fun addWatchlist(userId: String, listName: String) {
        userLists.updateOne(UserLists::userID eq userId,
            set(UserLists::userLists / WatchlistMap::watchlistMap / WatchlistName::name setTo listName), upsertTrue)
    }

    suspend fun addStockToWatchlist(userId: String, watchlist: String, stock: Stock) {
        userLists.updateOne(
            and(UserLists::userID eq userId, UserLists::userLists / WatchlistMap::watchlistMap / WatchlistName::name eq watchlist),
            push(UserLists::userLists / WatchlistMap::watchlistMap / Watchlist::list, stock), upsertTrue)
    }

    suspend fun deleteStockFromWatchlist(userId: String, watchlist: String, stock: String) {
        userLists.deleteOne(
            UserLists::userID eq userId, UserLists::userLists / WatchlistMap::watchlistMap / WatchlistName::name eq watchlist,
            UserLists::userLists / WatchlistMap::watchlistMap / Watchlist::list / Stock::name eq stock)
    }

    suspend fun updateWatchlistName(userId: String, watchlist: String) {
        userLists.updateOne(
            and(UserLists::userID eq userId, UserLists::userLists / WatchlistMap::watchlistMap / WatchlistName::name eq watchlist),
            set(UserLists::userLists / WatchlistMap::watchlistMap / WatchlistName::name setTo watchlist))
    }

    suspend fun updateStockPosition(userId: String, watchlist: String, list: List<Stock>) {
        userLists.updateOne(
            and(UserLists::userID eq userId , UserLists::userLists / WatchlistMap::watchlistMap / WatchlistName::name eq watchlist),
            set(UserLists::userLists / WatchlistMap::watchlistMap / Watchlist::list setTo list), upsertTrue)
    }

    suspend fun deleteWatchlist(userId: String, watchlist: String) {
        userLists.deleteOne(UserLists::userID eq userId,
            UserLists::userLists / WatchlistMap::watchlistMap / WatchlistName::name eq watchlist)
    }
}