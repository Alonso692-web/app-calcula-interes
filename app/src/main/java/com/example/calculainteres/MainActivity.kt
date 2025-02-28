package com.example.calculainteres

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormat
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private lateinit var etCantidadInicial: EditText
    private lateinit var etTasaInteres: EditText
    private lateinit var etPlazo: EditText
    private lateinit var tvMontoFinal: TextView
    private lateinit var tvGanancia: TextView
    private lateinit var btnCalcular: Button
    private lateinit var btnLimpiar: Button
    private lateinit var rbTipoInteresSimple: RadioButton
    private lateinit var rbTipoInteresCompuesto: RadioButton

    private val df = DecimalFormat("#.#####")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etCantidadInicial = findViewById(R.id.etCantidadInicial)
        etTasaInteres = findViewById(R.id.etTasaInteres)
        etPlazo = findViewById(R.id.etPlazo)
        tvMontoFinal = findViewById(R.id.tvMontoFinal)
        tvGanancia = findViewById(R.id.tvGanancia)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        btnCalcular = findViewById(R.id.btnCalcular)
        rbTipoInteresSimple = findViewById(R.id.rbTipoInteresSimple)
        rbTipoInteresCompuesto = findViewById(R.id.rbTipoInteresCompuesto)

        etCantidadInicial.requestFocus()
        rbTipoInteresSimple.isChecked = true

        rbTipoInteresCompuesto.setOnClickListener {
            rbTipoInteresSimple.isChecked = false
            rbTipoInteresCompuesto.isChecked = true
        }

        rbTipoInteresSimple.setOnClickListener {
            rbTipoInteresCompuesto.isChecked = false
            rbTipoInteresSimple.isChecked = true
        }


        btnCalcular.setOnClickListener {
            val cantidadInicial = etCantidadInicial.text.toString().toDoubleOrNull()
            val tasaInteres = etTasaInteres.text.toString().toDoubleOrNull()
            val plazo = etPlazo.text.toString().toDoubleOrNull()

            if (cantidadInicial != null && tasaInteres != null && plazo != null) {
                if (rbTipoInteresSimple.isChecked) {
                    val ganancia = cantidadInicial * ((tasaInteres / 100) / 365) * plazo
                    val sumaDinero = cantidadInicial + ganancia

                    val textoFormateadoGanancia = df.format(ganancia)
                    val textoFormateadosumaDinero = df.format(sumaDinero)

                    tvMontoFinal.text = "$ $textoFormateadoGanancia"
                    tvGanancia.text = "Ganancia: $ $textoFormateadosumaDinero"
                } else {
                    val aux: Double = (1 + (tasaInteres / 100) / 365)
                    val montoAuxiliar = Math.pow(aux, plazo)
                    val montoFinal = cantidadInicial * montoAuxiliar
                    val ganancia = montoFinal - cantidadInicial

                    val textoFormateadoMontoFinal = df.format(montoFinal)
                    val textoFormateadoGanancia = df.format(ganancia)
                    tvMontoFinal.text = "$ $textoFormateadoMontoFinal"
                    tvGanancia.text = "Ganancia: $ $textoFormateadoGanancia"
                }
            } else {
                Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show()
            }
        }


        btnLimpiar.setOnClickListener {
            etCantidadInicial.text.clear()
            etTasaInteres.text.clear()
            etPlazo.text.clear()
            tvMontoFinal.text = "$0.0"
            tvGanancia.text = "Ganancia: $0.0"
            etCantidadInicial.requestFocus()
            rbTipoInteresCompuesto.isChecked = false
            rbTipoInteresSimple.isChecked = true
        }

    }
}