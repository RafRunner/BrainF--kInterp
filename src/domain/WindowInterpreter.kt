package domain

import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.lang.RuntimeException
import javax.swing.BorderFactory
import javax.swing.JTextArea

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
                    val safeEvent = e ?: throw RuntimeException("Key Event not recived")
                    inWindow.border = null
                    puts(safeEvent.keyChar.toInt())
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
        memoryWindow.text = ""
        negativeMemory.forEachIndexed { i, c ->
            memoryWindow.append("${-1 * (i + 1)}: $c\n")
        }
        memory.forEachIndexed { i, c ->
            memoryWindow.append("$i: $c\n")
        }
    }
}
