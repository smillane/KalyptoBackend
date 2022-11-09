package kalypto.users.service

import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.*
import org.springframework.stereotype.Component

import kalypto.external.clientConnections.userListsCollection
import kalypto.users.model.*
import kalypto.stocks.service.StockQueriesService

@Component
class UserInformation(val stockQueriesService: StockQueriesService) {
    private val upsertTrue = UpdateOptions().upsert(true)

    suspend fun getUsersLists(userUID: String): List<UserLists> {
        return userListsCollection.find(UserLists::userID eq userUID).toList().sortedBy { it.position }
    }

    suspend fun addWatchlist(userUID: String, watchlistName: String, position: Int) {
        userListsCollection.insertOne(UserLists(userUID, position, watchlistName, emptyList()))
    }

    suspend fun addStockToWatchlist(userUID: String, watchlistName: String, position: Int, stock: String) {
        if (stockQueriesService.basicApiCheck(stock)) {
        }
        userListsCollection.updateOne(and(UserLists::userID eq userUID, UserLists::watchlistName eq watchlistName, UserLists::position eq position), addToSet(UserLists::watchlist, stock))
    }

    suspend fun deleteStockFromWatchlist(userUID: String, watchlistName: String, position: Int, stock: String) {
        userListsCollection.deleteOne(UserLists::userID eq userUID, UserLists::watchlistName eq watchlistName, UserLists::position eq position, UserLists::watchlist contains stock)
    }

    suspend fun updateWatchlistName(userUID: String, oldWatchlistName: String, position: Int, newWatchlistName: String) {
        userListsCollection.updateOne(and(UserLists::userID eq userUID, UserLists::position eq position, UserLists::watchlistName eq oldWatchlistName), set(UserLists::watchlistName setTo newWatchlistName), upsertTrue)
    }

    suspend fun updateWatchlist(userUID: String, watchlistName: String, position: Int, list: List<String>) {
        userListsCollection.updateOne(
            and(
                UserLists::userID eq userUID,
                UserLists::watchlistName eq watchlistName,
                UserLists::position eq position
            ),
            setValue(UserLists::watchlist, list), upsertTrue
        )
    }

    suspend fun deleteWatchlist(userUID: String, watchlist: String, position: Int) {
        userListsCollection.deleteOne(UserLists::userID eq userUID, UserLists::position eq position, UserLists::watchlistName eq watchlist)
    }
}