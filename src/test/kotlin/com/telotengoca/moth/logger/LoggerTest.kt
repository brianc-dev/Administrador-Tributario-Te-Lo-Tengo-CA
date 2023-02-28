package com.telotengoca.moth.logger

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoggerTest {

    companion object {

        private lateinit var logger: Logger
        @JvmStatic
        @BeforeAll
        fun setup() {
            logger = LoggerFactory.getLogger(this::class.java)
        }
    }

    @Test
    fun `test that logger works`() {
        logger.info("Example log from {}", this::class.java.simpleName)

        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        StatusPrinter.print(context)
    }
}