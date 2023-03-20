package kalypto.users.resource

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import kalypto.users.model.UserLists
import kalypto.users.service.UserInformation
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users/lists")
class UserResource(val userInformation: UserInformation) {

    @GetMapping("/{userID}")
    suspend fun getUserLists(
        @PathVariable("userID") userID: String,
    ): List<UserLists> {
        return userInformation.getUsersLists(userID)
    }

    @GetMapping("/{userID}/watchlist/{watchlistName}/position/{position}")
    suspend fun getSingleWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
    ): UserLists? {
        return userInformation.getWatchlist(userID, watchlistName, position)
    }

    @PostMapping("/{userID}/watchlist/{watchlistName}/position/{position}")
    suspend fun addWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
    ): InsertOneResult {
        return userInformation.addWatchlist(userID, watchlistName, position)
    }

    @DeleteMapping("/{userID}/watchlist/{watchlistName}/position/{position}")
    suspend fun deleteWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
    ): DeleteResult {
        return userInformation.deleteWatchlist(userID, watchlistName, position)
    }

    @PutMapping("/{userID}/watchlist/{currentWatchlistName}/position/{position}/{newWatchlistName}")
    suspend fun updateWatchlistName(
        @PathVariable("userID") userID: String,
        @PathVariable("currentWatchlistName") currentWatchlistName: String,
        @PathVariable("position") position: Int,
        @PathVariable("newWatchlistName") newWatchlistName: String,
    ): UpdateResult {
        return userInformation.updateWatchlistName(userID, currentWatchlistName, position, newWatchlistName)
    }

    @PostMapping("/{userID}/watchlist/{watchlistName}/position/{position}/stock/{stock}")
    suspend fun addStockToWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
        @PathVariable("stock") stock: String,
    ): UpdateResult {
        return userInformation.addStockToWatchlist(userID, watchlistName, position, stock)
    }

    @DeleteMapping("/{userID}/watchlist/{watchlistName}/position/{position}/stock/{stock}")
    suspend fun deleteStockFromWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
        @PathVariable("stock") stock: String,
    ): DeleteResult {
        return userInformation.deleteStockFromWatchlist(userID, watchlistName, position, stock)
    }

    @PutMapping("/{userID}/watchlist/{watchlistName}/position/{position}")
    suspend fun updateWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
        @RequestBody list: List<String>,
    ): UpdateResult {
        return userInformation.updateWatchlist(userID, watchlistName, position, list)
    }
}