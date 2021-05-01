package net.teamtruta.tiaires.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.adapters.WaypointListAdapter
import net.teamtruta.tiaires.data.models.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.models.Waypoint
import net.teamtruta.tiaires.extensions.hideKeyboard
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModel
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModelFactory

/**
 * A placeholder fragment containing a simple view.
 */
private const val ARG_PARAM1 = "geoCacheID"

class WaypointListFragment : Fragment(), WaypointListAdapter.GoToOnClickListener, WaypointListAdapter.WaypointDoneOnClickListener {

    private var geoCacheInTourID: Long = -1L

    private val viewModel: GeoCacheDetailViewModel by viewModels{
        GeoCacheDetailViewModelFactory((requireActivity().application as App).repository)
    }

    private lateinit var newWaypointNameET: EditText
    private lateinit var newWaypointCoordinatesET: EditText
    private lateinit var waypointListView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            geoCacheInTourID = it.getLong(ARG_PARAM1)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_waypoint_list, container, false)
        //val textView: TextView = root.findViewById(R.id.section_label)
        //pageViewModel.text.observe(viewLifecycleOwner, Observer<String> {
        //    textView.text = it
        //})

        // Setup recyclerview with waypoint list
        waypointListView = root.findViewById(R.id.waypoint_list)
        val layoutManager = LinearLayoutManager(container?.context)
        waypointListView.layoutManager = layoutManager

        val waypointListAdapter = WaypointListAdapter(this, this)
        waypointListView.adapter = waypointListAdapter

        // Observe data and fill in list
        viewModel.getGeoCacheInTourFromID(geoCacheInTourID).observe(requireActivity(),
                { geoCacheInTour: GeoCacheInTourWithDetails ->
                    waypointListAdapter.setData(geoCacheInTour.geoCache.waypoints) })

        // Add click listener to button top add new waypoint
        newWaypointNameET = root.findViewById(R.id.et_waypoint_name)
        newWaypointCoordinatesET = root.findViewById(R.id.et_waypoint_coordinates)
        val addNewWaypointButton: Button = root.findViewById(R.id.add_waypoint_button)
        addNewWaypointButton.setOnClickListener {
            addNewWaypoint()
        }
        return root
    }

    private fun addNewWaypoint() {
        // Get name and coordinates for the waypoint from the editTexts
        val waypointName: String = newWaypointNameET.text.toString()
        val waypointCoordinates: String = newWaypointCoordinatesET.text.toString()

        viewModel.addNewWaypointToGeoCache(waypointName, waypointCoordinates, geoCacheInTourID)

        // Clear the editTexts and scroll to bottom of list
        newWaypointNameET.text.clear()
        newWaypointCoordinatesET.text.clear()
        hideKeyboard()
        waypointListView.adapter?.notifyItemInserted(waypointListView.adapter!!.itemCount + 1)
    }

    companion object {

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(param1: Long) =
                WaypointListFragment().apply {
                    arguments = Bundle().apply {
                        putLong(ARG_PARAM1, param1)
                    }
            }
    }

    override fun onGoToClick(waypoint: Waypoint) {
        val gmmIntentUri = Uri.parse(String.format(resources.getString(R.string.coordinates_format),
                waypoint.latitude.value, waypoint.longitude.value))
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        startActivity(mapIntent)
    }

    override fun onWaypointDone(waypoint: Waypoint, done: Boolean) {
        viewModel.onWaypointDone(waypoint, done)
    }
}