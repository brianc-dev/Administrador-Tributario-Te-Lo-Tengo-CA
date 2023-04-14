package com.telotengoca.moth.utils

import java.io.File
import java.util.*

object Database {
    fun deleteDatabase() {
        val props = Properties()
        props.load(this::class.java.getResourceAsStream("/config.properties"))

        val dirName = props.getProperty("DATABASE_DIR")
        val fileName = props.getProperty("DATABASE_FILE")

        val file = File("$dirName${File.separator}$fileName")

        val result = file.delete()

        check(result)
    }
}