package view

import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel

class MainWindow (panel: JPanel) {

    private val width = 800
    private val height = 600

    private val window: JFrame = JFrame("Brainfuck Interpreter")

    init {
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        window.setLocation(500, 300)
        window.preferredSize = Dimension(width, height)
        window.isVisible = true
        window.isResizable = false
        window.add(panel)
        window.pack()
    }
}
