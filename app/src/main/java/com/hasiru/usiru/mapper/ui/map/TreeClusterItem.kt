package com.hasiru.usiru.mapper.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.hasiru.usiru.mapper.data.local.TreeEntity

class TreeClusterItem(val tree: TreeEntity) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(tree.latitude, tree.longitude)
    override fun getTitle(): String = if (tree.isEmptyPit) "Empty Pit" else tree.species
    override fun getSnippet(): String = "${tree.kannadaName} • ${tree.healthStatus} • O₂ ${tree.oxygenScore.toInt()}"
    override fun getZIndex(): Float = if (tree.isEmptyPit) 2f else 1f
}
