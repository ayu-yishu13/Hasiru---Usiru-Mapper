package com.hasiru.usiru.mapper.ui.dashboard

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.hasiru.usiru.mapper.HasiruApp
import com.hasiru.usiru.mapper.R

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory((requireActivity().application as HasiruApp).repository)
    }
    private var currentScore = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scoreText = view.findViewById<TextView>(R.id.totalOxygenScoreText)
        val treeCountText = view.findViewById<TextView>(R.id.treeCountText)
        val nativeCountText = view.findViewById<TextView>(R.id.nativeCountText)
        val pitCountText = view.findViewById<TextView>(R.id.emptyPitCountText)
        val chart = view.findViewById<LinearLayout>(R.id.topSpeciesChart)
        val share = view.findViewById<MaterialButton>(R.id.shareScoreButton)

        viewModel.totalOxygenScore.observe(viewLifecycleOwner) { score ->
            animateScore(scoreText, currentScore, score)
            currentScore = score
        }
        viewModel.treeCount.observe(viewLifecycleOwner) { treeCountText.text = "$it\nTrees" }
        viewModel.nativeCount.observe(viewLifecycleOwner) { nativeCountText.text = "$it\nNative" }
        viewModel.emptyPitCount.observe(viewLifecycleOwner) { pitCountText.text = "$it\nEmpty Pits" }
        viewModel.topSpecies.observe(viewLifecycleOwner) { species -> renderChart(chart, species) }
        share.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "My street's Hasiru-Usiru oxygen score is ${currentScore.toInt()}! Add more trees to increase your street's score.")
            }
            startActivity(Intent.createChooser(intent, "Share Score"))
        }
    }

    private fun animateScore(target: TextView, from: Float, to: Float) {
        ValueAnimator.ofFloat(from, to).apply {
            duration = 700
            addUpdateListener { target.text = (it.animatedValue as Float).toInt().toString() }
            doOnEnd { target.text = to.toInt().toString() }
            start()
        }
    }

    private fun renderChart(container: LinearLayout, species: List<Pair<String, Int>>) {
        container.removeAllViews()
        val max = species.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
        species.forEach { (name, count) ->
            val row = TextView(requireContext()).apply {
                text = "$name  ${"█".repeat((count * 20 / max).coerceAtLeast(1))}  $count"
                textSize = 15f
                setPadding(0, 8, 0, 8)
            }
            container.addView(row)
        }
        if (species.isEmpty()) container.addView(TextView(requireContext()).apply { text = "Tag trees to see species counts" })
    }
}
