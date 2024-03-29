package view

import domain.WindowInterpreter
import domain.exceptions.SyntaxErrorException
import java.awt.*
import java.lang.Exception
import javax.swing.*
import javax.swing.text.DefaultHighlighter
import javax.swing.text.PlainDocument

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
        textArea.font = Font(Font.MONOSPACED, Font.PLAIN, 12)

        panel.add(scroll, BorderLayout.CENTER)
        return panel
    }

    private fun getProgram(): CharArray = codeWindow.text.toCharArray()

    init {
        val interpreter = WindowInterpreter(outWindow, inWindow, memoryWindow)
        var currentProgramThread: Thread? = null

        val btnInterpret = JButton("Run")
        val btnDebug = JButton("Begin debugging")
        val btnStep = JButton("Next step")
        val btnEndProgram = JButton("End program")

        btnInterpret.addActionListener {
            outWindow.document = PlainDocument()
            inWindow.document = PlainDocument()
            codeWindow.isEditable = false
            codeWindow.highlighter.removeAllHighlights()

            currentProgramThread = Thread {
                try {
                    interpreter.reset()
                    interpreter.interpret(getProgram())
                    outWindow.append("\nExecution finished")
                } catch (e: InterruptedException) {
                    outWindow.append("\nExecution interrupted")
                } catch (e: SyntaxErrorException) {
                    outWindow.append("\nBad syntax! ${e.message}")
                } catch (e: Exception) {
                    outWindow.append("\nExecution interrupted by exception: ${e.message}")
                } finally {
                    interpreter.dumpMemoryToMemoryWindow()
                    codeWindow.isEditable = true
                }
            }

            currentProgramThread!!.start()
        }

        var programCounter = 0
        var programBeingDebugged = getProgram()

        btnDebug.addActionListener {
            if (currentProgramThread != null) {
                currentProgramThread!!.interrupt()
            }

            interpreter.reset()
            programCounter = 0
            programBeingDebugged = getProgram()

            btnStep.isEnabled = true
            btnDebug.isEnabled = false

            outWindow.document = PlainDocument()
            inWindow.document = PlainDocument()
            memoryWindow.document = PlainDocument()
            codeWindow.isEditable = false
            codeWindow.highlighter.addHighlight(0, 1, DefaultHighlighter.DefaultHighlightPainter(Color.RED))

            memoryWindow.append("Beginning debug...\n")
            memoryWindow.append("First instruction: ${getProgram()[0]}\n")
        }

        btnStep.addActionListener {
            Thread {
                val program = programBeingDebugged

                if (programCounter == program.size) {
                    btnStep.isEnabled = false
                    btnDebug.isEnabled = true
                    codeWindow.isEditable = true
                    codeWindow.highlighter.removeAllHighlights()

                    outWindow.append("\nExecution finished")
                }
                else {
                    val oldCounter = programCounter
                    try {
                        programCounter = interpreter.interpret(program, programCounter, true)
                    } catch (e: Exception) {
                        btnStep.isEnabled = false
                        btnDebug.isEnabled = true
                        codeWindow.isEditable = true
                        codeWindow.highlighter.removeAllHighlights()

                        when (e) {
                            is SyntaxErrorException -> outWindow.append("\nDebugging interrupted by bad syntax: ${e.message}")
                            else -> outWindow.append("\nDebugging interrupted by exception: ${e.message}")
                        }
                    } finally {
                        codeWindow.highlighter.removeAllHighlights()
                        codeWindow.highlighter.addHighlight(programCounter, programCounter + 1, DefaultHighlighter.DefaultHighlightPainter(Color.RED))
                        interpreter.dumpMemoryToMemoryWindow()

                        memoryWindow.append("\nProgram counter: $programCounter\n")
                        memoryWindow.append("Instruction executed: ${program[oldCounter]}\n")

                        if (programCounter == program.size) {
                            memoryWindow.append("\nThis was the final instruction\n")
                        } else {
                            memoryWindow.append("\nNext instruction: ${program[programCounter]}\n")
                        }
                    }
                }
            }.start()
        }

        btnEndProgram.addActionListener {
            if (currentProgramThread != null) {
                currentProgramThread!!.interrupt()
            }

            inWindow.border = null
            btnStep.isEnabled = false
            btnDebug.isEnabled = true
            codeWindow.isEditable = true
            codeWindow.highlighter.removeAllHighlights()
        }

        isVisible = true
        layout = GridBagLayout()

        outWindow.isEditable = false
        memoryWindow.isEditable = false

        btnStep.isEnabled = false

        val gb = GridBagConstraints()
        gb.anchor = GridBagConstraints.LINE_START

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
        add(JLabel("Code:"), gb)

        gb.gridx = 3
        gb.weightx = 0.3
        add(JLabel("Program memory:"), gb)

        gb.gridy = 1
        gb.gridx = 1
        gb.weighty = 1.0
        gb.weightx = 1.0
        gb.fill = GridBagConstraints.BOTH
        add(addScrollToWindow(codeWindow), gb)

        gb.gridx = 3
        gb.weightx = 0.3
        add(addScrollToWindow(memoryWindow), gb)

        gb.weighty = 0.05
        gb.gridy = 2
        gb.gridx = 1
        add(JLabel("Program output:"), gb)

        gb.gridx = 3
        add(JLabel("Program input:"), gb)

        gb.weighty = 0.5
        gb.gridy = 3
        gb.gridx = 1
        add(addScrollToWindow(outWindow), gb)
        // The output window must not wrap lines so some code output can be properly formatted
        outWindow.lineWrap = false

        gb.gridx = 3
        add(addScrollToWindow(inWindow), gb)

        val buttonsPanel = JPanel()
        buttonsPanel.layout = BoxLayout(buttonsPanel, BoxLayout.X_AXIS)

        buttonsPanel.add(btnInterpret)
        buttonsPanel.add(btnDebug)
        buttonsPanel.add(btnStep)

        gb.weighty = 0.1
        gb.fill = GridBagConstraints.NONE
        gb.gridy = 4
        gb.gridx = 1
        add(buttonsPanel, gb)

        gb.gridx = 3
        add(btnEndProgram, gb)
    }
}
