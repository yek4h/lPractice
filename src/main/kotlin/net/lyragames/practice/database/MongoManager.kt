package net.lyragames.practice.database

import com.mongodb.*
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import dev.ryu.core.shared.backend.mongodb.MongoManager
import dev.ryu.core.shared.system.module.BackendModule
import org.bson.Document
import java.util.concurrent.CompletableFuture


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 25/06/2024
*/

class MongoManager {

    lateinit var mongoClient: MongoClient
    lateinit var mongoDatabase: MongoDatabase
    lateinit var profileCollection: MongoCollection<Document>
    lateinit var arenaRatingsCollection: MongoCollection<Document>

    fun initialize(useCredentials: Boolean = false): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            mongoClient = if (useCredentials) {
                MongoClient(
                    ServerAddress("127.0.0.1", 27017),
                    MongoCredential.createCredential("admin", "practice", "your_password".toCharArray()),
                    MongoClientOptions.builder().build()
                )
            } else {
                MongoClient("127.0.0.1", 27017)
            }
            mongoDatabase = mongoClient.getDatabase("practice")
            profileCollection = mongoDatabase.getCollection("profiles")
            arenaRatingsCollection = mongoDatabase.getCollection("arenaRatings")
        }
    }

    fun close() {
        if (this::mongoClient.isInitialized) {
            mongoClient.close()
        }
    }
}