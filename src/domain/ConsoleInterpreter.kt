package domain

import java.lang.RuntimeException
import java.util.*
import kotlin.math.absoluteValue

class ConsoleInterpreter: AbstractInterpreter() {

    private val scanner = Scanner(System.`in`)

    override fun printCell() {
        val charToPrint = getPointedCellValue().toChar()
        print(charToPrint)
    }

    override fun readToCell() {
        val input = scanner.next().single().toInt()
        puts(input)
    }

    fun close() {
        scanner.close()
    }
}