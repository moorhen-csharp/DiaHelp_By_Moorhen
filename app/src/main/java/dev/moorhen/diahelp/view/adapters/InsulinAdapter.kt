// dev.moorhen.diahelp.view.adapters.InsulinAdapter
package dev.moorhen.diahelp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.moorhen.diahelp.R
import dev.moorhen.diahelp.data.model.InsulinModel
import java.text.SimpleDateFormat
import java.util.*

class InsulinAdapter(private var insulinList: List<InsulinModel>) :
    RecyclerView.Adapter<InsulinAdapter.InsulinViewHolder>() {

    class InsulinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doseText: TextView = itemView.findViewById(R.id.textInsulinDose)
        val dateText: TextView = itemView.findViewById(R.id.textInsulinDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsulinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_insulin_note, parent, false)
        return InsulinViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsulinViewHolder, position: Int) {
        val item = insulinList[position]
        holder.doseText.text = "${item.InsulinDose} ед"

        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.dateText.text = sdf.format(item.Date)
    }

    override fun getItemCount() = insulinList.size

    fun updateData(newList: List<InsulinModel>) {
        this.insulinList = newList
        notifyDataSetChanged()
    }
}