package tests

import domain.Interpreter

fun main() {
   val interpreter = Interpreter()
    // Hello world
    interpreter.interpret("++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.".toCharArray().toList())
    interpreter.reset()
    // Thruth machine
    interpreter.interpret(",------------------------------------------------>++++++++++++++++++++++++++++++++++++++++++++++++<[>+[.]]>.".toCharArray().toList())

    interpreter.close()
}
