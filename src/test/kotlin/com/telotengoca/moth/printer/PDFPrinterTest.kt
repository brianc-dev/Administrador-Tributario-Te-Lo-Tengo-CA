package com.telotengoca.moth.printer

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PDFPrinterTest {
    @Test
    fun assertThatFileIsWritten() {
//        give
        val printer = PDFPrinter()
//        when
        val result = printer.print("randomName")
//        then
        assertTrue(result)
    }
}