package stockapp.users.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import stockapp.users.model.IndividualList
import stockapp.users.model.ListName
import stockapp.users.model.UserListsModel
import stockapp.users.service.UserInformation

@RestController
@RequestMapping("/users/")
class UserResource(
    val userInformation: UserInformation
) {
    @GetMapping("/{userID}/list")
    suspend fun getUserLists(@PathVariable("userID") userID: String): UserListsModel? {
        return userInformation.getUsersLists(userID)
    }

    @PostMapping("/{userID}/list")
    suspend fun addUserList(@PathVariable("userID") userID: String,
                            @PathVariable("list") list: IndividualList) {
        userInformation.addList(userID, list)
    }

    @PutMapping("/{userID}/list")
    suspend fun updateUserList(@PathVariable("userID") userID: String,
                               @PathVariable("list")list: Map<ListName, Map<String, Number>>) {
        userInformation.updateList(userID, list)
    }

    @PutMapping("/{userID}/list/{name}")
    suspend fun updateListName(@PathVariable("userID") userID: String,
                               @PathVariable("listName") listName: String) {
        userInformation.updateListName(userID, listName)
    }
}