package net.teamtruta.tiaires.adapters

import android.content.Context
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import androidx.recyclerview.widget.RecyclerView
import net.teamtruta.tiaires.adapters.TourListAdapter.TourViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import net.teamtruta.tiaires.R

class TourListAdapter(private val _tourList: List<GeocachingTourWithCaches>,
                      private val onClickListener: ItemClickListener,
                      private val context: Context) : RecyclerView.Adapter<TourViewHolder>() {
    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_tour_layout, parent, false)
        return TourViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val tour = _tourList[position]

        // Set tour title
        holder.tourTitle.text = tour.tour.name

        // Set tour symbol
        if (tour.tour.isCurrentTour) {
            holder.tourSymbol.setImageResource(R.drawable.star)
        }

        // Write progress in text
        val numFinds = tour.getNumFound().toLong()
        val numDNFS = tour.getNumDNF().toLong()
        val totalGeoCaches = tour.getSize().toLong() // # TODO -- this breaks the OO model...
        val progressText = "$numFinds + $numDNFS / $totalGeoCaches"
        holder.tourProgressText.text = progressText

        // Fill in progress
        val progress = (numFinds.toDouble() + numDNFS.toDouble()) / totalGeoCaches.toDouble() * 100.0
        holder.tourProgress.progress = progress.toInt()

        // Grey out section if tour is done
        if (numFinds + numDNFS == totalGeoCaches) {
            holder.layout.setBackgroundColor(context.getColor(R.color.light_grey))
        }
    }

    // total number of rows
    override fun getItemCount(): Int {
        return _tourList.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class TourViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val tourTitle: TextView = itemView.findViewById(R.id.tour_title)
        val tourSymbol: ImageView = itemView.findViewById(R.id.tour_symbol)
        val tourProgress: ProgressBar = itemView.findViewById(R.id.tour_progress)
        val tourProgressText: TextView = itemView.findViewById(R.id.tour_progress_text)
        val layout: ConstraintLayout = itemView.findViewById(R.id.constraintLayout)
        override fun onClick(view: View) {
            val position = adapterPosition
            onClickListener.onItemClick(view, position, _tourList[position].tour.id)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int, tourID: Long)
    }
}