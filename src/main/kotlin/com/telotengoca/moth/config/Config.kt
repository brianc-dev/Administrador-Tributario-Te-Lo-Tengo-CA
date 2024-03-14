package com.telotengoca.moth.config

import java.util.*

/**
 * Helper to get configuration properties from config.properties file.
 */
object Config {
    val properties: Properties = Properties()
    /**
     * Configuration file name
     */
    const val CONFIG_FILE = "/config.properties"

    init {
        this::class.java.getResourceAsStream(CONFIG_FILE).use { inputStream ->
            properties.load(inputStream)
        }
    }
}