package net.lyragames.practice.database

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import java.io.Closeable

class Mongo (private val dbName: String) : Closeable {


    lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    lateinit var profiles: MongoCollection<Document>

    fun load(credentials: MongoCredentials) {
        client = if (credentials.shouldAuthenticate()) {
            val serverAddress = ServerAddress(credentials.host, credentials.port)
            val credential = MongoCredential.createCredential(credentials.username!!, dbName, credentials.password!!.toCharArray())
            MongoClient(serverAddress, credential, MongoClientOptions.builder().build())

        } else {
            MongoClient(credentials.host, credentials.port)
        }
        database = client.getDatabase("lpractice")

        profiles = database.getCollection("profiles")
    }

    override fun close() {
        client.close()
    }

}