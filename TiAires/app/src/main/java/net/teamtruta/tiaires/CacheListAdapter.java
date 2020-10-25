package net.teamtruta.tiaires;

import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


class CacheListAdapter extends RecyclerView.Adapter<CacheListAdapter.ViewHolder> implements ItemTouchHelperAdapter{

    private static GeocachingTour tour;
    private EditOnClickListener editOnClickListener;
    private GoToOnClickListener goToOnClickListener;

    static OnVisitListener onVisitListener;

    //private static GeocacheInTour recentlyVisitedCache;
    //private static int recentlyVisitedCachePosition;


    CacheListAdapter(GeocachingTour tour, EditOnClickListener editOnClickListener, GoToOnClickListener goToOnClickListener){
        CacheListAdapter.tour = tour;
        this.editOnClickListener = editOnClickListener;
        this.goToOnClickListener = goToOnClickListener;
    }
    @Override
    public int getItemCount(){
        return (int) tour.getSize();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){

        GeocacheInTour geocacheInTour = tour._tourCaches.get(position);
        Geocache geocache = geocacheInTour.getGeocache();

        // 1. Set cache name
        holder.cacheName.setText(geocache.getName());

        // 2. Set cache type symbol
        int drawableID;

        if(geocacheInTour.getVisit() == FoundEnumType.Found){
            drawableID = R.drawable.cache_icon_found;
        } else if(geocacheInTour.getVisit() == FoundEnumType.DNF){
            drawableID = R.drawable.cache_icon_dnf;
        } else if(geocacheInTour.getVisit() == FoundEnumType.Disabled){
            drawableID = R.drawable.cache_icon_disabled50x50;
        } else {
            drawableID = GeocacheIcon.getIconDrawable(geocacheInTour.getGeocache().getType());
        }

        Drawable cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), drawableID);
        holder.cacheSymbol.setImageDrawable(cacheSymbolDrawable);

        // 3. Set information line 1: Code, difficulty and terrain and Size
        holder.cacheCode.setText(geocache.getCode());

        String difTerString = App.getContext().getString(R.string.cache_dif_ter);
        String difficulty = geocache.getDifficulty();
        String terrain = geocache.getTerrain();
        String diffString = ((Double.parseDouble(difficulty) > 4) ?
                "<font color='red'>" + difficulty + "</font>/" : difficulty);
        String terString = ((Double.parseDouble(terrain) > 4) ?
                "<font color='red'>" + terrain + "</font>/" : terrain);
        holder.cacheDifTer.setText( HtmlCompat.fromHtml(String.format(difTerString, diffString, terString),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        String sizeString = App.getContext().getString(R.string.cache_size);
        holder.cacheSize.setText(String.format(sizeString, geocache.getSize()));


        // 4. Set information line 2: Favorites and whether cache has hint
        String favString = App.getContext().getString(R.string.cache_favs);
        holder.cacheFavs.setText(String.format(favString, geocache.getFavourites()));
        if(geocache.hasHint()){
            holder.cacheHasHint.setText(App.getContext().getString(R.string.cache_has_hint));
        }

        List<GeocacheLog> last10Logs = geocache.getLastNLogs(10);
        for(int i=0; i<10;i++){

            ImageView iv = new ImageView(App.getContext());

            if(last10Logs.get(i).getLogType() == FoundEnumType.Found){
                iv.setImageDrawable(ContextCompat.getDrawable(holder.view.getContext(), R.drawable.green_square));
            } else if( last10Logs.get(i).getLogType() == FoundEnumType.DNF ){
                iv.setImageDrawable(ContextCompat.getDrawable(holder.view.getContext(), R.drawable.red_square));
            } else {
                iv.setImageDrawable(ContextCompat.getDrawable(holder.view.getContext(), R.drawable.blue_square));
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(4,0,4,0);
            iv.setLayoutParams(layoutParams);

            holder.lastLogsLayout.addView(iv, i);

        }


        // 5. Cache Hint
        holder.hint.setText(getHintText(geocache));


        // 6. Set DNF information if required
        Spanned dnfInfo = getDNFInfo(geocache);
        if(!dnfInfo.toString().equals("")) { // Cache is a DNF risk
            holder.dnfInfo.setText(dnfInfo);
            holder.dnfInfo.setVisibility(View.VISIBLE);

            String dnfRiskString = "<b>DNF Risk</b>:  <i>" + geocache.getDNFRisk() + "</i>";
            holder.dnfInfoExpanded.setText(HtmlCompat.fromHtml(dnfRiskString, HtmlCompat.FROM_HTML_MODE_LEGACY));
            holder.dnfInfoExpanded.setVisibility(View.VISIBLE);

        }


        // 7. Set listener for edit button
        holder.editButton.setOnClickListener(v -> {
            if(editOnClickListener!=null){
                editOnClickListener.onEditClick(geocacheInTour.get_id());
            }
        });

        // 8. Set listener for go-to-cache button
        holder.goToButton.setOnClickListener(v -> {
            if(goToOnClickListener!=null){
                goToOnClickListener.onGoToClick(geocache);
            }
        });

        // 9. Make section grey if cache has been visited
        if(geocacheInTour.getVisit() == FoundEnumType.Found || geocacheInTour.getVisit() == FoundEnumType.DNF){
            holder.layout.setBackgroundColor(App.getContext().getColor(R.color.light_grey));
        } else {
            holder.layout.setBackgroundColor(App.getContext().getColor(R.color.white));
        }

        // 10. Setup expansion
        holder.view.setOnClickListener(v -> expandHolder(holder, geocache));
        holder.extraInfoArrow.setOnClickListener(v -> expandHolder(holder, geocache));

    }

    private Spanned getHintText(Geocache geocache) {

        String hintString = "<strong>HINT</strong>: <i>" + geocache.getHint() + "</i>";
        return HtmlCompat.fromHtml(hintString, HtmlCompat.FROM_HTML_MODE_LEGACY);

    }

    void expandHolder(ViewHolder holder, Geocache geocache){

        if(holder.extraInfoArrow.isChecked() || holder.extraInfoLayout.getVisibility() != View.VISIBLE){

            // Remove hint indication from line 1
            if(geocache.hasHint()){
                holder.cacheHasHint.setVisibility(View.INVISIBLE);
                holder.hint.setVisibility(View.VISIBLE);
            }

            // Show extra information
            holder.extraInfoLayout.setVisibility(View.VISIBLE);

            // Show DNF information if needed
            if(geocache.isDNFRisk())
                holder.dnfInfo.setVisibility(View.GONE);

        } else {
            // Hide extra information
            if(geocache.hasHint()){
                holder.cacheHasHint.setVisibility(View.VISIBLE);
                holder.hint.setVisibility(View.GONE);
            }

            holder.extraInfoLayout.setVisibility(View.GONE);
            if(geocache.isDNFRisk())
                holder.dnfInfo.setVisibility(View.VISIBLE);

        }
    }

    private Spanned getDNFInfo(Geocache geocache){

        String info = "";
        if(geocache.isDNFRisk()){
            String red = String.valueOf(App.getContext().getColor(R.color.red));
            info = "<font color=\"" + red + "\">" + geocache.getDNFRiskShort() + "</font>";
        }

        return HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    @NotNull
    public CacheListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_cache_layout, parent, false);
        return new ViewHolder(v);
    }

    static void visitItem(int position, FoundEnumType visit){

        // Save the deleted item in case user wants to undo the action;

        // Useful if we want to add an undo functionality here
        // recentlyVisitedCache = tour.getCacheInTour(position);
        // recentlyVisitedCachePosition = position;

        GeocacheInTour selectedGeocache = tour._tourCaches.get(position);
        selectedGeocache.setVisit(visit);
        selectedGeocache.saveChanges();

        onVisitListener.onVisit(visit.toString());
    }

    @Override
    public void onItemDismiss(int position) {
        //tour._tourCaches.remove(position);
        //notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        Collections.swap(tour._tourCaches, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        //tour.swapCachePositions(fromPosition, toPosition);
        tour.updateTourCaches();

        return true;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView cacheName;
        ImageView cacheSymbol;
        TextView cacheCode;
        TextView cacheDifTer;
        TextView cacheSize;
        TextView cacheFavs;
        TextView cacheHasHint;
        TextView dnfInfo;
        TextView dnfInfoExpanded;
        TextView hint;
        Button editButton;
        Button goToButton;
        ConstraintLayout layout;
        ConstraintLayout extraInfoLayout;
        CheckBox extraInfoArrow;
        GridLayout lastLogsLayout;

        ViewHolder(View v){
            super(v);
            view = v;

            cacheName = v.findViewById(R.id.cache_title);
            cacheSymbol = v.findViewById(R.id.cache_symbol);
            cacheCode = v.findViewById(R.id.cache_code);
            cacheDifTer = v.findViewById(R.id.cache_dif_ter);
            cacheSize = v.findViewById(R.id.cache_size);
            cacheFavs = v.findViewById(R.id.cache_favs);
            cacheHasHint = v.findViewById(R.id.cache_has_hint);
            dnfInfo = v.findViewById(R.id.dnf_info);
            dnfInfoExpanded = v.findViewById(R.id.dnf_info_expanded);
            hint = v.findViewById(R.id.hint);
            editButton = v.findViewById(R.id.edit_button);
            goToButton = v.findViewById(R.id.go_to_button);
            layout = v.findViewById(R.id.element_cache_layout);
            extraInfoLayout = v.findViewById(R.id.expandable_info);
            extraInfoArrow = v.findViewById(R.id.extra_info_arrow);
            lastLogsLayout = v.findViewById(R.id.last10LogsSquares);

        }

    }

    interface EditOnClickListener {
        void onEditClick(long cacheID);
    }

    interface GoToOnClickListener{
        void onGoToClick(Geocache geocache);
    }

    interface OnVisitListener{
        void onVisit(String visit);
    }
}
