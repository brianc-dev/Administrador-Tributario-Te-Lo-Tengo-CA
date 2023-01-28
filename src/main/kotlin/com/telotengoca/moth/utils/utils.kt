package com.telotengoca.moth.utils

import java.security.SecureRandom

object IDUtils {
    /**
     * Generates a random id of given [length]
     * This function relies on SecureRandom().
     * @return the generated id
     */
    fun generateRandomId(length: Int): String {
        val id = SecureRandom().ints(48, 122 + 1).filter{
            (it <= 57 || it >= 65) && (it <= 90 || it >= 97)
        }.limit(length.toLong())
            .collect(::StringBuilder, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString()
        return id
    }
}

object HexUtils {
    /**
     * Converts a ByteArray into a string
     */
    fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (h in hash) {
            val hex = Integer.toHexString(0xff and h.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}