package domain

import java.lang.RuntimeException
import java.util.*
import kotlin.math.absoluteValue

open class Interpreter {

    protected var memory = mutableListOf(0)
    protected var negativeMemory = mutableListOf<Int>()
    protected var programPointer = 0

    private val stack = mutableListOf<Int>()

    private val scanner = Scanner(System.`in`)

    protected fun puts(value: Int) {
        if (programPointer >= 0) {
            memory[programPointer] = value
        } else {
            negativeMemory[programPointer.absoluteValue - 1] = value
        }
    }

    protected fun getPointedCellValue(): Int {
        return if (programPointer >= 0) {
            memory[programPointer]
        } else {
            negativeMemory[programPointer.absoluteValue - 1]
        }
    }

    private fun findMatchingEndWhile(program: List<Char>, startIndex: Int): Int? {
        var loopCounter = 0

        program.slice(IntRange(startIndex + 1, program.size - 1)).forEachIndexed { i, c ->
            if (c == ']') {
                if (loopCounter == 0) {
                    return  i + startIndex + 1
                }
                loopCounter--
            }
            if (c == '[') {
                loopCounter++
            }
        }

        return null
    }

    private fun incrementPointer() {
        programPointer++
        if (programPointer > 0 && programPointer == memory.size) {
            memory.add(0)
        }
    }

    private fun decrementPonter() {
        programPointer--
        if (programPointer < 0 && programPointer.absoluteValue - 1 == negativeMemory.size) {
            negativeMemory.add(0)
        }
    }

    private fun incrementCell() {
        if(programPointer >= 0) {
            if (memory[programPointer] == 255) {
                memory[programPointer] = 0
                return
            }
            memory[programPointer]++
            return
        }
        if (negativeMemory[programPointer.absoluteValue - 1] == 255) {
            negativeMemory[programPointer.absoluteValue - 1] = 0
            return
        }
        negativeMemory[programPointer.absoluteValue - 1]++
    }

    private fun decrementCell() {
        if(programPointer >= 0) {
            if (memory[programPointer] == 0) {
                memory[programPointer] = 255
                return
            }
            memory[programPointer]--
            return
        }
        if (negativeMemory[programPointer.absoluteValue - 1] == 0) {
            negativeMemory[programPointer.absoluteValue - 1] = 255
            return
        }
        negativeMemory[programPointer.absoluteValue - 1]--
    }

    private fun printCell() {
        val charToPrint = getPointedCellValue().toChar()
        print(charToPrint)
    }

    private fun readToCell() {
        val input = scanner.next().single().toInt()
        puts(input)
    }

    fun interpret(program: List<Char>) {
        interpret(program, 0)
    }

    fun interpret(program: List<Char>, entryPoint: Int) {
        program.slice(IntRange(entryPoint, program.size - 1)).forEachIndexed { i, c ->
            val realIndex = i + entryPoint

            when(c) {
                '>' -> incrementPointer()
                '<' -> decrementPonter()
                '+' -> incrementCell()
                '-' -> decrementCell()
                '.' -> printCell()
                ',' -> readToCell()
                '[' -> {
                    val currentValue = getPointedCellValue()
                    if (currentValue == 0) {
                        val indexToJump = findMatchingEndWhile(program, realIndex)
                            ?: throw RuntimeException("A open loop in index $realIndex must be closed")
                        interpret(program, indexToJump + 1)
                        return
                    }
                    stack.add(realIndex)
                }
                ']' -> {
                    if (stack.isNotEmpty()) {
                        val indexToJump = stack.removeAt(stack.size - 1)
                        interpret(program, indexToJump)
                        return
                    }
                }
            }
        }
    }

    fun reset() {
        memory = mutableListOf(0)
        negativeMemory = mutableListOf()
        programPointer = 0
    }

    fun close() {
        scanner.close()
    }
}