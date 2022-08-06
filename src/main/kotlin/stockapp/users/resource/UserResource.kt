package stockapp.users.resource

import org.litote.kmongo.coroutine.CoroutineFindPublisher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import stockapp.users.model.Stock
import stockapp.users.model.UserLists
import stockapp.users.service.UserInformation

@RestController
@RequestMapping("/users/")
class UserResource(val userInformation: UserInformation) {
    @GetMapping("/{userID}")
    suspend fun getUserLists(
        @PathVariable("userID") userID: String): CoroutineFindPublisher<UserLists> {
        return userInformation.getUsersLists(userID)
    }

    @PostMapping("/{userID}/watchlist/{watchlistName}")
    suspend fun addWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String) {
        userInformation.addWatchlist(userID, watchlistName)
    }

    @PostMapping("/{userID}/watchlist/{watchlist}/stock/{stock}")
    suspend fun addStockToWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("stock") stock: Stock) {
        userInformation.addStockToWatchlist(userID, watchlistName, stock)
    }

    @PutMapping("/{userID}/watchlist/{watchlist}/list/{list}")
    suspend fun updateWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("list") list: List<Stock>) {
        userInformation.updateWatchlist(userID, watchlistName, list)
    }

    @PutMapping("/{userID}/watchlist/{watchlistName}")
    suspend fun updateWatchlistName(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String) {
        userInformation.updateWatchlistName(userID, watchlistName)
    }

    @DeleteMapping("/{userID}/watchlist/{watchlistName}/{stock}")
    suspend fun deleteStockFromWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String,
        @PathVariable("stock") stock: String) {
        userInformation.deleteStockFromWatchlist(userID, watchlistName, stock)
    }

    @DeleteMapping("/{userID}/watchlist/{watchlistName}")
    suspend fun deleteWatchlist(
        @PathVariable("userID") userID: String,
        @PathVariable("watchlistName") watchlistName: String) {
        userInformation.deleteWatchlist(userID, watchlistName)
    }
}