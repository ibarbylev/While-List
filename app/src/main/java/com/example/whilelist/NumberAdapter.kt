package com.example.whilelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NumberAdapter(
    private var numbers: List<String>,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<NumberAdapter.ViewHolder>() {

    fun updateList(newNumbers: List<String>) {
        numbers = newNumbers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_number, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val number = numbers[position]
        holder.textViewNumber.text = number
        holder.buttonRemove.setOnClickListener { onRemoveClick(number) }
    }

    override fun getItemCount(): Int = numbers.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewNumber: TextView = view.findViewById(R.id.textViewNumber)
        val buttonRemove: Button = view.findViewById(R.id.buttonRemove)
    }
}