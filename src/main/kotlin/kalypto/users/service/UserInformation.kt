package kalypto.users.service

import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.*
import org.springframework.stereotype.Component

import kalypto.external.clientConnections.userListsCollection
import kalypto.users.model.*
import kalypto.stocks.service.StockQueriesService
import kotlinx.coroutines.coroutineScope

@Component
class UserInformation(val stockQueriesService: StockQueriesService) {
    private val upsertTrue = UpdateOptions().upsert(true)

    suspend fun getUsersLists(userUID: String): List<UserLists> = coroutineScope {
        return@coroutineScope userListsCollection.find(UserLists::userID eq userUID).toList().sortedBy { it.position }
    }

    suspend fun getWatchlist(userUID: String, watchlistName: String, position: Int) = coroutineScope {
        return@coroutineScope userListsCollection.findOne(UserLists::userID eq userUID, UserLists::watchlistName eq watchlistName, UserLists::position eq position)
    }

    suspend fun addWatchlist(userUID: String, watchlistName: String, position: Int) = coroutineScope {
        userListsCollection.insertOne(UserLists(userUID, position, watchlistName, emptyList()))
    }

    suspend fun addStockToWatchlist(userUID: String, watchlistName: String, position: Int, stock: String) =
        coroutineScope {
            if (stockQueriesService.basicApiCheck(stock)) {
                userListsCollection.updateOne(
                    and(
                        UserLists::userID eq userUID,
                        UserLists::watchlistName eq watchlistName,
                        UserLists::position eq position
                    ), addToSet(UserLists::watchlist, stock)
                )
            }
            userListsCollection.updateOne(
                and(
                    UserLists::userID eq userUID,
                    UserLists::watchlistName eq watchlistName,
                    UserLists::position eq position
                ), addToSet(UserLists::watchlist, stock)
            )
        }

    suspend fun deleteStockFromWatchlist(userUID: String, watchlistName: String, position: Int, stock: String) =
        coroutineScope {
            userListsCollection.deleteOne(
                UserLists::userID eq userUID,
                UserLists::watchlistName eq watchlistName,
                UserLists::position eq position,
                UserLists::watchlist contains stock
            )
        }

    suspend fun updateWatchlistName(
        userUID: String,
        currentWatchlistName: String,
        position: Int,
        newWatchlistName: String,
    ) = coroutineScope {
        userListsCollection.updateOne(
            and(
                UserLists::userID eq userUID,
                UserLists::position eq position,
                UserLists::watchlistName eq currentWatchlistName
            ),
            setValue(UserLists::watchlistName, newWatchlistName)
        )
    }

    suspend fun updateWatchlist(userUID: String, watchlistName: String, position: Int, list: List<String>) =
        coroutineScope {
            userListsCollection.updateOne(
                and(
                    UserLists::userID eq userUID,
                    UserLists::watchlistName eq watchlistName,
                    UserLists::position eq position
                ),
                setValue(UserLists::watchlist, list),
                upsertTrue
            )
        }

    suspend fun deleteWatchlist(userUID: String, watchlist: String, position: Int) = coroutineScope {
        userListsCollection.deleteOne(
            UserLists::userID eq userUID,
            UserLists::position eq position,
            UserLists::watchlistName eq watchlist
        )
    }
}