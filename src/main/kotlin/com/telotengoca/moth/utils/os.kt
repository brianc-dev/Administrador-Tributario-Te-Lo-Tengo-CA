package com.telotengoca.moth.utils

object OSUtils {
    enum class OS {
        WINDOWS,
        UNIX,
        MAC,
        OTHER
    }

    private val OS_NAME: OS by lazy { getOs() }

    private fun getOs(): OS {
        val osName = System.getProperty("os.name")
        return when {
            osName === null -> {
                throw ExceptionInInitializerError("Couldn't determine OS")
            }

            osName.contains("windows", true) -> {
                OS.WINDOWS
            }

            (osName.contains("linux", true)
                    || osName.contains("mpe/ix", true)
                    || osName.contains("freebsd", true)
                    || osName.contains("irix", true)
                    || osName.contains("digital unix", true)
                    || osName.contains("unix", true)) -> {
                OS.UNIX
            }

            (osName.contains("mac")) -> {
                OS.MAC
            }
            else -> OS.OTHER
        }

    }

    fun isWindows(): Boolean {
        return OS_NAME === OS.WINDOWS
    }

    fun isUnix(): Boolean {
        return OS_NAME === OS.UNIX
    }

    fun isMac(): Boolean {
        return OS_NAME === OS.MAC
    }
}