package domain

import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.lang.RuntimeException
import java.lang.StringBuilder
import javax.swing.BorderFactory
import javax.swing.JTextArea
import javax.swing.text.PlainDocument

class WindowInterpreter(private val outWindow: JTextArea, private val inWindow: JTextArea, private val memoryWindow:JTextArea) : AbstractInterpreter() {

    override fun printCell() {
        val charToPrint = getPointedCellValue().toChar().toString()
        outWindow.append(charToPrint)
    }

    override fun readToCell() {
        val lock = Object()

        inWindow.border = BorderFactory.createTitledBorder("waiting for input")

        val waitForKey = object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}

            override fun keyPressed(e: KeyEvent?) {
                synchronized(lock) {
                    val safeEvent = e ?: throw RuntimeException("Key Event not received")
                    val asciiValue = safeEvent.keyChar.toInt()

                    if (asciiValue < 0 || asciiValue > 255) {
                        return
                    }

                    inWindow.border = null
                    puts(asciiValue)
                    lock.notifyAll()
                }
            }

            override fun keyReleased(e: KeyEvent?) {}
        }

        synchronized(lock) {
            dumpMemoryToMemoryWindow()
            inWindow.addKeyListener(waitForKey)
            lock.wait()
            inWindow.removeKeyListener(waitForKey)
        }
    }

    fun dumpMemoryToMemoryWindow() {
        val memoryOutput = StringBuilder()
        var ni = negativeMemory.size
        while (ni > 0) {
            ni--
            memoryOutput.append("${-1 * (ni + 1)}: ${negativeMemory[ni]}\n")
        }
        memory.forEachIndexed { i, c ->
            memoryOutput.append("$i: $c\n")
        }
        memoryOutput.append("\nMemory pointer: $memoryPointer\n")
        memoryOutput.append("Value pointed: ${getPointedCellValue()}\n")

        memoryWindow.document = PlainDocument()
        memoryWindow.text = memoryOutput.toString()
    }
}
