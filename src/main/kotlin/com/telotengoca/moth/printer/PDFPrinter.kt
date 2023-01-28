package com.telotengoca.moth.printer

import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.util.stream.Stream

class PDFPrinter : Printer {
    companion object {
        private val DEFAULT_TARGET_DIR: String by lazy {
            System.getProperty("user.home") + File.separator + "Documents" + File.separator
        }
    }

    override fun print(name: String): Boolean {
        val pageSize = PageSize.getRectangle("LETTER")
        val newPageSize = Rectangle(pageSize.width, pageSize.height / 3)
        val document = Document(newPageSize)

        PdfWriter.getInstance(document, FileOutputStream("${DEFAULT_TARGET_DIR}${name}.pdf"))
        document.open()

        val table = PdfPTable(3)
        Stream.of("Column header 1", "Column header 2", "Column header 3")
            .forEach {
                val header = PdfPCell()
                header.backgroundColor = BaseColor.LIGHT_GRAY
                header.borderWidth = 2f
                header.phrase = Phrase(it)
                table.addCell(header)
            }

        table.addCell("row 1, col 1");
        table.addCell("row 1, col 2");
        table.addCell("row 1, col 3");

        document.add(table)


        document.close()

        return true
    }

    private fun formatPDF(document: Document) {

    }
}