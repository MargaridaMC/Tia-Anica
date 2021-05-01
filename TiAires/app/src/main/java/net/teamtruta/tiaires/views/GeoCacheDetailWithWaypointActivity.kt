package net.teamtruta.tiaires.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.adapters.SectionsPagerAdapter
import net.teamtruta.tiaires.data.models.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModel
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModelFactory

class GeoCacheDetailWithWaypointActivity : AppCompatActivity() {

    private val viewModel: GeoCacheDetailViewModel by viewModels{
        GeoCacheDetailViewModelFactory((application as App).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache_w_waypoints_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_geo_cache_detail)
        setSupportActionBar(toolbar)

        // Setup Action Bar
        val ab = supportActionBar!!
        ab.setDisplayHomeAsUpEnabled(true)

        val geoCacheID = intent.getLongExtra(App.GEOCACHE_IN_TOUR_ID_EXTRA, -1L)

        // Observe geocache that was clicked on
        viewModel.getGeoCacheInTourFromID(geoCacheID).observe(this,
                { geoCacheInTour: GeoCacheInTourWithDetails -> setupToolbarName(geoCacheInTour) })

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, geoCacheID)

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

    }

    private fun setupToolbarName(geoCacheInTour: GeoCacheInTourWithDetails) {
        val currentGeoCache = geoCacheInTour.geoCache.geoCache

        // Set GeoCache Title
        val ab = supportActionBar
        ab!!.title = currentGeoCache.name
    }


    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, TourActivity::class.java)
        startActivity(intent)
        return true
    }
}