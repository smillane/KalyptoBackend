package stockapp.external.client

import com.mongodb.ConnectionString
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*

val connectionString: ConnectionString? = System.getenv("MONGODB_URI")?.let {
    ConnectionString("$it?retryWrites=false")
}

val client = if (connectionString != null) KMongo.createClient(connectionString).coroutine else KMongo.createClient().coroutine