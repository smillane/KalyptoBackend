package stockapp.users.service

import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.*
import stockapp.external.clientConnections.userLists
import stockapp.users.model.AllLists
import stockapp.users.model.IndividualList
import stockapp.users.model.ListName
import stockapp.users.model.UserListsModel

class UserInformation {
    private val upsertTrue = UpdateOptions().upsert(true)

    suspend fun getUsersLists(userId: String): UserListsModel? {
        return userLists.findOne(UserListsModel::userID eq userId)
    }

    suspend fun updateList(userId: String, list: Map<ListName, Map<String, Number>>) {
        userLists.updateOne(UserListsModel::userID eq userId, set(IndividualList::stockList setTo list), upsertTrue)
    }

    suspend fun updateListName(userId: String, listName: String) {
        userLists.updateOne(UserListsModel::userID eq userId, set(ListName::name setTo listName), upsertTrue)
    }

    suspend fun addList(userId: String, list: IndividualList) {
        userLists.updateOne(UserListsModel::userID eq userId, push(AllLists::lists, list))
    }
}