package domain

import java.util.*

class ConsoleInterpreter: AbstractInterpreter() {

    private val scanner = Scanner(System.`in`)

    override fun printCell() {
        val charToPrint = getPointedCellValue().toChar()
        print(charToPrint)
    }

    override fun readToCell() {
        val input = scanner.next().single().toInt()

        if (input < 0 || input > 255) {
            return
        }

        puts(input)
    }

    fun close() {
        scanner.close()
    }
}
