package stockapp.users.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import stockapp.users.service.UserInformation

@RestController
@RequestMapping("/users/")
class UserResource(
    val userInformation: UserInformation
) {
    @GetMapping("/{userID}/list")
    suspend fun getUserLists(@PathVariable("userID") userID: String): Any {
        return userInformation.getUserLists(userID)
    }

    @PutMapping("/{stockID}/list")
    suspend fun updateUserLists(@PathVariable("userID") userID: String): Any {
        return userInformation.updateUserLists(userID)
    }
}