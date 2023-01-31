package com.telotengoca.moth.model

import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import kotlin.system.exitProcess

interface MothDatabase {
    /**
     * This functions creates the database
     */
    fun createDatabase(): Unit

    /**
     * Connects with database
     */
    fun connectDatabase(): Connection
}

class MothDatabaseImpl: MothDatabase {
    companion object {
        /**
         * Connection string for sqlite database with JDBC
         */
        const val CONNECTION_STRING = "jdbc:sqlite:database/moth.sqlite"
    }

    /**
     * This method creates a sqlite database in a file in root dir.
     */
    override fun createDatabase() {
        // create "database" dir
        val dirName = "database"
        val rootDir = File(dirName)
        try {
            if (!rootDir.exists()) rootDir.mkdir()
            // create database file for sqlite
            val databaseFile = File(rootDir, "moth.sqlite")
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
        return DriverManager.getConnection(CONNECTION_STRING)
    }
}
