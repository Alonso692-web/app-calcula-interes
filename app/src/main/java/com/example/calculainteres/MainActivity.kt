package com.example.calculainteres

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private lateinit var etCantidadInicial: TextInputEditText
    private lateinit var etTasaInteres: TextInputEditText
    private lateinit var etPlazo: TextInputEditText
    private lateinit var tvMontoFinal: TextView
    private lateinit var tvGanancia: TextView
    private lateinit var btnCalcular: MaterialButton
    private lateinit var btnLimpiar: MaterialButton
    private lateinit var rbTipoInteresSimple: MaterialRadioButton
    private lateinit var rbTipoInteresCompuesto: MaterialRadioButton
    private lateinit var frequencySpinner: MaterialAutoCompleteTextView
    private lateinit var frequencyInputLayout: TextInputLayout
    private lateinit var chart: LineChart

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
        setupChart()
        setupFrequencySpinner()
        setupRadioButtons()
        setupButtons()
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
        rbTipoInteresSimple.isChecked = true
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = true
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
    }

    private fun setupRadioButtons() {
        val interestTypeGroup = findViewById<RadioGroup>(R.id.interestTypeGroup)

        interestTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTipoInteresCompuesto -> {
                    frequencyInputLayout.visibility = View.VISIBLE
                    // Establece "Anual" como valor predeterminado
                    frequencySpinner.setText(compoundingFrequencies[3], false) // Índice 3 = "Anual"
                    frequencySpinner.post {
                        frequencySpinner.clearFocus() // Elimina el foco para evitar apertura automática
                    }
                }

                else -> {
                    frequencyInputLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun setupFrequencySpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.compounding_frequencies,
            R.layout.dropdown_item
        )

        frequencySpinner.setAdapter(adapter)

        // Establece el valor inicial (solo si es necesario)
        frequencySpinner.setText(adapter.getItem(3), false) // "Anual"


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

    private fun validateInputs(): Boolean {
        var isValid = true

        fun validateField(editText: TextInputEditText, errorMessage: String) {
            if (editText.text?.toString()?.isBlank() == true) {
                editText.error = errorMessage
                isValid = false
            }
        }

        validateField(etCantidadInicial, "Ingrese un monto válido")
        validateField(etTasaInteres, "Ingrese una tasa válida")
        validateField(etPlazo, "Ingrese un plazo válido")

        return isValid
    }

    private fun calculateResults() {
        val principal = etCantidadInicial.text.toString().toDouble()
        val rate = etTasaInteres.text.toString().toDouble() / 100
        val days = etPlazo.text.toString().toInt()

        if (rbTipoInteresSimple.isChecked) {
            val interest = principal * rate * (days / 365.0)
            updateUI(principal + interest, interest)
        } else {
            val frequency = frequencyDays[frequencySpinner.text.toString()] ?: 365
            val periods = days.toDouble() / frequency
            val amount = principal * (1 + rate / frequency).pow(periods * frequency)
            updateUI(amount, amount - principal)
        }
    }

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
        val entries = ArrayList<Entry>()
        val principal = etCantidadInicial.text.toString().toDouble()
        val rate = etTasaInteres.text.toString().toDouble() / 100
        val days = etPlazo.text.toString().toInt()

        for (i in 0..days step 30) {
            val amount = if (rbTipoInteresSimple.isChecked) {
                principal * (1 + rate * (i / 365.0))
            } else {
                val frequency = frequencyDays[frequencySpinner.text.toString()] ?: 365
                val periods = i.toDouble() / frequency
                principal * (1 + rate / frequency).pow(periods * frequency)
            }
            entries.add(Entry(i.toFloat(), amount.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Crecimiento de la inversión").apply {
            color = resources.getColor(R.color.green_500)
            valueTextColor = resources.getColor(R.color.primary)
            lineWidth = 2f
        }

        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun clearInputs() {
        etCantidadInicial.text?.clear()
        etTasaInteres.text?.clear()
        etPlazo.text?.clear()
        tvMontoFinal.text = "$0.00"
        tvGanancia.text = "Ganancia: $0.00"
        frequencySpinner.setText(compoundingFrequencies[0], false)
        rbTipoInteresSimple.isChecked = true
        frequencyInputLayout.visibility = View.GONE
        etCantidadInicial.requestFocus()
    }

    private fun resetChart() {
        chart.clear()
        chart.invalidate()
    }
}