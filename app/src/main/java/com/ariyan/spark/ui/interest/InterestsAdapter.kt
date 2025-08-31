package com.ariyan.spark.ui.interest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ariyan.spark.R
import com.google.android.material.chip.Chip

class InterestsAdapter(
  private val items: List<String>,
  private val onSelectionChanged: (Set<String>) -> Unit
) : RecyclerView.Adapter<InterestsAdapter.VH>() {

  private val selected = mutableSetOf<String>()

  inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val chip: Chip = itemView.findViewById(R.id.interest_item_chip)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chip, parent, false)
    return VH(view)
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    val label = items[position]
    holder.chip.text = label
    holder.chip.isChecked = selected.contains(label)
    holder.chip.setOnClickListener { // It's good practice to set this on the chip itself
      // The chip's isChecked state updates automatically on click when it's checkable
      if (holder.chip.isChecked) {
        selected.add(label)
      } else {
        selected.remove(label)
      }
      onSelectionChanged(selected)
    }
  }

  override fun getItemCount(): Int = items.size
}
