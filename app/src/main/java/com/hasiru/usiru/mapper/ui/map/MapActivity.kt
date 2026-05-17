package com.hasiru.usiru.mapper.ui.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import coil.load
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.hasiru.usiru.mapper.HasiruApp
import com.hasiru.usiru.mapper.R
import com.hasiru.usiru.mapper.data.local.TreeEntity
import com.hasiru.usiru.mapper.ui.addtree.AddTreeActivity
import com.hasiru.usiru.mapper.ui.dashboard.DashboardFragment
import com.hasiru.usiru.mapper.ui.speciesguide.SpeciesGuideFragment

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val viewModel: MapViewModel by viewModels {
        MapViewModel.Factory((application as HasiruApp).repository)
    }
    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<TreeClusterItem>? = null
    private lateinit var mapLayer: View
    private lateinit var fragmentLayer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        mapLayer = findViewById(R.id.mapContainer)
        fragmentLayer = findViewById(R.id.fragmentContainer)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        findViewById<FloatingActionButton>(R.id.addTreeFab).setOnClickListener { openAddTree() }
        setupBottomNavigation()
        Handler(Looper.getMainLooper()).postDelayed({ findViewById<View>(R.id.splashOverlay).isVisible = false }, 1500)
        findViewById<Chip>(R.id.offlineChip).isVisible = false
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(12.9716, 77.5946), 12f))
        clusterManager = ClusterManager(this, map)
        clusterManager?.renderer = TreeClusterRenderer(map, clusterManager!!)
        clusterManager?.setOnClusterItemClickListener { showTreeBottomSheet(it.tree); true }
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        viewModel.treesLiveData.observe(this) { trees -> renderTrees(trees) }
        viewModel.fetchCommunityTrees()
    }

    private fun renderTrees(trees: List<TreeEntity>) {
        val manager = clusterManager ?: return
        manager.clearItems()
        manager.addItems(trees.map { TreeClusterItem(it) })
        manager.cluster()
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottomNavigation).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> { showMap(); true }
                R.id.nav_tag_tree -> { openAddTree(); false }
                R.id.nav_dashboard -> { showFragment(DashboardFragment()); true }
                R.id.nav_species -> { showFragment(SpeciesGuideFragment()); true }
                else -> false
            }
        }
    }

    private fun showMap() {
        mapLayer.isVisible = true
        fragmentLayer.isVisible = false
        findViewById<FloatingActionButton>(R.id.addTreeFab).isVisible = true
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        mapLayer.isVisible = false
        fragmentLayer.isVisible = true
        findViewById<FloatingActionButton>(R.id.addTreeFab).isVisible = false
        supportFragmentManager.commit { replace(R.id.fragmentContainer, fragment) }
    }

    private fun openAddTree() = startActivity(Intent(this, AddTreeActivity::class.java))

    private fun showTreeBottomSheet(tree: TreeEntity) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_tree_detail, null)
        view.findViewById<TextView>(R.id.detailSpecies).text = if (tree.isEmptyPit) "Empty Pit" else tree.species
        view.findViewById<TextView>(R.id.detailKannada).text = tree.kannadaName
        view.findViewById<TextView>(R.id.detailHealth).text = tree.healthStatus
        view.findViewById<TextView>(R.id.detailOxygen).text = "Oxygen Score: %.1f".format(tree.oxygenScore)
        view.findViewById<TextView>(R.id.detailNative).text = if (tree.isNative) "Native tree" else "Non-native / Unknown"
        val image = view.findViewById<ImageView>(R.id.detailPhoto)
        if (tree.photoUri != null) image.load(Uri.parse(tree.photoUri)) else image.setImageResource(R.drawable.ic_leaf)
        dialog.setContentView(view)
        dialog.show()
    }

    private inner class TreeClusterRenderer(map: GoogleMap, manager: ClusterManager<TreeClusterItem>) : DefaultClusterRenderer<TreeClusterItem>(this@MapActivity, map, manager) {
        override fun onBeforeClusterItemRendered(item: TreeClusterItem, markerOptions: MarkerOptions) {
            val hue = when {
                item.tree.isEmptyPit -> BitmapDescriptorFactory.HUE_YELLOW
                item.tree.healthStatus == "Sick" || item.tree.healthStatus == "Dead" -> BitmapDescriptorFactory.HUE_RED
                else -> BitmapDescriptorFactory.HUE_GREEN
            }
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue)).title(item.title).snippet(item.snippet)
        }
    }
}
