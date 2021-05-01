package net.teamtruta.tiaires.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.Coordinate
import net.teamtruta.tiaires.data.models.Waypoint

class WaypointListAdapter(private val goToOnClickListener: GoToOnClickListener,
                          private val waypointDoneOnClickListener: WaypointDoneOnClickListener)
    : RecyclerView.Adapter<WaypointListAdapter.ViewHolder>() {

    private var waypoints: List<Waypoint> = listOf()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val waypointCheckBox: CheckBox = view.findViewById(R.id.waypoint_checkbox)
        val waypointName: TextView = view.findViewById(R.id.tv_waypoint_name)
        val waypointCoordinates: TextView = view.findViewById(R.id.tv_waypoint_coordinates)
        val goToWaypointButton: Button = view.findViewById(R.id.go_to_button)
    }

    fun setData(waypointList: List<Waypoint>){
        this.waypoints = waypointList
        notifyDataSetChanged()
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_waypoint_list, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view with the actual contents
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val waypoint = waypoints[position]
        holder.waypointCheckBox.isChecked = waypoint.isDone || waypoint.isParking
        holder.waypointCheckBox.setOnClickListener{
            waypointDoneOnClickListener.onWaypointDone(waypoint, holder.waypointCheckBox.isChecked)}
        holder.waypointName.text = waypoint.name
        holder.waypointCoordinates.text = Coordinate.prettyPrint(waypoint.latitude, waypoint.longitude)
        holder.goToWaypointButton.setOnClickListener { goToOnClickListener.onGoToClick(waypoint) }
    }

    override fun getItemCount(): Int {
        return waypoints.size
    }

    interface GoToOnClickListener {
        fun onGoToClick(waypoint: Waypoint)
    }

    interface  WaypointDoneOnClickListener {
        fun onWaypointDone(waypoint:Waypoint, done: Boolean)
    }
}