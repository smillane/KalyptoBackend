package stockapp.users.model

data class ListName(val name: String)
data class IndividualList(val stockList: Map<ListName, Map<String, Number>>)
data class AllLists(val lists: List<IndividualList>)
data class UserListsModel(val userID: String, val lists: AllLists)