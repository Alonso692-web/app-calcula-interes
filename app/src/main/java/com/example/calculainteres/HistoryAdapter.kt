package com.example.calculainteres

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class HistoryAdapter(private val historyList: List<CalculationHistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    // Formatos para los números
    private val currencyFormat = DecimalFormat("'$'#,##0.00")
    private val gainFormat = DecimalFormat("'+$'#,##0.00")
    private val percentFormat = DecimalFormat("#.##'%'")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = historyList.size

    // ViewHolder actualizado para coincidir con el nuevo layout
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvTerm: TextView = itemView.findViewById(R.id.tvTerm)
        private val tvGain: TextView = itemView.findViewById(R.id.tvGain)
        private val tvRate: TextView = itemView.findViewById(R.id.tvRate)

        fun bind(item: CalculationHistoryItem) {
            val termText = if (item.termInDays == 1) "dia" else "dias"

            tvAmount.text = "Cantidad: ${currencyFormat.format(item.initialAmount)}"
            tvTerm.text = "Plazo: ${item.termInDays} $termText"
            tvGain.text = gainFormat.format(item.gain)
            tvRate.text = percentFormat.format(item.rate)
        }
    }
}