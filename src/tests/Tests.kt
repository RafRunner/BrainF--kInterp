package tests

import domain.ConsoleInterpreter

fun main() {
   val interpreter = ConsoleInterpreter()
    // Hello world
    interpreter.interpret("++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.".toCharArray().toList())
    interpreter.reset()
    // Thruth machine
    interpreter.interpret(",------------------------------------------------>++++++++++++++++++++++++++++++++++++++++++++++++<[>+[.]]>.".toCharArray().toList())

    interpreter.close()
}
