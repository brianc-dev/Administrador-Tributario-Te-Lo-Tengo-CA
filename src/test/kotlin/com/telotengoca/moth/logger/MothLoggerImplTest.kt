package com.telotengoca.moth.logger

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.sql.SQLException
import java.util.*

class MothLoggerImplTest {

    companion object {
        private lateinit var logger: MothLogger

        @JvmStatic
        @BeforeAll
        fun `delete prior logs`() {
            val props = Properties()
            props.load(this::class.java.getResourceAsStream("/config.properties"))

            val dbDir = props.getProperty("DATABASE_DIR")
            val logdbFile = props.getProperty("LOG_DATABASE_FILE")

            // delete log database
            File("$dbDir/$logdbFile").run {
                delete()
                assertFalse(exists())
            }

            // delete log file
            val logsDir = props.getProperty("LOG_DIR")
            val logFile = props.getProperty("LOG_FILE")

            File("$logsDir/$logFile").run {
                delete()
                assertFalse(exists())
            }

            logger = MothLoggerFactory.getLogger(MothLoggerImplTest::class.java)
        }
    }

    @Test
    fun `test that we can log`() {
        logger.info("This is a log message")
    }

    @Test
    fun `test that we can log info`() {
        logger.info("This is a log message")
    }

    @Test
    fun `test that we can log an error`() {
        logger.error("This is a error log message")
    }
    @Test
    fun `test that we can log an debug message`() {
        logger.debug("This is a debug log message")
    }

    @Test
    fun `test that we can log an warn message`() {
        logger.warn("This is a warn log message")
    }

    @Test
    fun `test that we can log an exception`() {
        try {
            throw SQLException("Database error")
        } catch (e: SQLException) {
            logger.error("A database error has happened: ", e)
        }
    }
}