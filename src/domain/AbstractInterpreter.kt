package domain

import domain.exceptions.SyntaxErrorException
import kotlin.math.absoluteValue

abstract class AbstractInterpreter {

    protected var memory = mutableListOf(0)
    protected var negativeMemory = mutableListOf<Int>()
    protected var memoryPointer = 0

    private val stack = mutableListOf<Int>()

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
        memoryPointer++
        if (memoryPointer > 0 && memoryPointer == memory.size) {
            memory.add(0)
        }
    }

    private fun decrementPonter() {
        memoryPointer--
        if (memoryPointer < 0 && memoryPointer.absoluteValue - 1 == negativeMemory.size) {
            negativeMemory.add(0)
        }
    }

    private fun incrementCell() {
        if (memoryPointer >= 0) {
            if (memory[memoryPointer] == 255) {
                memory[memoryPointer] = 0
                return
            }
            memory[memoryPointer]++
            return
        }
        if (negativeMemory[memoryPointer.absoluteValue - 1] == 255) {
            negativeMemory[memoryPointer.absoluteValue - 1] = 0
            return
        }
        negativeMemory[memoryPointer.absoluteValue - 1]++
    }

    private fun decrementCell() {
        if (memoryPointer >= 0) {
            if (memory[memoryPointer] == 0) {
                memory[memoryPointer] = 255
                return
            }
            memory[memoryPointer]--
            return
        }
        if (negativeMemory[memoryPointer.absoluteValue - 1] == 0) {
            negativeMemory[memoryPointer.absoluteValue - 1] = 255
            return
        }
        negativeMemory[memoryPointer.absoluteValue - 1]--
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

    fun interpret(program: List<Char>) {
        interpret(program, 0, false)
    }

    fun interpret(program: List<Char>, entryPoint: Int, debugging: Boolean): Int {
        val endPoit = if (debugging) entryPoint else program.size - 1
        program.slice(IntRange(entryPoint, endPoit)).forEachIndexed { i, c ->
            val realIndex = i + entryPoint

            when(c) {
                '>' -> incrementPointer()
                '<' -> decrementPonter()
                '+' -> incrementCell()
                '-' -> decrementCell()
                '.' -> printCell()
                ',' -> readToCell()
                '[' -> {
                    val indexToJump = findMatchingEndWhile(program, realIndex)
                        ?: throw SyntaxErrorException("A open loop '[' in index $realIndex must be closed")
                    if (getPointedCellValue() == 0) {
                        if (!debugging) {
                            interpret(program, indexToJump + 1, false)
                        }
                        return indexToJump + 1
                    }
                    stack.add(realIndex)
                }
                ']' -> {
                    if (stack.isEmpty()) {
                        throw SyntaxErrorException("A closing loop ']' in index $realIndex has no '[' to return")
                    }
                    val indexToJump = stack.removeAt(stack.size - 1)
                    if (getPointedCellValue() != 0) {
                        if (!debugging) {
                            interpret(program, indexToJump, false)
                        }
                        return indexToJump
                    }
                }
            }
        }
        return entryPoint + 1
    }

    fun reset() {
        memory = mutableListOf(0)
        negativeMemory = mutableListOf()
        memoryPointer = 0
    }
}
