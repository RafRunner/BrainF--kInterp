package view

import domain.WindowInterpreter
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*


class InterpreterView : JPanel() {

    private val codeWindow = JTextArea()
    private val outWindow = JTextArea()
    private val inWindow = JTextArea()
    private val memoryWindow = JTextArea()

    private fun addScrollToWindow(textArea: JTextArea): JPanel {
        val panel = JPanel()
        panel.layout = BorderLayout()

        val scroll = JScrollPane(textArea)
        textArea.lineWrap = true

        panel.add(scroll, BorderLayout.CENTER)
        return panel
    }

    init {
        val interpreter = WindowInterpreter(outWindow, inWindow, memoryWindow)
        var curentProgramThread: Thread? = null

        isVisible = true

        val btnInterpret = JButton("Interpret")
        val btnEndProgram = JButton("End program")

        btnInterpret.addActionListener {
            outWindow.text = ""
            inWindow.text = ""
            curentProgramThread = Thread(Runnable {
                try {
                    interpreter.interpret(codeWindow.text.toCharArray().toList())
                    outWindow.append("\nExecution finished")
                } catch (e: StackOverflowError) {
                    outWindow.append("\nExecution interrupted by stack overflow")
                } catch (e: InterruptedException) {
                    outWindow.append("\nExecution interrupted")
                } finally {
                    interpreter.dumpMemoryToMemoryWindow()
                    interpreter.reset()
                }
            })
            curentProgramThread!!.start()
        }

        btnEndProgram.addActionListener {
            if (curentProgramThread != null) {
                curentProgramThread!!.interrupt()
                interpreter.dumpMemoryToMemoryWindow()
                inWindow.border = null
                interpreter.reset()
            }
        }

        layout = GridBagLayout()

        outWindow.isEditable = false

        val gb = GridBagConstraints()
        gb.anchor = GridBagConstraints.LINE_START
        gb.weightx = 1.0
        gb.weighty = 1.0

        gb.gridx = 0
        gb.gridy = 0
        gb.weightx = 0.2
        gb.weighty = 0.05
        add(JLabel(), gb)

        gb.gridx = 2
        add(JLabel(), gb)

        gb.gridx = 4
        add(JLabel(), gb)

        gb.gridx = 1
        gb.weightx = 1.0
        add(JLabel("Enter the code here:"), gb)

        gb.gridx = 3
        gb.weightx = 0.2
        add(JLabel("Program memory:"), gb)

        gb.gridy = 1
        gb.gridx = 1
        gb.weighty = 1.0
        gb.fill = GridBagConstraints.BOTH
        add(addScrollToWindow(codeWindow), gb)

        gb.gridx = 3
        gb.weightx = 0.2
        add(addScrollToWindow(memoryWindow), gb)

        gb.weighty = 0.05
        gb.gridy = 2
        gb.gridx = 1
        add(JLabel("Program output:"), gb)

        gb.gridx = 3
        add(JLabel("Program input:"), gb)

        gb.weighty = 0.3
        gb.gridy = 3
        gb.gridx = 1
        add(addScrollToWindow(outWindow), gb)

        gb.gridx = 3
        add(addScrollToWindow(inWindow), gb)

        gb.weighty = 0.1
        gb.fill = GridBagConstraints.NONE
        gb.gridy = 4
        gb.gridx = 1
        add(btnInterpret, gb)

        gb.gridx = 3
        add(btnEndProgram, gb)
    }
}