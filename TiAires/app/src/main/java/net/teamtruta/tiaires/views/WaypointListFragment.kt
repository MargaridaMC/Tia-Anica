package net.teamtruta.tiaires.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.adapters.WaypointListAdapter
import net.teamtruta.tiaires.callbacks.WaypointInteractionCallback
import net.teamtruta.tiaires.data.models.Coordinate
import net.teamtruta.tiaires.data.models.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.models.Waypoint
import net.teamtruta.tiaires.extensions.hideKeyboard
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModel
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModelFactory

/**
 * A placeholder fragment containing a simple view.
 */
private const val ARG_PARAM1 = "geoCacheID"

class WaypointListFragment : Fragment(),
    WaypointListAdapter.GoToOnClickListener,
    WaypointListAdapter.WaypointDoneOnClickListener,
    WaypointListAdapter.EditWaypointOnClickListener{

    private var geoCacheInTourID: Long = -1L

    private val viewModel: GeoCacheDetailViewModel by viewModels{
        GeoCacheDetailViewModelFactory((requireActivity().application as App).repository)
    }

    private lateinit var newWaypointNameET: EditText
    private lateinit var newWaypointCoordinatesET: EditText
    private lateinit var newWaypointNotesET: EditText
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

        // Setup recyclerview with waypoint list
        waypointListView = root.findViewById(R.id.waypoint_list)
        val layoutManager = LinearLayoutManager(container?.context)
        waypointListView.layoutManager = layoutManager

        val waypointListAdapter = WaypointListAdapter(this, this,
            this, viewModel)
        waypointListView.adapter = waypointListAdapter

        // Add handler for swiping to delete
        val swipeHandler = object : WaypointInteractionCallback(requireContext()){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = waypointListView.adapter as WaypointListAdapter
                adapter.onItemDismiss(viewHolder.absoluteAdapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(waypointListView)

        // Observe data and fill in list
        viewModel.getGeoCacheInTourFromID(geoCacheInTourID).observe(requireActivity(),
                { geoCacheInTour: GeoCacheInTourWithDetails ->
                    waypointListAdapter.setData(geoCacheInTour.geoCache.waypoints) })

        // Add click listener to button top add new waypoint
        newWaypointNameET = root.findViewById(R.id.et_waypoint_name)
        newWaypointCoordinatesET = root.findViewById(R.id.et_waypoint_coordinates)
        newWaypointNotesET = root.findViewById(R.id.et_waypoint_notes)
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
        val waypointNotes: String = newWaypointNotesET.text.toString()

        viewModel.waypointAdditionSuccessful.observe(requireActivity(), { waypointAddedEvent ->
            waypointAddedEvent.getContentIfNotHandled()?.let {
                (success, message) ->
                Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
                if(success){
                    // Clear the editTexts and scroll to bottom of list
                    newWaypointNameET.setText("")
                    newWaypointCoordinatesET.setText("")
                    newWaypointNotesET.setText("")
                    hideKeyboard()
                    waypointListView.adapter?.notifyItemInserted(waypointListView.adapter!!.itemCount + 1)
                }
            }
        })
        viewModel.addNewWaypointToGeoCache(waypointName, waypointCoordinates, waypointNotes, geoCacheInTourID)
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
        if(waypoint.latitude != null && waypoint.longitude != null){
            val gmmIntentUri = Uri.parse(String.format(resources.getString(R.string.coordinates_format),
                    waypoint.latitude!!.value, waypoint.longitude!!.value))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(mapIntent)
        }
    }

    override fun onWaypointDone(waypoint: Waypoint, waypointState: Int) {
        viewModel.onWaypointDone(waypoint, waypointState)
    }

    override fun editWaypoint(waypoint: Waypoint) {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.edit_waypoint_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        // Get the dialog fields
        val waypointNameET = dialog.findViewById<EditText>(R.id.et_waypoint_name)
        waypointNameET.setText(waypoint.name)
        val waypointCoordinatesET = dialog.findViewById<EditText>(R.id.et_waypoint_coordinates)
        waypointCoordinatesET.setText(waypoint.latitude?.let {
            waypoint.longitude?.let { it1 ->
                Coordinate.prettyPrint(
                    it, it1
                )
            }
        })
        val waypointNotesET = dialog.findViewById<EditText>(R.id.et_waypoint_notes)
        waypointNotesET.setText(waypoint.notes)

        // Setup button to delete waypoint
        val deleteButton = dialog.findViewById<Button>(R.id.delete_button)
        deleteButton.setOnClickListener {
            deleteWaypointDialog(waypoint)
            dialog.dismiss()
        }

        // Setup button to accept the changes made to the waypoint
        val acceptChangesButton = dialog.findViewById<Button>(R.id.accept_changes_button)
        acceptChangesButton.setOnClickListener {
            val name: String= waypointNameET.text.toString()
            val coordinateString: String = waypointCoordinatesET.text.toString()
            val notes: String = waypointNotesET.text.toString()
            viewModel.updateWaypoint(waypoint, name, coordinateString, notes)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteWaypointDialog(waypoint: Waypoint){
        val alertDialogBuilder = AlertDialog.Builder(requireActivity())
        alertDialogBuilder.setMessage("Are you sure you want to delete this waypoint? This is irreversible.")
            .setPositiveButton("Yes") { _, _ -> viewModel.deleteWaypoint(waypoint) }
            .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}
    }

}