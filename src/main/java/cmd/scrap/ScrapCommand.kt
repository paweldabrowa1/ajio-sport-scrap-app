package org.example.cmd.scrap

import cmd.Command
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.example.db.map.MapTableDao
import org.example.db.map.PlayingField
import java.util.regex.Matcher
import java.util.regex.Pattern

class ScrapCommand(mapTable: MapTableDao) : Command("scrap", mapTable) {

    override fun onSetup(options: Options) {
        val o = Option(
            "f", "file",
            true, "scrapping pdf path"
        )
        o.isRequired = true
        options.addOption(o)
        val o2 = Option(
            "r", "regexp",
            true, "scrapping regexp (with params CITY, STREET, POSTAL_CODE"
        )
        o2.isRequired = true
        options.addOption(o2)
    }

    abstract class ScrapParam(
        val name: String,
        var indexAt: Int = -1,
        var index: Int = 0,
    ) {
        abstract fun fill(field: PlayingField, data: String)

        override fun toString(): String {
            return "$name $indexAt $index"
        }
    }

    class CitySP: ScrapParam("CITY") {
        override fun fill(field: PlayingField, data: String) {
            field.city = data
        }
    }

    class StreetSP: ScrapParam("STREET") {
        override fun fill(field: PlayingField, data: String) {
            field.street = data
        }
    }

    class PostalCodeSP: ScrapParam("POSTAL_CODE") {
        override fun fill(field: PlayingField, data: String) {
            field.postalCode = data
        }
    }

    override fun onExecute(cmd: CommandLine) {
        val pdfPath = cmd.getOptionValue("file")
        val pdfContent = extractData(pdfPath)
        var regexp = cmd.getOptionValue("regexp")

        val scrapParams = arrayListOf(
            CitySP(), StreetSP(), PostalCodeSP()
        )
        for (scrapParam in scrapParams) {
            scrapParam.indexAt = regexp.indexOf(scrapParam.name)
        }
        val sortedParams = scrapParams.sortedBy { it.indexAt }.filter { scrapParam -> scrapParam.indexAt != -1 }
        for ((index, sortedParam) in sortedParams.withIndex()) {
            sortedParam.index = index
            regexp = regexp.replace(sortedParam.name, "(.*)")
        }

        val pattern = Pattern.compile(regexp, Pattern.DOTALL)

        pdfContent.lines().forEach {
            val field = PlayingField(
                "-",
                "",
                "",
                "",
                0.0,
                0.0,
                "",
                it
            )

            val m: Matcher = pattern.matcher(it)
            if (m.find()) {
                for (scrapParam in sortedParams) {
                    scrapParam.fill(field, m.group(scrapParam.index + 1))
                }

                mapTable.insertField(field)
            } else {
//                field.error = "NO_REGEX_APPLIED"
            }
        }
    }

    private fun extractData(pdfPath: String): String {
        try {
            var extractedText = ""

            val pdfReader = PdfReader(pdfPath)
            val n = pdfReader.numberOfPages

            for (i in 0 until n) {
                extractedText =
                    """
                 $extractedText${
                        PdfTextExtractor.getTextFromPage(pdfReader, i + 1).trim { it <= ' ' }
                    }
                  
                 """.trimIndent()
            }
            pdfReader.close()

            return extractedText
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }
}