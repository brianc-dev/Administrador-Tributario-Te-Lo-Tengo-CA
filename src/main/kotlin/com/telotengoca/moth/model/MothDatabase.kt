package com.telotengoca.moth.model

import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import kotlin.system.exitProcess

interface MothDatabase {
    /**
     * Connects with database
     */
    fun connectDatabase(): Connection
}

class MothDatabaseImpl: MothDatabase {

    private val connectionString: String
    companion object {

        private val properties: Properties = Properties()

        fun tableExists(table: String, connection: Connection): Boolean {
            connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name = ?").use {
                it.setString(1, table)
                it.executeQuery().use {
                    it.next()
                    val result = it.getInt(1)
                    return result != 0
                }
            }
        }
    }

    init {
        properties.load(this::class.java.getResourceAsStream("/config.properties"))

        val dbDir = properties.getProperty("DATABASE_DIR")
        val dbFile = properties.getProperty("DATABASE_FILE")
        connectionString = "jdbc:sqlite:$dbDir/$dbFile"
        
        createDatabase()
    }

    /**
     * This method creates a sqlite database in a file in root dir.
     */
    private fun createDatabase() {
        // create "database" dir
        val dirName = checkNotNull(properties.getProperty("DATABASE_DIR"))
        val rootDir = File(dirName)
        try {
            if (!rootDir.exists()) rootDir.mkdir()
            // create database file for sqlite
            val dbFileName = checkNotNull(properties.getProperty("DATABASE_FILE"))
            val databaseFile = File(rootDir, dbFileName)
            if (!databaseFile.exists()) databaseFile.createNewFile()
        } catch (e: SecurityException) {
            // TODO: add log
            print("a security exception occurred")
            print(e.localizedMessage)
            exitProcess(255)
        } catch (e: IOException) {
            // TODO: add log
            print("an IO error occurred")
            print(e.localizedMessage)
            exitProcess(255)
        }
    }

    /**
     * Returns a Connection to interact with the database
     * @return Connection if successfully connected to database
     * @exception java.sql.SQLException
     * @exception java.sql.SQLTimeoutException
     */
     override fun connectDatabase(): Connection {
        return DriverManager.getConnection(connectionString)
    }
}
