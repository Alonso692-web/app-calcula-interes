package com.example.calculainteres

data class CalculationHistoryItem(
    val initialAmount: Double,
    val rate: Double,
    val termInDays: Int,
    val gain: Double
)