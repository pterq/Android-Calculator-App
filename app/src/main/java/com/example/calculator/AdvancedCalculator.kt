package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Stack
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class AdvancedCalculator : AppCompatActivity() {
    private lateinit var inputExpression: TextView
    private lateinit var resultDisplay: TextView

    // zmienne do przywrócenia treści
    private var currentExpression: String = ""
    private var currentResult: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_calculator)

        inputExpression = findViewById(R.id.inputExpression)
        resultDisplay = findViewById(R.id.resultDisplay)

        // przywrócenie zapisanego stanu, jeśli jest
        if (savedInstanceState != null) {
            currentExpression = savedInstanceState.getString("inputExpression", "")
            currentResult = savedInstanceState.getString("resultDisplay", "")
            inputExpression.text = currentExpression
            resultDisplay.text = currentResult
        }

        val buttons = listOf(
            Pair(R.id.btn0, "0"), Pair(R.id.btn1, "1"), Pair(R.id.btn2, "2"),
            Pair(R.id.btn3, "3"), Pair(R.id.btn4, "4"), Pair(R.id.btn5, "5"),
            Pair(R.id.btn6, "6"), Pair(R.id.btn7, "7"), Pair(R.id.btn8, "8"),
            Pair(R.id.btn9, "9"), Pair(R.id.btnDot, "."), Pair(R.id.btnAdd, "+"),
            Pair(R.id.btnSubtract, "-"), Pair(R.id.btnMultiply, "*"), Pair(R.id.btnDivide, "/"),
            Pair(R.id.btnOpenPar, "("), Pair(R.id.btnClosePar, ")"), Pair(R.id.btnPower, "^"),
            Pair(R.id.btnSin, "sin("), Pair(R.id.btnCos, "cos("), Pair(R.id.btnTan, "tan("),
            Pair(R.id.btnLn, "ln("), Pair(R.id.btnSqrt, "sqrt("), Pair(R.id.btnSquare, "^2"),
            Pair(R.id.btnLog, "log(")
        )

        //obsługa przyczisków funkcji, nawiasów, liczb
        buttons.forEach { (id, value) ->
            val button = findViewById<Button>(id)

            button?.setOnClickListener {
                inputExpression.append(value)
                //calculate(inputExpression.text.toString())
            }
        }

        //obsługa przycisku AC
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            inputExpression.text = ""
            resultDisplay.text = ""
        }

        //obsługa prczycisku =
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            calculate(inputExpression.text.toString())
        }

        //obsługa prczycisku Delete C
        val doubleClickThreshold = 300L
        var lastClickTime = 0L
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val expression = deleteLastCharacter(inputExpression.text.toString())

            //sprawdzenie czy podwójny klik
            if (currentTime - lastClickTime < doubleClickThreshold) {
                inputExpression.text = ""
                resultDisplay.text = ""
            } else {
                // pojedyńczy klik
                inputExpression.text = expression
                //calculate(expression)

                if (expression.isEmpty()) resultDisplay.text = ""
            }
            lastClickTime = currentTime
        }

        //przycisk zmiany znaku +/-
        findViewById<Button>(R.id.btnChangeSign).setOnClickListener {
            var expression = inputExpression.text.toString()
            try {
                expression = changeSign(expression)
                inputExpression.text = expression
            } catch (e: Exception) {
                resultDisplay.text = "Error: ${e.message}"
            }

            calculate(expression)
        }

        //przycisk %
        findViewById<Button>(R.id.btnPercent).setOnClickListener {
            calculate(inputExpression.text.toString())
            var expression = resultDisplay.text.toString().replace("=", "")

            turnToPercent(expression)
        }
    }

    // uruchamianie przeliczania równania na wynik
    private fun calculate(expression: String){
        try {
            inputExpression.text = removeLeadingZero(expression)
            val tokenized = tokenizeExpression(expression)
            val rpn = infixToRPN(tokenized)
            val result = evaluateRPN(rpn)
            resultDisplay.text = "=$result"
        } catch (e: Exception) {
            resultDisplay.text = "Error: ${e.message}"
        }
    }

    // usuwanie 0 z widoku jeśli po nim jest jakas liczba
    private fun removeLeadingZero(input: String): String {
        return if (input.startsWith("0") && input.length > 1 && input[1].isDigit()) {
            // usunięcie początkowego '0' i zwrot reszty
            return input.substring(1)
        } else {
            //
            return input
        }
    }

    // funkcja czyszczenia
    private fun deleteLastCharacter(expression: String): String {
        return if (expression.isNotEmpty()) expression.dropLast(1) else ""

    }

    // funkcja zmiany znaku +/-
    private fun changeSign(expression: String): String {
        if (expression.isEmpty()) {
            throw IllegalArgumentException("Expression is empty")
        }

        val modifiedExpression = StringBuilder(expression.trim())
        var i = modifiedExpression.length - 1

        var operators = listOf('+', '-', '*', '/', '(', '^')
        var countOp = modifiedExpression.count { it in operators }
        var countMinus = modifiedExpression.count { it in listOf('-') }

        // jednoznakowa liczba na początku, np. "5" //
        if (modifiedExpression[0].isDigit() && modifiedExpression.length == 1) {
            println("przyp 5")
            if (modifiedExpression[0] != '0') {
                modifiedExpression.insert(0, "-")
            }
            return modifiedExpression.toString()
        }
        // wieloznakowa liczba na początku, np. "1111"
        if(modifiedExpression[0].isDigit() && countOp == 0){
            println("przyp 6")
            if (modifiedExpression[0] != '0') {
                modifiedExpression.insert(0, "-")
            }
            return modifiedExpression.toString()
        }

        while (i >= 0) {
            if (modifiedExpression[i] in listOf('+', '-', '*', '/', '(', '^')) {
                when {
                    // Jeśli minus na początku
                    i == 0 && modifiedExpression[i] == '-' -> {
                        println("przyp 1")
                        modifiedExpression.deleteCharAt(i)
                        break
                    }
                    // Jeśli minus przed nawiasem
                    i > 0 && modifiedExpression[i] == '-' && modifiedExpression[i - 1] == '(' -> {
                        println("przyp 2")
                        modifiedExpression.deleteCharAt(i)
                        break
                    }
                    // Liczba w nawiasach
                    i < modifiedExpression.length - 1 && modifiedExpression[i] == '(' -> {
                        // Znaleźliśmy nawias otwierający, sprawdzamy, czy za nawiasem jest liczba
                        println("przyp 3")
                        var j = i + 2
                        while (j < modifiedExpression.length && (modifiedExpression[j].isDigit() || modifiedExpression[j] == '.')) {
                            j++
                        }
                        // Sprawdzamy, czy po nawiasie jest liczba
                        if (j < modifiedExpression.length && modifiedExpression[j] == ')') {
                            // Jeśli liczba w nawiasach
                            //modifiedExpression.insert(j, ')')
                            modifiedExpression.insert(i + 1, "-")
                        }
                        break
                    }
                    // Liczba po operatorze
                    i < modifiedExpression.length - 1 && modifiedExpression[i + 1].isDigit() -> {
                        println("przyp 4")
                        var j = i + 1
                        while (j < modifiedExpression.length && (modifiedExpression[j].isDigit() || modifiedExpression[j] == '.')) {
                            j++
                        }
                        val number = modifiedExpression.substring(i + 1, j)
                        if (number.toDoubleOrNull() != null && number != "0") {
                            modifiedExpression.insert(j, ')')
                            modifiedExpression.insert(i + 1, "(-")
                        }
                        break
                    }
                }
            }
            i--
        }
        return modifiedExpression.toString()
    }

    // funkcja %
    private fun turnToPercent(expression: String): String{
        var resultString = ""
        try {
            var data = expression.toDoubleOrNull()
            if (data != null) {

                val result = data * 100
                resultString = String.format("%.2f", result)

                inputExpression.text = ""
                resultDisplay.text = "${resultString}%"
            } else {
                resultDisplay.text = "Error"
            }
        } catch (e: Exception) {
            resultDisplay.text = "Error: ${e.message}"
        }
        return resultString
    }

    // zamiana równania na tokeny
    private fun tokenizeExpression(expression: String): String {
        val tokens = mutableListOf<String>()
        val operators = setOf('+', '-', '*', '/', '^', '(', ')')
        val functions = setOf("sin", "cos", "tan", "ln", "sqrt", "log")
        var numberBuffer = StringBuilder()

        //inputExpression.text= "nowy ${expression}"

        var i = 0
        while (i < expression.length) {
            val char = expression[i]

            if (char.isLetter()) {
                val function = functions.find { expression.startsWith(it, i) }
                if (function != null) {
                    tokens.add(function)
                    i += function.length - 1
                } else {
                    throw IllegalArgumentException("Unknown function at $i")
                }
            } else if (char.isDigit() || char == '.') {
                numberBuffer.append(char)
            }
            // Obsługa znaku minus (-), gdy jest częścią liczby
            else if (char in operators) {
                if (numberBuffer.isNotEmpty()) {
                    tokens.add(numberBuffer.toString())
                    numberBuffer.clear()
                }
                tokens.add(char.toString())
            } else if (char == ' ') {
                // Ignore spaces
            } else {
                throw IllegalArgumentException("Unknown character: $char")
            }
            i++
        }

        if (numberBuffer.isNotEmpty()) {
            tokens.add(numberBuffer.toString())
        }

        //inputExpression.text= "nowy ${tokens}"
        return tokens.joinToString(" ")
    }

    // zmiana tokenów w notacji infixowe do tokenów w odwrotnej notacji polskie
    private fun infixToRPN(expression: String): String {
        val precedence = mapOf(
            "^" to 4,
            "*" to 3,
            "/" to 3,
            "+" to 2,
            "-" to 2,
            "(" to 1
        )

        val output = mutableListOf<String>()
        val operators = Stack<String>()
        val functions = setOf("sin", "cos", "tan", "ln", "sqrt", "log")

        val tokens = expression.split(" ")

        for (i in tokens.indices) {
            val token = tokens[i]

            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token in functions -> operators.push(token)
                token == "(" -> operators.push(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        output.add(operators.pop())
                    }
                    if (operators.isEmpty() || operators.pop() != "(") {
                        throw IllegalArgumentException("Mismatched parentheses")
                    }
                    if (operators.isNotEmpty() && operators.peek() in functions) {
                        output.add(operators.pop())
                    }
                }
                token in precedence.keys -> {
                    if (token == "-" && (i == 0 || tokens[i - 1] in precedence.keys || tokens[i - 1] == "(")) {
                        output.add("0") // Add a zero before negative number
                    }
                    while (operators.isNotEmpty() && precedence[operators.peek()]!! >= precedence[token]!!) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }
                else -> throw IllegalArgumentException("Unknown token: $token")
            }
        }

        while (operators.isNotEmpty()) {
            output.add(operators.pop())
        }

        return output.joinToString(" ")
    }

    // liczenie
    private fun evaluateRPN(expression: String): Double {
        val stack = Stack<Double>()
        val tokens = expression.split(" ")

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> stack.push(token.toDouble())
                token in listOf("+", "-", "*", "/", "^") -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> if (b != 0.0) a / b else throw ArithmeticException("Division by zero")
                        "^" -> Math.pow(a, b)
                        else -> throw IllegalArgumentException("Unknown operator: $token")
                    }
                    stack.push(result)
                }

                token in listOf("sin", "cos", "tan", "ln", "sqrt", "log") -> {
                    val a = stack.pop()
                    val result = when (token) {
                        "sin" -> sin(a/180*Math.PI)
                        "cos" -> cos(Math.toRadians(a))
                        "tan" -> tan(Math.toRadians(a))
                        "ln" -> if (a > 0) ln(a) else throw ArithmeticException("ln(x) for x <= 0")
                        "sqrt" -> if (a >= 0) sqrt(a) else throw ArithmeticException("sqrt(x) for x < 0")
                        "log" -> if (a > 0) log10(a) else throw ArithmeticException("log(x) for x <= 0")
                        else -> throw IllegalArgumentException("Unknown function: $token")
                    }
                    stack.push(result)
                }

                else -> throw IllegalArgumentException("Unknown token: $token")
            }
        }

        if (stack.size != 1) {
            throw IllegalStateException("Invalid RPN expression")
        }

        return stack.pop()
    }

    // zapisanie stanu
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("inputExpression", inputExpression.text.toString())
        outState.putString("resultDisplay", resultDisplay.text.toString())
    }
}

