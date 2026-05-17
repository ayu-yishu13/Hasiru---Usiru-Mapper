package com.hasiru.usiru.mapper.ui.speciesguide

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.hasiru.usiru.mapper.R

class SpeciesAdapter : RecyclerView.Adapter<SpeciesAdapter.SpeciesViewHolder>() {
    private val items = mutableListOf<SpeciesItem>()
    private var kannada = false
    private val expanded = mutableSetOf<Int>()

    fun submitList(newItems: List<SpeciesItem>) {
        items.clear(); items.addAll(newItems); notifyDataSetChanged()
    }

    fun setKannada(enabled: Boolean) {
        kannada = enabled; notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeciesViewHolder = SpeciesViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_species_card, parent, false)
    )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SpeciesViewHolder, position: Int) = holder.bind(items[position], position)

    inner class SpeciesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card: MaterialCardView = view.findViewById(R.id.speciesCard)
        private val swatch: View = view.findViewById(R.id.speciesColorSwatch)
        private val title: TextView = view.findViewById(R.id.speciesTitle)
        private val subtitle: TextView = view.findViewById(R.id.speciesSubtitle)
        private val factor: TextView = view.findViewById(R.id.speciesFactor)
        private val description: TextView = view.findViewById(R.id.speciesDescription)
        private val details: TextView = view.findViewById(R.id.speciesDetails)

        fun bind(item: SpeciesItem, position: Int) {
            swatch.setBackgroundColor(Color.parseColor(item.placeholderColor))
            title.text = if (kannada) item.kannadaName else item.commonName
            subtitle.text = if (kannada) item.commonName else item.kannadaName
            factor.text = "Factor ${item.speciesFactor} • Oxygen: girth × ${item.speciesFactor}"
            description.text = if (kannada) item.descriptionKn else item.descriptionEn
            details.text = "${item.scientificName}\n${item.descriptionEn}\n${item.descriptionKn}"
            details.visibility = if (expanded.contains(position)) View.VISIBLE else View.GONE
            card.setOnClickListener {
                if (!expanded.add(position)) expanded.remove(position)
                notifyItemChanged(position)
            }
        }
    }
}
