package net.teamtruta.tiaires;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TourListAdapter extends RecyclerView.Adapter<TourListAdapter.TourViewHolder> {

    private ArrayList<GeocachingTour> _tourList;
    private LayoutInflater mInflater;
    private ItemClickListener onClickListener;

    // data is passed into the constructor
    TourListAdapter(Context context,  ArrayList<GeocachingTour> data, ItemClickListener listener) {

        this.mInflater = LayoutInflater.from(context);
        _tourList = data;
        this.onClickListener = listener;
    }

    // inflates the row layout from xml when needed
    @Override
    public TourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.element_tour_layout, parent, false);
        return new TourViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(TourViewHolder holder, int position) {
        //String animal = mData._tourList(position);
        GeocachingTour tour = _tourList.get(position);

        // Set tour title
        holder.tourTitle.setText(tour.getName());

        // Set tour symbol
        if(tour.isCurrentTour){
            holder.tourSymbol.setImageResource(R.drawable.star);
        }

        // Write progress in text
        int numFinds = tour.getNumFound();
        int numDNFS = tour.getNumDNF();
        int totalCaches = tour._size;
        String progressText = numFinds + " + " + numDNFS + " / " + totalCaches;
        holder.tourProgressText.setText(progressText);

        // Fill in progress
        double progress = ((double) numFinds + (double) numDNFS) / (double) totalCaches * 100.;
        holder.tourProgress.setProgress((int) progress);

        // Grey out section if tour is done
        if(numFinds + numDNFS == totalCaches){
            holder.layout.setBackgroundColor(Color.parseColor("#DCDCDC"));
        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return _tourList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class TourViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tourTitle;
        ImageView tourSymbol;
        ProgressBar tourProgress;
        TextView tourProgressText;
        ConstraintLayout layout;

        TourViewHolder(View itemView) {
            super(itemView);

            tourTitle = itemView.findViewById(R.id.tour_title);
            tourSymbol = itemView.findViewById(R.id.tour_symbol);
            tourProgress = itemView.findViewById(R.id.tour_progress);
            tourProgressText = itemView.findViewById(R.id.tour_progress_text);
            layout = itemView.findViewById(R.id.constraintLayout);
            
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (onClickListener != null) {
                int position = getAdapterPosition();
                String tourName = _tourList.get(position).getName();
                onClickListener.onItemClick(view, position, tourName);
            }
        }

    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener  {
        void onItemClick(View view, int position, String tourName);
    }
}
