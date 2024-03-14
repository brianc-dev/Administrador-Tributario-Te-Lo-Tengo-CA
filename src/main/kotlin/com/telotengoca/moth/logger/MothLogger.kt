package com.telotengoca.moth.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
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
            return MothLoggerImpl(clazz)
        }
}

/**
 * Logger implementation that uses logback
 */
class MothLoggerImpl(clazz: Class<*>): MothLogger {
    private val logger: Logger = LoggerFactory.getLogger(clazz)

    companion object {

        init {
            createLoggerDatabase()
            createLoggerDir()
        }

        private fun createLoggerDir() {
            val props = Properties()
            props.load(this::class.java.getResourceAsStream("/config.properties"))

            val logDir = props.getProperty("LOG_DIR")
            val logFile = props.getProperty("LOG_FILE")

            File(logDir).also {
                if (!it.exists()) it.mkdir()
            }

            File("$logDir/$logFile").also {
                if (!it.exists()) it.createNewFile()
            }
        }

        private fun createLoggerDatabase() {

            val props = Properties()
            props.load(this::class.java.getResourceAsStream("/config.properties"))

            val dbDir = props.getProperty("DATABASE_DIR")
            val logDbFile = props.getProperty("LOG_DATABASE_FILE")

            File(dbDir).also {
                if (!it.exists()) it.mkdir()
            }

            File("$dbDir/$logDbFile").also {
                if (!it.exists()) it.createNewFile()
            }

            val con = DriverManager.getConnection("jdbc:sqlite:$dbDir/$logDbFile")

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
        if (!logger.isTraceEnabled) return
        logger.trace(message, args)
    }

    override fun trace(message: String, throwable: Throwable) {
        if (!logger.isTraceEnabled) return
        logger.trace(message, throwable)
    }

    override fun debug(message: String, vararg args: Any) {
        if (!logger.isDebugEnabled) return
        logger.debug(message, args)
    }

    override fun debug(message: String, throwable: Throwable) {
        if (!logger.isDebugEnabled) return
        logger.debug(message, throwable)
    }

    override fun info(message: String, vararg args: Any) {
        if (!logger.isInfoEnabled) return
        logger.info(message, args)
    }

    override fun info(message: String, throwable: Throwable) {
        if (!logger.isInfoEnabled) return
        logger.info(message, throwable)
    }

    override fun warn(message: String, vararg args: Any) {
        if (!logger.isWarnEnabled) return
        logger.warn(message, args)
    }

    override fun warn(message: String, throwable: Throwable) {
        if (!logger.isWarnEnabled) return
        logger.warn(message, throwable)
    }

    override fun error(message: String, vararg args: Any) {
        if (!logger.isErrorEnabled) return
        logger.error(message, args)
    }

    override fun error(message: String, throwable: Throwable) {
        if (!logger.isErrorEnabled) return
        logger.error(message, throwable)
    }
}