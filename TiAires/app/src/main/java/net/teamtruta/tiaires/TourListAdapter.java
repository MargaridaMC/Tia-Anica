package net.teamtruta.tiaires;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import net.teamtruta.tiaires.data.GeocachingTourWithCaches;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TourListAdapter extends RecyclerView.Adapter<TourListAdapter.TourViewHolder> {

    private final List<GeocachingTourWithCaches> _tourList;
    private final ItemClickListener onClickListener;
    private final Context context;

    // data is passed into the constructor
    TourListAdapter(List<GeocachingTourWithCaches>data, ItemClickListener listener, Context applicationContext){

        this._tourList = data;
        this.onClickListener = listener;
        this.context = applicationContext;
    }

    // inflates the row layout from xml when needed
    @NotNull
    @Override
    public TourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_tour_layout, parent, false);
        return new TourViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(TourViewHolder holder, int position) {

        GeocachingTourWithCaches tour = _tourList.get(position);

        // Set tour title
        holder.tourTitle.setText(tour.getTour().getName());

        // Set tour symbol
        if(tour.getTour().isCurrentTour()){
            holder.tourSymbol.setImageResource(R.drawable.star);
        }

        // Write progress in text
        long numFinds = tour.getNumFound();
        long numDNFS = tour.getNumDNF();
        long totalGeoCaches = tour.getSize();// # TODO -- this breaks the OO model...

        String progressText = numFinds + " + " + numDNFS + " / " + totalGeoCaches;
        holder.tourProgressText.setText(progressText);

        // Fill in progress
        double progress = ((double) numFinds + (double) numDNFS) / (double) totalGeoCaches * 100.;
        holder.tourProgress.setProgress((int) progress);

        // Grey out section if tour is done

        if(numFinds + numDNFS == totalGeoCaches){
            holder.layout.setBackgroundColor(context.getColor(R.color.light_grey));
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
                //String tourName = _tourList.get(position).getTour().getName();
                onClickListener.onItemClick(view, position, _tourList.get(position).getTour().getId());
            }
        }

    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener  {
        void onItemClick(View view, int position, Long tourID);
    }
}
