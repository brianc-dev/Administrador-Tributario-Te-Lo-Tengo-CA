package com.telotengoca.moth.model

import com.telotengoca.moth.config.Config
import com.telotengoca.moth.logger.MothLogger
import com.telotengoca.moth.logger.MothLoggerFactory
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
        private val logger: MothLogger = MothLoggerFactory.getLogger(MothDatabase::class.java)

        private val properties: Properties = Properties()

        /**
         * Convenience function to check if a table exists. Only works for SQLite.
         * @param table name of table to check
         * @param connection connection to database where to check. Database must be a SQLite database
         */
        fun tableExists(table: String, connection: Connection): Boolean {
            // TODO: Fix this. Seems to not be working
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
        properties.load(this::class.java.getResourceAsStream(Config.CONFIG_FILE_URL))

        val dbDir = checkNotNull(properties.getProperty("DATABASE_DIR"))
        val dbFile = checkNotNull(properties.getProperty("DATABASE_FILE"))
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
            logger.error("A security exception occurred. Database directory or file couldn't be created", e)
            exitProcess(255)
        } catch (e: IOException) {
            logger.error("An I/O error occurred", e)
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
