package com.ebc.myapp

import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebc.myapp.ui.theme.MyappTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ScreenFinanceView()
                }
            }
        }
    }
}

@Composable
fun ScreenFinanceView(){
    val limits = arrayOf (0.01, 644.59, 5470.93, 9614.67, 11176.63, 13381.48,26988.51, 42537.59, 81211.26, 108281.68, 324845.02)
    val fixedFee = arrayOf (0.0, 12.38, 321.26, 772.10, 1022.01, 1417.12, 4323.58, 7980.73, 19582.83, 28245.36, 101876.90)
    val surplusAtLowerLimit = arrayOf (1.92, 6.40, 10.88, 16.0, 17.92, 21.36, 23.52, 30.00, 32.00, 34.00, 35.00)

    //States
    var incomeQuantity by remember {
        mutableStateOf("")
    }

    var savingsQuantity by remember {
        mutableStateOf("")
    }

    var commonQuantity by remember {
        mutableStateOf("")
    }

    var extraQuantity by remember {
        mutableStateOf("")
    }

    //Class Operations
    val balance = AdminResult()
    var taxes = balance.calculateTaxes(
        income = incomeQuantity.toDoubleOrNull() ?: 0.0,
        incomeRange = limits,
        surplusAtLowerLimit = surplusAtLowerLimit,
        fixFee = fixedFee
    )

    var savingsPercent = balance.savingPercent(
        income = incomeQuantity.toDoubleOrNull() ?: 0.0,
        saving = savingsQuantity.toDoubleOrNull() ?: 0.0
    )

    var commonPercent = balance.commonPercent(
        income = incomeQuantity.toDoubleOrNull() ?: 0.0,
        common = commonQuantity.toDoubleOrNull() ?: 0.0
    )

    var extraPercent = balance.extraPercent(
        income = incomeQuantity.toDoubleOrNull() ?: 0.0,
        extras = extraQuantity.toDoubleOrNull() ?: 0.0
    )

    var leftOverQuantity = balance.leftOver(
        income = incomeQuantity.toDoubleOrNull() ?: 0.0,
        taxes = taxes,
        savings = savingsQuantity.toDoubleOrNull() ?: 0.0,
        commonExpenses = commonQuantity.toDoubleOrNull() ?: 0.0,
        extras = extraQuantity.toDoubleOrNull() ?: 0.0
    )
    //Display Elements
    Column (
        modifier = Modifier.padding(32.dp) ,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title),
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))
        IncomeInputField(ammount = incomeQuantity , changeEvent = {incomeQuantity = it})
        Text(text = stringResource(id = R.string.savings_label))
        InputMoneyField(ammount = savingsQuantity, changeEvent = {savingsQuantity = it})
        Text(text = stringResource(id = R.string.common_label))
        InputMoneyField(ammount = commonQuantity, changeEvent = {commonQuantity = it})
        Text(text = stringResource(id = R.string.extras_label))
        InputMoneyField(ammount = extraQuantity, changeEvent = {extraQuantity = it})
        Text(text = stringResource(id = R.string.taxes, taxes))
        Text(text = stringResource(id = R.string.savings, savingsPercent))
        Text(text = stringResource(id = R.string.common, commonPercent))
        Text(text = stringResource(id = R.string.extra_expenses, extraPercent))
        Text(text = stringResource(id = R.string.left_over, leftOverQuantity))
    }
}

@Composable
fun IncomeInputField(ammount: String, changeEvent: (String) -> Unit){
    OutlinedTextField(
        value = ammount,
        onValueChange = changeEvent,
        label = { Text(text = stringResource(id = R.string.gross_income))},
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Spacer(modifier = Modifier.height(5.dp))
}

@Composable
fun InputMoneyField(ammount: String, changeEvent: (String) -> Unit){
    TextField(
        value = ammount,
        onValueChange = changeEvent,
        label = { Text(text = stringResource(id = R.string.amount))},
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

    )
}

private class AdminResult(){
    fun calculateTaxes(fixFee:Array<Double> ,income: Double, incomeRange:Array<Double>, surplusAtLowerLimit:Array<Double> ) : Double {
        var res = 0.0
        fun calculateBase(income: Double, lowerLimit: Array<Double>) : Double {
            var base = 0.0
            for((index, l) in lowerLimit.withIndex()){
                if(index <= lowerLimit.size){
                    if(income > l && income <= lowerLimit.get(index  + 1)){
                        base += (income - lowerLimit.get(index))
                    }
                }
            }
            return base
        }

        val base = calculateBase(income=income, lowerLimit = incomeRange)
        for ((index, incomeLimit) in incomeRange.withIndex()){
            if(index <= incomeRange.size){
                if(income > incomeLimit && income <= incomeRange.get(index  + 1)){
                    res += (base * surplusAtLowerLimit.get(index))/100 + fixFee.get(index)
                }
            }
        }
        return  res
    }

    fun savingPercent(income: Double = 0.0, saving: Double = 0.0) : Double {
        return (saving * 100) / income
    }

    fun commonPercent(income: Double = 0.0, common: Double = 0.0) : Double {
        return (common * 100 ) / income
    }

    fun extraPercent(income: Double = 0.0, extras: Double = 0.0) : Double {
        return (extras * 100) / income
    }

    fun leftOver(taxes: Double ,income: Double, savings: Double, commonExpenses: Double, extras: Double) : String {
        val estimatedTotal = income - taxes - savings - commonExpenses - extras
        if (estimatedTotal >= 0){
            return estimatedTotal.toString()
        }
        else {
            return "Your expenses exceed your income!"
        }
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultView(){
    MyappTheme {
        ScreenFinanceView()
    }
}
