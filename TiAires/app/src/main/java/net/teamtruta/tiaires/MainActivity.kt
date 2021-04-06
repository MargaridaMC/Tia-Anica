package net.teamtruta.tiaires

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import net.teamtruta.tiaires.TourListAdapter.ItemClickListener
import net.teamtruta.tiaires.TourListAdapter.TourViewHolder
import net.teamtruta.tiaires.viewModels.MainActivityViewModel
import net.teamtruta.tiaires.viewModels.MainActivityViewModelFactory
import java.util.*

class MainActivity : AppCompatActivity(), ItemClickListener {

    var TAG = MainActivity::class.java.simpleName

    private val viewModel: MainActivityViewModel by viewModels{
        MainActivityViewModelFactory((application as App).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCenter.start(application, "67d245e6-d08d-4d74-8616-9af6c3471a09", Analytics::class.java, Crashes::class.java)

        // If Login is required, set contentView to the login page
        // Else go to home page
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
        val username = sharedPref.getString("username", "")
        val authCookie = sharedPref.getString(getString(R.string.authentication_cookie_key), "")
        val properties: MutableMap<String, String?> = HashMap()
        properties["Username"] = username
        Analytics.trackEvent("MainActivity.onCreate", properties)
        Log.d(TAG, "Cookie: $authCookie")
        if (authCookie == "") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Setup content
        setContentView(R.layout.activity_main)
        val tourListView = findViewById<RecyclerView>(R.id.tour_list)
        tourListView.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(tourListView.context, LinearLayout.VERTICAL)
        dividerItemDecoration.setDrawable(ColorDrawable(getColor(R.color.black)))
        tourListView.addItemDecoration(dividerItemDecoration)

        viewModel.allTours.observe(this) {
            tours ->

            if(tours.isEmpty()){
                setContentView(R.layout.activity_main_nothing_to_show)
            } else {
                val tourListAdapter: RecyclerView.Adapter<TourViewHolder> =
                        TourListAdapter(tours, this, applicationContext)
                tourListView.adapter = tourListAdapter
            }

        }

        // Setup fab
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view: View ->
            val intent = Intent(view.context, TourCreationActivity::class.java)
            startActivity(intent)
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Put username on toolbar
        if (username != "") {
            Objects.requireNonNull(supportActionBar)!!.title = username
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.go_to_login_page) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(view: View, position: Int, tourID: Long) {
        //Toast.makeText(this, "You clicked on the tour with the id: " + tourID , Toast.LENGTH_SHORT).show();
        val intent = Intent(this, TourActivity::class.java)
        viewModel.setCurrentTourID(tourID)
        startActivity(intent)
    }

    override fun onBackPressed() {
        // A more recommended way seems to be what is documented here: https://developer.android.com/guide/navigation/navigation-custom-back#java
        // do what you want to do when the "back" button is pressed.
        finishAffinity()

        // android.os.Process.killProcess(android.os.Process.myPid());
        // System.exit(0); // see: https://stackoverflow.com/questions/18292016/difference-between-finish-and-system-exit0
    }
}