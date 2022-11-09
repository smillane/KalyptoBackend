package kalypto.users.resource

import kalypto.users.model.UserLists
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kalypto.users.service.UserInformation

@RestController
@RequestMapping("/users/lists")
class UserResource(val userInformation: UserInformation) {

    @GetMapping("/{userID}")
    suspend fun getUserLists(
        @PathVariable("userID") userID: String,
    ): List<UserLists> {
        return userInformation.getUsersLists(userID)
    }

    @PostMapping("/{userID}/watchlist/{watchlistName}/position/{position}")
    suspend fun addWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
    ) {
        userInformation.addWatchlist(userID, watchlistName, position)
    }

    @PostMapping("/{userID}/watchlist/{watchlistName}/position/{position}/stock/{stock}")
    suspend fun addStockToWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
        @PathVariable("stock") stock: String,
    ) {
        userInformation.addStockToWatchlist(userID, watchlistName, position, stock)
    }

    @PutMapping("/{userID}/watchlist/{watchlist}/position/{position}/list/{list}")
    suspend fun updateWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
        @PathVariable("list") list: List<String>,
    ) {
        userInformation.updateWatchlist(userID, watchlistName, position, list)
    }

    @PutMapping("/{userID}/watchlist/{currentWatchlistName}/position/{position}/{newWatchlistName}")
    suspend fun updateWatchlistName(
        @PathVariable("userID") userID: String,
        @PathVariable("currentWatchlistName") currentWatchlistName: String,
        @PathVariable("position") position: Int,
        @PathVariable("newWatchlistName") newWatchlistName: String,
    ) {
        userInformation.updateWatchlistName(userID, currentWatchlistName, position, newWatchlistName)
    }

    @DeleteMapping("/{userID}/watchlist/{watchlistName}/position/{position}/stock/{stock}")
    suspend fun deleteStockFromWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
        @PathVariable("stock") stock: String,
    ) {
        userInformation.deleteStockFromWatchlist(userID, watchlistName, position, stock)
    }

    @DeleteMapping("/{userID}/watchlist/{watchlistName}/position/{position}")
    suspend fun deleteWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("position") position: Int,
    ) {
        userInformation.deleteWatchlist(userID, watchlistName, position)
    }
}