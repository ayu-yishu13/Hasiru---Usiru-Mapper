package com.hasiru.usiru.mapper.ui.speciesguide

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.hasiru.usiru.mapper.R
import org.json.JSONArray

class SpeciesGuideFragment : Fragment(R.layout.fragment_species_guide) {
    private val adapter = SpeciesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.speciesRecyclerView)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        adapter.submitList(loadSpecies())
        view.findViewById<MaterialButtonToggleGroup>(R.id.languageToggle).addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) adapter.setKannada(checkedId == R.id.kannadaButton)
        }
    }

    private fun loadSpecies(): List<SpeciesItem> {
        val json = requireContext().assets.open("species_guide.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            SpeciesItem(
                commonName = item.getString("commonName"),
                kannadaName = item.getString("kannadaName"),
                scientificName = item.getString("scientificName"),
                speciesFactor = item.getDouble("speciesFactor").toFloat(),
                isNative = item.getBoolean("isNative"),
                descriptionEn = item.getString("descriptionEn"),
                descriptionKn = item.getString("descriptionKn"),
                placeholderColor = item.getString("placeholderColor")
            )
        }
    }
}
