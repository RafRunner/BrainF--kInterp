package domain

import domain.exceptions.SyntaxErrorException
import java.util.ArrayDeque
import kotlin.math.absoluteValue

abstract class AbstractInterpreter {

    protected var memory = mutableListOf(0)
    protected var negativeMemory = mutableListOf<Int>()
    protected var memoryPointer = 0

    // The callstack (addresses of '['s so the program knows where to jump from a ']')
    private var stack = ArrayDeque<Int>()
    // A cache so the program doesn't have to find matching ']'s everytime
    private var matchingEndWhileCache = mutableMapOf<Int, Int>()

    private fun findMatchingEndWhile(program: CharArray, startIndex: Int): Int? {
        val valueInCache = matchingEndWhileCache[startIndex]
        if (valueInCache != null) {
            return valueInCache
        }

        var loopCounter = 0

        program.slice(IntRange(startIndex + 1, program.size - 1)).forEachIndexed { i, c ->
            if (c == ']') {
                if (loopCounter == 0) {
                    matchingEndWhileCache[startIndex] = i + startIndex + 1
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
        memoryPointer++
        if (memoryPointer > 0 && memoryPointer == memory.size) {
            memory.add(0)
        }
    }

    private fun decrementPointer() {
        memoryPointer--
        if (memoryPointer < 0 && memoryPointer.absoluteValue - 1 == negativeMemory.size) {
            negativeMemory.add(0)
        }
    }

    private fun incrementCell() {
        val currentValue = getPointedCellValue()

        if (currentValue == 255) {
            puts(0)
        } else {
            puts(currentValue + 1)
        }
    }

    private fun decrementCell() {
        val currentValue = getPointedCellValue()

        if (currentValue == 0) {
            puts(255)
        } else {
            puts(currentValue - 1)
        }
    }

    protected abstract fun printCell()

    protected abstract fun readToCell()

    protected fun puts(value: Int) {
        if (memoryPointer >= 0) {
            memory[memoryPointer] = value
        } else {
            negativeMemory[memoryPointer.absoluteValue - 1] = value
        }
    }

    protected fun getPointedCellValue(): Int {
        return if (memoryPointer >= 0) {
            memory[memoryPointer]
        } else {
            negativeMemory[memoryPointer.absoluteValue - 1]
        }
    }

    fun interpret(program: String) {
        interpret(program.toCharArray())
    }

    fun interpret(program: CharArray) {
        interpret(program, 0, false)
    }

    fun interpret(program: CharArray, entryPoint: Int, debugging: Boolean): Int {
        val endPoint = if (debugging) entryPoint else program.size - 1
        var index = entryPoint

        loop@ while (index <= endPoint) {
            when(program[index]) {
                '>' -> incrementPointer()
                '<' -> decrementPointer()
                '+' -> incrementCell()
                '-' -> decrementCell()
                '.' -> printCell()
                ',' -> readToCell()
                '[' -> {
                    val indexToJump = findMatchingEndWhile(program, index)
                        ?: throw SyntaxErrorException("A open loop '[' in index $index must be closed")
                    if (getPointedCellValue() == 0) {
                        index = indexToJump + 1
                        if (debugging) {
                            break@loop
                        }
                        continue@loop
                    }
                    stack.push(index)
                }
                ']' -> {
                    if (stack.isEmpty()) {
                        throw SyntaxErrorException("A closing loop ']' in index $index has no '[' to return")
                    }
                    val indexToJump = stack.pop()
                    if (getPointedCellValue() != 0) {
                        index = indexToJump
                        if (debugging) {
                            break@loop
                        }
                        continue@loop
                    }
                }
            }
            index++
        }
        return index
    }

    fun reset() {
        memory = mutableListOf(0)
        negativeMemory = mutableListOf()
        memoryPointer = 0
        stack = ArrayDeque()
        matchingEndWhileCache = mutableMapOf()
    }
}
