package com.telotengoca.moth.logger

import com.telotengoca.moth.model.MothDatabase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.DriverManager
import java.util.*

interface MothLogger {

    fun trace(message: String, vararg args: Any)
    fun trace(message: String, throwable: Throwable)
    fun debug(message: String, vararg args: Any)
    fun debug(message: String, throwable: Throwable)
    fun info(message: String, vararg args: Any)
    fun info(message: String, throwable: Throwable)
    fun warn(message: String, vararg args: Any)
    fun warn(message: String, throwable: Throwable)
    fun error(message: String, vararg args: Any)
    fun error(message: String, throwable: Throwable)
}

/**
 * Class that returns a wrapper class that hides the actual logger implementation
 */
object MothLoggerFactory {
        fun getLogger(clazz: Class<*>): MothLogger {
            // return logger implementation
            return MothLoggerImpl2(clazz)
        }
}

/**
 * Logger implementation that uses logback
 */
class MothLoggerImpl2(clazz: Class<*>): MothLogger {
    private val logger: Logger = LoggerFactory.getLogger(clazz)

    companion object {

        init {
            createLoggerTables()
        }

        private fun createLoggerTables() {

            val props = Properties()
            props.load(this::class.java.getResourceAsStream("/config.properties"))

            val dbDir = props.getProperty("DATABASE_DIR")
            val logdbFile = props.getProperty("LOG_DATABASE_FILE")

            val con = DriverManager.getConnection("jdbc:sqlite:$dbDir/$logdbFile")

            con.use {
                it.createStatement().use {
                    it.execute("CREATE TABLE IF NOT EXISTS logging_event(timestmp BIGINT NOT NULL, formatted_message TEXT NOT NULL, logger_name VARCHAR(254) NOT NULL, level_string VARCHAR(254) NOT NULL,    thread_name VARCHAR(254), reference_flag SMALLINT, arg0 VARCHAR(254), arg1 VARCHAR(254), arg2 VARCHAR(254), arg3 VARCHAR(254), caller_filename VARCHAR(254) NOT NULL, caller_class VARCHAR(254) NOT NULL, caller_method VARCHAR(254) NOT NULL, caller_line CHAR(4) NOT NULL, event_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT)")
                    it.execute("CREATE TABLE IF NOT EXISTS logging_event_property (event_id BIGINT NOT NULL, mapped_key VARCHAR(254) NOT NULL, mapped_value TEXT, PRIMARY KEY(event_id, mapped_key), FOREIGN KEY (event_id) REFERENCES logging_event(event_id))")
                    it.execute("CREATE TABLE IF NOT EXISTS logging_event_exception (event_id BIGINT NOT NULL, i SMALLINT NOT NULL, trace_line VARCHAR(254) NOT NULL, PRIMARY KEY(event_id, i), FOREIGN KEY (event_id) REFERENCES logging_event(event_id))")
                }
            }
        }
    }

    override fun trace(message: String, vararg args: Any) {
        logger.trace(message, args)
    }

    override fun trace(message: String, throwable: Throwable) {
        logger.trace(message, throwable)
    }

    override fun debug(message: String, vararg args: Any) {
        logger.debug(message, args)
    }

    override fun debug(message: String, throwable: Throwable) {
        logger.debug(message, throwable)
    }

    override fun info(message: String, vararg args: Any) {
        logger.info(message, args)
    }

    override fun info(message: String, throwable: Throwable) {
        logger.info(message, throwable)
    }

    override fun warn(message: String, vararg args: Any) {
        logger.warn(message, args)
    }

    override fun warn(message: String, throwable: Throwable) {
        logger.warn(message, throwable)
    }

    override fun error(message: String, vararg args: Any) {
        logger.error(message, args)
    }

    override fun error(message: String, throwable: Throwable) {
        logger.error(message, throwable)
    }
}

class MothLoggerImpl private constructor(private val database: MothDatabase): MothLogger {

    companion object {
        private var instance: MothLogger? = null
        fun getInstance(database: MothDatabase): MothLogger {
            if (instance == null) {
                instance = MothLoggerImpl(database)
            }
            return checkNotNull(instance)
        }
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    init {
        createLoggerTables()
        createTransactionTable()
    }

    private fun createLoggerTables() {

        val props = Properties()
        props.load(this::class.java.getResourceAsStream("/config.properties"))

        val dbDir = props.getProperty("DATABASE_DIR")
        val logdbFile = props.getProperty("LOG_DATABASE_FILE")

        val con = DriverManager.getConnection("jdbc:sqlite:$dbDir/$logdbFile")

        con.use {
            it.createStatement().use {
                it.execute("CREATE TABLE IF NOT EXISTS logging_event(timestmp BIGINT NOT NULL, formatted_message TEXT NOT NULL, logger_name VARCHAR(254) NOT NULL, level_string VARCHAR(254) NOT NULL,    thread_name VARCHAR(254), reference_flag SMALLINT, arg0 VARCHAR(254), arg1 VARCHAR(254), arg2 VARCHAR(254), arg3 VARCHAR(254), caller_filename VARCHAR(254) NOT NULL, caller_class VARCHAR(254) NOT NULL, caller_method VARCHAR(254) NOT NULL, caller_line CHAR(4) NOT NULL, event_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT)")
                it.execute("CREATE TABLE IF NOT EXISTS logging_event_property (event_id BIGINT NOT NULL, mapped_key VARCHAR(254) NOT NULL, mapped_value TEXT, PRIMARY KEY(event_id, mapped_key), FOREIGN KEY (event_id) REFERENCES logging_event(event_id))")
                it.execute("CREATE TABLE IF NOT EXISTS logging_event_exception (event_id BIGINT NOT NULL, i SMALLINT NOT NULL, trace_line VARCHAR(254) NOT NULL, PRIMARY KEY(event_id, i), FOREIGN KEY (event_id) REFERENCES logging_event(event_id))")
            }
        }
    }

    private fun createTransactionTable() {
        database.connectDatabase().use {
            it.createStatement().use {
                it.execute("CREATE TABLE IF NOT EXISTS `transaction`(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, datetime TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), user TEXT NOT NULL, code VARCHAR(4) NOT NULL, description TEXT)")
            }
        }
    }

    override fun trace(message: String, vararg args: Any) {
        logger.trace(message, args)
    }

    override fun trace(message: String, throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun debug(message: String, vararg args: Any) {
        logger.debug(message, args)
    }

    override fun debug(message: String, throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun info(message: String, vararg args: Any) {
        logger.info(message, args)
    }

    override fun info(message: String, throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun warn(message: String, vararg args: Any) {
        logger.warn(message, args)
    }

    override fun warn(message: String, throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun error(message: String, vararg args: Any) {
        logger.error(message, args)
    }

    override fun error(message: String, throwable: Throwable) {
        TODO("Not yet implemented")
    }

//    override fun transaction(user: String, cod: String, description: String) {
//        database.connectDatabase().use {
//            it.prepareStatement("INSERT INTO `transaction`(`user`, `code`, `description`) VALUES(?, ?, ?)").use {
//                it.setString(1, user)
//                it.setString(2, cod)
//                it.setString(3, description)
//
//                val result = it.executeUpdate()
//                check(result == 1)
//            }
//        }
//        logger.info("Transaction: User '{}' has initiated a transaction with code [{}] and description: {}", user, cod, description)
//    }
}