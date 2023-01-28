package com.telotengoca.moth.model

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File

class MothDatabaseImplTest {

    @Test
    fun `test that database can be created`() {
        val database = MothDatabaseImpl()
        database.createDatabase()
        println(File("database/moth.sqlite").absolutePath)
        assert(File("database/moth.sqlite").exists())
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