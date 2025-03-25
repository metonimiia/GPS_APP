package com.example.gps_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var AddOperation = false
    private var AddTochka = true
    private lateinit var workingTV: AppCompatTextView
    private lateinit var resultsTV: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        workingTV = findViewById(R.id.workingTV)
        resultsTV = findViewById(R.id.resultsTV)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val button = findViewById<Button>(R.id.mp3player)
        button.setOnClickListener {
            val intent = Intent(this, mp3player::class.java)
            startActivity(intent)
        }
    }
    fun numberAction(view: View)//Функция чисел
    {
        if(view is Button)
        {
            if(view.text == ".")
            {
                if(AddTochka) {
                    workingTV.append(view.text)
                }
                AddTochka = false//чтобы не поставить точку 2 и больше раз
            }
            else
                workingTV.append(view.text)
            AddOperation = true
        }
    }
    fun operationAction(view: View)//функция операций
    {
        if(view is Button && AddOperation)
        {
            workingTV.append(view.text)
            AddOperation = false
            AddTochka = true
        }
    }
    fun clearAction(view: View)//функция очистки
    {
        workingTV.text = ""
        resultsTV.text = "0"
    }
    fun backspaceAction(view: View)//функция стирания
    {
        val length = workingTV.length()
        if(length > 0)
            workingTV.text = workingTV.text.dropLast(1)
    }
    fun rezultAction(view: View)//функция вычисления результата
    {
        resultsTV.text = calculateResults()
    }

    private fun calculateResults(): String//функция для вычисления результата
    {
        val digitsOperators = digitsOperators()
        if (digitsOperators.isEmpty())
            return  ""
        val timesDivision = MultDiv(digitsOperators)
        if (timesDivision.isEmpty())
            return  ""
        val result = SumRazn(timesDivision)
        return result.toString()
    }

    private fun SumRazn(numbersAndOperators: MutableList<Any>): Float//функция сложения и вычитания
    {
        var result = numbersAndOperators[0] as Float
        for(i in numbersAndOperators.indices)
        {
            if(numbersAndOperators[i] is Char && i != numbersAndOperators.lastIndex)
            {
                val operator = numbersAndOperators[i]
                val nextDigit = numbersAndOperators[i + 1] as Float
                if (operator == '+')
                    result += nextDigit
                if (operator == '-')
                    result -= nextDigit
            }
        }

        return result
    }

    private fun MultDiv(numbersAndOperators: MutableList<Any>): MutableList<Any>//Функция проверки операции и деления
    {
        var list = numbersAndOperators
        while (list.contains('x') || list.contains('÷'))
        {
            list = ActionMultDiv(list)
        }
        return list
    }

    private fun ActionMultDiv(numbersAndOperators: MutableList<Any>): MutableList<Any> {//Функция деления и умножения
        val newList = mutableListOf<Any>()
        var restartIndex = numbersAndOperators.size
        for (i in numbersAndOperators.indices) {
            if (numbersAndOperators[i] is Char && i != numbersAndOperators.lastIndex && i < restartIndex) {
                val operator = numbersAndOperators[i]
                val prevNumber = numbersAndOperators[i - 1] as Float
                val nextNumber = numbersAndOperators[i + 1] as Float
                if (operator == 'x') {
                    newList.add(prevNumber * nextNumber)
                    restartIndex = i + 1
                } else if (operator == '÷') {
                    newList.add(prevNumber / nextNumber)
                    restartIndex = i + 1
                } else {//если оператор умножение и не деление добавляем предыдущее число и оператор
                    newList.add(prevNumber)
                    newList.add(operator)
                }
            }
            if (i > restartIndex) {
                newList.add(numbersAndOperators[i])
            }
        }
        return newList
    }

    private fun digitsOperators(): MutableList<Any>//Функция преобразования списка на числа и операции
    {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for (simvol in workingTV.text)
        {
            if(simvol.isDigit() || simvol == '.')
                currentDigit += simvol
            else
            {
                list.add(currentDigit.toFloat())
                currentDigit = ""
                list.add(simvol)
            }
        }
        if(currentDigit != "")
            list.add(currentDigit.toFloat())
        return  list
    }

}




