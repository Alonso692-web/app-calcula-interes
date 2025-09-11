package com.example.calculainteres

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    // --- Vistas de Inputs y Resultados ---
    private lateinit var etCantidadInicial: TextInputEditText
    private lateinit var etTasaInteres: TextInputEditText
    private lateinit var etPlazo: TextInputEditText
    private lateinit var tvMontoFinal: TextView
    private lateinit var tvGanancia: TextView

    // --- Layouts y TextViews de Error ---
    private lateinit var amountInputLayout: TextInputLayout
    private lateinit var rateInputLayout: TextInputLayout
    private lateinit var daysInputLayout: TextInputLayout
    private lateinit var tvAmountError: TextView
    private lateinit var tvRateError: TextView
    private lateinit var tvDaysError: TextView

    // --- Botones y Controles ---
    private lateinit var btnCalcular: MaterialButton
    private lateinit var btnLimpiar: MaterialButton
    private lateinit var rbTipoInteresSimple: MaterialRadioButton
    private lateinit var rbTipoInteresCompuesto: MaterialRadioButton
    private lateinit var frequencySpinner: MaterialAutoCompleteTextView
    private lateinit var frequencyInputLayout: TextInputLayout
    private lateinit var chart: PieChart

    // --- Historial (Movimientos) ---
    private lateinit var rvHistory: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<CalculationHistoryItem>()

    // --- Variables y Formatos ---
    private var currentPrincipal: Double = 0.0
    private var currentGain: Double = 0.0
    private val df = DecimalFormat("#,##0.00")
    private val compoundingFrequencies = arrayOf("Diaria", "Mensual", "Trimestral", "Anual")
    private val frequencyDays = mapOf(
        "Diaria" to 1,
        "Mensual" to 30,
        "Trimestral" to 90,
        "Anual" to 365
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupNumberFormatting()
        setupChart()
        setupFrequencySpinner()
        setupRadioButtons()
        setupButtons()
        setupRecyclerView()
    }

    private fun initViews() {
        etCantidadInicial = findViewById(R.id.etCantidadInicial)
        etTasaInteres = findViewById(R.id.etTasaInteres)
        etPlazo = findViewById(R.id.etPlazo)
        tvMontoFinal = findViewById(R.id.tvMontoFinal)
        tvGanancia = findViewById(R.id.tvGanancia)
        btnCalcular = findViewById(R.id.btnCalcular)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        rbTipoInteresSimple = findViewById(R.id.rbTipoInteresSimple)
        rbTipoInteresCompuesto = findViewById(R.id.rbTipoInteresCompuesto)
        frequencySpinner = findViewById(R.id.frequencySpinner)
        frequencyInputLayout = findViewById(R.id.frequencyInputLayout)
        chart = findViewById(R.id.chart)
        rvHistory = findViewById(R.id.rvHistory)

        // --- Inicializar los Layouts y TextViews de Error ---
        amountInputLayout = findViewById(R.id.amountInputLayout)
        rateInputLayout = findViewById(R.id.rateInputLayout)
        daysInputLayout = findViewById(R.id.daysInputLayout)
        tvAmountError = findViewById(R.id.tvAmountError)
        tvRateError = findViewById(R.id.tvRateError)
        tvDaysError = findViewById(R.id.tvDaysError)

        rbTipoInteresSimple.isChecked = true
    }

    // ... (El resto de las funciones setupChart, setupRadioButtons, etc., no cambian)
    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setUsePercentValues(false)
        chart.setEntryLabelColor(Color.BLACK)
        chart.setEntryLabelTextSize(12f)
        chart.legend.isEnabled = true
        chart.setHoleColor(Color.TRANSPARENT)
        chart.setTransparentCircleAlpha(0)
        chart.setDrawEntryLabels(true)
        chart.setEntryLabelTextSize(14f)
        chart.setDrawCenterText(true)
        chart.centerText = "Rendimiento"
    }

    private fun setupRadioButtons() {
        val interestTypeGroup = findViewById<RadioGroup>(R.id.interestTypeGroup)
        interestTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTipoInteresCompuesto -> {
                    frequencyInputLayout.visibility = View.VISIBLE
                    frequencySpinner.setText(compoundingFrequencies[3], false)
                    frequencySpinner.post { frequencySpinner.clearFocus() }
                }
                else -> {
                    frequencyInputLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun setupFrequencySpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.compounding_frequencies, R.layout.dropdown_item
        )
        frequencySpinner.setAdapter(adapter)
        frequencySpinner.setText(adapter.getItem(3), false)
        frequencySpinner.setOnClickListener {
            if (frequencyInputLayout.visibility == View.VISIBLE) {
                frequencySpinner.showDropDown()
            }
        }
        frequencySpinner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && frequencyInputLayout.visibility == View.VISIBLE) {
                frequencySpinner.showDropDown()
            }
        }
    }

    private fun setupButtons() {
        btnCalcular.setOnClickListener {
            if (validateInputs()) {
                calculateResults()
                animateResults()
                updateChart()
            }
        }
        btnLimpiar.setOnClickListener {
            clearInputs()
            resetChart()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(historyList)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter
    }

    private fun addHistoryItem(item: CalculationHistoryItem) {
        historyList.add(0, item)
        historyAdapter.notifyItemInserted(0)
        rvHistory.scrollToPosition(0)
    }


    // --- FUNCIÓN DE VALIDACIÓN MODIFICADA ---
    private fun validateInputs(): Boolean {
        var isValid = true

        // Limpiar errores previos
        clearErrors()

        if (etCantidadInicial.text.toString().replace(",", "").isBlank()) {
            amountInputLayout.isErrorEnabled = true // Activa el color rojo y el ícono
            tvAmountError.visibility = View.VISIBLE   // Muestra nuestro TextView de error
            isValid = false
        }
        if (etTasaInteres.text.toString().isBlank()) {
            rateInputLayout.isErrorEnabled = true
            tvRateError.visibility = View.VISIBLE
            isValid = false
        }
        if (etPlazo.text.toString().isBlank()) {
            daysInputLayout.isErrorEnabled = true
            tvDaysError.visibility = View.VISIBLE
            isValid = false
        }

        return isValid
    }

    private fun calculateResults() {
        val principalText = etCantidadInicial.text.toString().replace(",", "")
        val principal = if (principalText.isBlank()) 0.0 else principalText.toDouble()
        val rateValue = etTasaInteres.text.toString().toDouble()
        val rate = rateValue / 100
        val days = etPlazo.text.toString().toInt()

        if (rbTipoInteresSimple.isChecked) {
            val interest = principal * rate * (days / 365.0)
            currentPrincipal = principal
            currentGain = interest
            updateUI(principal + interest, interest)
        } else {
            val frequency = frequencyDays[frequencySpinner.text.toString()] ?: 365
            val periods = days.toDouble() / frequency
            val amount = principal * (1 + rate / frequency).pow(periods * frequency)
            currentPrincipal = principal
            currentGain = amount - principal
            updateUI(amount, amount - principal)
        }

        val historyItem = CalculationHistoryItem(principal, rateValue, days, currentGain)
        addHistoryItem(historyItem)
    }

    // ... (updateUI, animateResults, updateChart no cambian)
    private fun updateUI(amount: Double, gain: Double) {
        tvMontoFinal.text = "$${df.format(amount)}"
        tvGanancia.text = "Ganancia: $${df.format(gain)}"
    }

    private fun animateResults() {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            interpolator = BounceInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float
                tvMontoFinal.scaleX = value
                tvMontoFinal.scaleY = value
            }
        }
        animator.start()
    }

    private fun updateChart() {
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(currentPrincipal.toFloat(), "Inicial"))
            add(PieEntry(currentGain.toFloat(), "Ganancia"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(this@MainActivity, R.color.green_500),
                ContextCompat.getColor(this@MainActivity, R.color.profits)
            )
            valueTextColor = Color.WHITE
            valueTextSize = 16f
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "$${df.format(value.toDouble())}"
                }
            }
        }

        chart.data = PieData(dataSet).apply {
            setValueTextSize(14f)
            setValueTypeface(Typeface.DEFAULT_BOLD)
        }
        chart.invalidate()
        chart.animateY(1000, Easing.EaseInOutQuad)
    }


    // --- FUNCIÓN clearInputs MODIFICADA ---
    private fun clearInputs() {
        etCantidadInicial.text?.clear()
        etTasaInteres.text?.clear()
        etPlazo.text?.clear()

        // Limpiar los errores visuales
        clearErrors()

        tvMontoFinal.text = "$0.00"
        tvGanancia.text = "Ganancia: $0.00"
        frequencySpinner.setText(compoundingFrequencies[3], false) // "Anual"
        rbTipoInteresSimple.isChecked = true
        frequencyInputLayout.visibility = View.GONE
        etCantidadInicial.requestFocus()
        currentPrincipal = 0.0
        currentGain = 0.0
    }

    // --- NUEVA FUNCIÓN para centralizar la limpieza de errores ---
    private fun clearErrors() {
        amountInputLayout.isErrorEnabled = false // Desactiva el color rojo y el ícono
        tvAmountError.visibility = View.GONE     // Oculta nuestro TextView

        rateInputLayout.isErrorEnabled = false
        tvRateError.visibility = View.GONE

        daysInputLayout.isErrorEnabled = false
        tvDaysError.visibility = View.GONE
    }


    private fun resetChart() {
        chart.clear()
        chart.invalidate()
    }

    private fun setupNumberFormatting() {
        etCantidadInicial.addTextChangedListener(NumberTextWatcher(etCantidadInicial))
    }

    // ... (NumberTextWatcher no cambia)
    inner class NumberTextWatcher(private val editText: TextInputEditText) : TextWatcher {
        private var isFormatting = false
        private var previousText = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (!isFormatting) {
                previousText = s?.toString() ?: ""
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isFormatting) return
            isFormatting = true
            val currentText = s.toString()
            if (currentText != previousText) {
                try {
                    if (currentText.isEmpty()) {
                        isFormatting = false
                        return
                    }
                    val cleanString = currentText.replace(",", "")
                    if (cleanString.contains('.')) {
                        val parts = cleanString.split('.')
                        if (parts.isNotEmpty()) {
                            val integerPart = if (parts[0].isNotEmpty()) {
                                val integerValue = parts[0].toLong()
                                DecimalFormat("#,###").format(integerValue)
                            } else ""
                            val decimalPart = if (parts.size > 1) ".${parts[1]}" else ""
                            val formatted = integerPart + decimalPart
                            if (formatted != currentText) {
                                editText.setText(formatted)
                                editText.setSelection(formatted.length)
                            }
                        }
                    } else {
                        if (cleanString.isNotEmpty()) {
                            val number = cleanString.toLong()
                            val formatted = DecimalFormat("#,###").format(number)
                            if (formatted != currentText) {
                                editText.setText(formatted)
                                editText.setSelection(formatted.length)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignorar errores de formato mientras el usuario escribe
                }
            }
            isFormatting = false
        }
    }
}