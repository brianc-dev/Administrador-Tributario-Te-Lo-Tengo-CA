package com.telotengoca.moth.config

object Config {
    /**
     * Configuration file name
     */
    val CONFIG_FILE = "config.properties"

    /**
     * Configuration file URL in resources
     */
    val CONFIG_FILE_URL
        get() = "/$CONFIG_FILE"
}