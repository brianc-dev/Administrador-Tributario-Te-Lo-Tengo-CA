package com.telotengoca.moth.model

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class MothDatabaseImplTest {

    companion object {

        @AfterAll
        @JvmStatic
        fun `delete database`() {
            val props = Properties()
            props.load(this::class.java.getResourceAsStream("/config.properties"))

            val dbDir = props.getProperty("DATABASE_DIR")
            val dbFile = props.getProperty("DATABASE_FILE")

            val file = File("$dbDir/$dbFile")

            file.delete()
        }
    }

    @Test
    fun `test that database can be created`() {
        MothDatabaseImpl()
        val props = Properties()
        props.load(this::class.java.getResourceAsStream("/config.properties"))

        val dbDir = props.getProperty("DATABASE_DIR")
        val dbFile = props.getProperty("DATABASE_FILE")

        assert(File("$dbDir/$dbFile").exists())
    }

    @Test
    fun `test that connection to database can be established`() {
        val database = MothDatabaseImpl()
        val connection = database.connectDatabase()
        assertNotNull(connection)
        connection.close()
        assert(connection.isClosed)
    }
}