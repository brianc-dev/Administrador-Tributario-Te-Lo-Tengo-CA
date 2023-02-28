package com.telotengoca.moth.logger

import com.telotengoca.moth.model.MothDatabase
import com.telotengoca.moth.model.MothDatabaseImpl
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class MothLoggerImplTest {

    companion object {
        private lateinit var logger: MothLogger

        @JvmStatic
        @BeforeAll
        fun `setup db`() {
            val props = Properties()
            props.load(this::class.java.getResourceAsStream("/config.properties"))

            val dbDir = props.getProperty("DATABASE_DIR")
            val logdbFile = props.getProperty("LOG_DATABASE_FILE")

            File("$dbDir/$logdbFile").run {
                delete()
                assertFalse(exists())
            }

            val logsDir = props.getProperty("LOG_DIR")
            val logFile = props.getProperty("LOG_FILE")

            File("$logsDir/$logFile").run {
                delete()
                assertFalse(exists())
            }

            val database: MothDatabase = MothDatabaseImpl()
            logger = MothLoggerImpl.getInstance(database)
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
}