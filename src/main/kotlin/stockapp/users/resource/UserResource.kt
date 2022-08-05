package stockapp.users.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import stockapp.users.model.UserLists
import stockapp.users.service.UserInformation

@RestController
@RequestMapping("/users/")
class UserResource(
    val userInformation: UserInformation
) {
    @GetMapping("/{userID}")
    suspend fun getUserLists(@PathVariable("userID") userID: String): UserLists? {
        return userInformation.getUsersLists(userID)
    }

    @PostMapping("/{userID}/watchlist")
    suspend fun addWatchlist(@PathVariable("userID") userID: String, @PathVariable("watchlist") watchlist: String) {
        userInformation.addWatchlist(userID, watchlist)
    }

    @PostMapping("/{userID}/watchlist/{watchlist}/{stock}")
    suspend fun addStockToWatchlist(
        @PathVariable("userID") userID: String, @PathVariable("watchlist") watchlist: String, @PathVariable("stock") stock: String) {
        userInformation.addStockToWatchlist(userID, watchlist, stock)
    }

    @PostMapping("/{userID}/watchlist/{watchlist}/{stock}/position/{position}")
    suspend fun updateStockPosition(
        @PathVariable("userID") userID: String, @PathVariable("watchlist") watchlist: String, @PathVariable("stock") stock: String, @PathVariable("position") position: Int) {
        userInformation.updateStockPosition(userID, watchlist, stock, position)
    }

    @PutMapping("/{userID}/watchlist/{name}")
    suspend fun updateWatchlistName(@PathVariable("userID") userID: String, @PathVariable("watchlist") watchlist: String) {
        userInformation.updateWatchlistName(userID, watchlist)
    }

    @DeleteMapping("/{userID}/watchlist/{watchlist}/{stock}")
    suspend fun deleteStockFromWatchlist(
        @PathVariable("userID") userID: String, @PathVariable("watchlist") watchlist: String, @PathVariable("stock") stock: String) {
        userInformation.deleteStockFromWatchlist(userID, watchlist, stock)
    }

    @DeleteMapping("/{userID}/watchlist/{watchlist}")
    suspend fun deleteWatchlist(@PathVariable("userID") userID: String, @PathVariable("watchlist") watchlist: String) {
        userInformation.deleteWatchlist(userID, watchlist)
    }
}