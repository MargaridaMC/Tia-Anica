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


class GeoCacheListAdapter extends RecyclerView.Adapter<GeoCacheListAdapter.ViewHolder> implements ItemTouchHelperAdapter{

    private static GeocachingTour tour;
    private final EditOnClickListener editOnClickListener;
    private final GoToOnClickListener goToOnClickListener;

    static OnVisitListener onVisitListener;

    //private static GeoCacheInTour recentlyVisitedCache;
    //private static int recentlyVisitedGeoCachePosition;


    GeoCacheListAdapter(GeocachingTour tour, EditOnClickListener editOnClickListener, GoToOnClickListener goToOnClickListener){
        GeoCacheListAdapter.tour = tour;
        this.editOnClickListener = editOnClickListener;
        this.goToOnClickListener = goToOnClickListener;
    }
    @Override
    public int getItemCount(){
        return (int) tour.getSize();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){

        GeoCacheInTour geoCacheInTour = tour._tourGeoCaches.get(position);
        GeoCache geoCache = geoCacheInTour.getGeoCache();

        // 1. Set geoCache name
        holder.geoCacheName.setText(geoCache.getName());

        // 2. Set geoCache type symbol
        int drawableID;

        if(geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.Found){
            drawableID = R.drawable.geo_cache_icon_found;
        } else if(geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.DNF){
            drawableID = R.drawable.geo_cache_icon_dnf;
        } else if(geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.Disabled){
            drawableID = R.drawable.geo_cache_icon_disabled50x50;
        } else {
            drawableID = GeoCacheIcon.getIconDrawable(geoCacheInTour.getGeoCache().getType());
        }

        Drawable geoCacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), drawableID);
        holder.geoCacheSymbol.setImageDrawable(geoCacheSymbolDrawable);

        // 3. Set information line 1: Code, difficulty and terrain and Size
        holder.geoCacheCode.setText(geoCache.getCode());

        String difTerString = App.getContext().getString(R.string.geo_cache_dif_ter);
        String difficulty = geoCache.getDifficulty();
        String terrain = geoCache.getTerrain();
        String diffString = ((Double.parseDouble(difficulty) > 4) ?
                "<font color='red'>" + difficulty + "</font>/" : difficulty);
        String terString = ((Double.parseDouble(terrain) > 4) ?
                "<font color='red'>" + terrain + "</font>/" : terrain);
        holder.geoCacheDifTer.setText( HtmlCompat.fromHtml(String.format(difTerString, diffString, terString),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        String sizeString = App.getContext().getString(R.string.geo_cache_size);
        holder.geoCacheSize.setText(String.format(sizeString, geoCache.getSize()));


        // 4. Set information line 2: Favorites and whether geoCache has hint
        String favString = App.getContext().getString(R.string.geo_cache_favs);
        holder.geoCacheFavs.setText(String.format(favString, geoCache.getFavourites()));
        if(geoCache.hasHint()){
            holder.geoCacheHasHint.setText(App.getContext().getString(R.string.geo_cache_has_hint));
        } else {
            holder.geoCacheHasHint.setText(App.getContext().getString(R.string.geo_cache_has_no_hint));
        }

        List<GeoCacheLog> last10Logs = geoCache.getLastNLogs(10);
        for(int i=0; i<10;i++){
            TextView tv = (TextView) holder.lastLogsLayout.getChildAt(i);
            if(last10Logs.get(i).getLogType() == VisitOutcomeEnum.Found){
                tv.setTextColor(holder.view.getContext().getColor(R.color.colorPrimary));
            } else if( last10Logs.get(i).getLogType() == VisitOutcomeEnum.DNF ){
                tv.setTextColor(holder.view.getContext().getColor(R.color.red));
            } else {
                tv.setTextColor(holder.view.getContext().getColor(R.color.blue));
            }

        }


        // 5. GeoCache Hint
        holder.hint.setText(getHintText(geoCache));


        // 6. Set DNF information if required
        if(geoCache.isDNFRisk()){

            String dnfString = App.getContext().getString(R.string.dnf_risk);
            dnfString = String.format(dnfString, geoCache.getDNFRisk());

            holder.dnfInfo.setText(HtmlCompat.fromHtml(
                    dnfString,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
            holder.dnfInfo.setVisibility(View.VISIBLE);
        }

       // 7. Set listener for edit button
        holder.editButton.setOnClickListener(v -> {
            if(editOnClickListener!=null){
                editOnClickListener.onEditClick(geoCacheInTour.get_id());
            }
        });

        // 8. Set listener for go-to-geoCache button
        holder.goToButton.setOnClickListener(v -> {
            if(goToOnClickListener!=null){
                goToOnClickListener.onGoToClick(geoCache);
            }
        });

        // 9. Make section grey if geoCache has been visited
        if(geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.Found
                || geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.DNF
                || geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.Disabled){
            holder.layout.setBackgroundColor(App.getContext().getColor(R.color.light_grey));
        } else {
            holder.layout.setBackgroundColor(App.getContext().getColor(R.color.white));
        }

        // 10. Setup expansion
        holder.view.setOnClickListener(v -> expandHolder(holder, geoCache));
        holder.extraInfoArrow.setOnClickListener(v -> expandHolder(holder, geoCache));

        // 11. Add Cache Attributes
        if(geoCache.getAttributes().size() > 0){
            for(GeoCacheAttributeEnum attribute: geoCache.getAttributes()){
                ImageView iv = new ImageView(App.getContext());
                iv.setImageDrawable(App.getContext().getDrawable(
                        GeoCacheAttributeIcon.getGeoCacheAttributeIcon(attribute)));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        (int) App.getContext().getResources().getDimension(R.dimen.medium_icon_size),
                        (int) App.getContext().getResources().getDimension(R.dimen.medium_icon_size));
                layoutParams.setMarginEnd((int) App.getContext().getResources().getDimension(R.dimen.tiny_padding));
                iv.setLayoutParams(layoutParams);
                holder.attributeList.addView(iv);
            }
        }

    }

    private Spanned getHintText(GeoCache geoCache) {

        String hintString = "<strong>HINT</strong>: <i>" + geoCache.getHint() + "</i>";
        return HtmlCompat.fromHtml(hintString, HtmlCompat.FROM_HTML_MODE_LEGACY);

    }

    void expandHolder(ViewHolder holder, GeoCache geoCache){

        if(holder.extraInfoArrow.isChecked() || holder.extraInfoLayout.getVisibility() != View.VISIBLE){

            // Remove hint indication from line 1
            if(geoCache.hasHint()){
                holder.geoCacheHasHint.setVisibility(View.GONE);
                holder.hint.setVisibility(View.VISIBLE);
            }

            // Show extra information
            holder.extraInfoLayout.setVisibility(View.VISIBLE);

            holder.extraInfoArrow.setChecked(true);

            // Show attributes
            if(geoCache.getAttributes().size() != 0)
                holder.attributeList.setVisibility(View.VISIBLE);

        } else {
            // Hide extra information
            if(geoCache.hasHint()){
                holder.geoCacheHasHint.setVisibility(View.VISIBLE);
                holder.hint.setVisibility(View.GONE);
            }

            holder.extraInfoLayout.setVisibility(View.GONE);
            holder.extraInfoArrow.setChecked(false);

            // Hide attributes
            holder.attributeList.setVisibility(View.GONE);
        }
    }

    @NotNull
    public GeoCacheListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_geo_cache_layout, parent, false);
        return new ViewHolder(v);
    }

    static void visitItem(int position, VisitOutcomeEnum visit){

        // Save the deleted item in case user wants to undo the action;

        // Useful if we want to add an undo functionality here
        // recentlyVisitedCache = tour.getCacheInTour(position);
        // recentlyVisitedCachePosition = position;

        GeoCacheInTour selectedGeoCache = tour._tourGeoCaches.get(position);
        selectedGeoCache.setCurrentVisitOutcome(visit);
        selectedGeoCache.saveChanges();

        onVisitListener.onVisit(visit.toString());
    }

    @Override
    public void onItemDismiss(int position) {
        //tour._tourCaches.remove(position);
        //notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        Collections.swap(tour._tourGeoCaches, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        //tour.swapCachePositions(fromPosition, toPosition);
        tour.updateTourCaches();

        return true;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView geoCacheName;
        ImageView geoCacheSymbol;
        TextView geoCacheCode;
        TextView geoCacheDifTer;
        TextView geoCacheSize;
        TextView geoCacheFavs;
        TextView geoCacheHasHint;
        TextView dnfInfo;
        TextView dnfInfoExpanded;
        TextView hint;
        Button editButton;
        Button goToButton;
        ConstraintLayout layout;
        ConstraintLayout extraInfoLayout;
        CheckBox extraInfoArrow;
        GridLayout lastLogsLayout;
        LinearLayout attributeList;

        ViewHolder(View v){
            super(v);
            view = v;

            geoCacheName = v.findViewById(R.id.geo_cache_title);
            geoCacheSymbol = v.findViewById(R.id.geo_cache_symbol);
            geoCacheCode = v.findViewById(R.id.geo_cache_code);
            geoCacheDifTer = v.findViewById(R.id.geo_cache_dif_ter);
            geoCacheSize = v.findViewById(R.id.geo_cache_size);
            geoCacheFavs = v.findViewById(R.id.geo_cache_favs);
            geoCacheHasHint = v.findViewById(R.id.geo_cache_has_hint);
            dnfInfo = v.findViewById(R.id.dnf_risk);
            dnfInfoExpanded = v.findViewById(R.id.dnf_info_expanded);
            hint = v.findViewById(R.id.hint);
            editButton = v.findViewById(R.id.edit_button);
            goToButton = v.findViewById(R.id.go_to_button);
            layout = v.findViewById(R.id.element_geo_cache_layout);
            extraInfoLayout = v.findViewById(R.id.expandable_info);
            extraInfoArrow = v.findViewById(R.id.extra_info_arrow);
            lastLogsLayout = v.findViewById(R.id.last10LogsSquares);
            attributeList = v.findViewById(R.id.geo_cache_attributes);
        }

    }

    interface EditOnClickListener {
        void onEditClick(long geoCacheID);
    }

    interface GoToOnClickListener{
        void onGoToClick(GeoCache geoCache);
    }

    interface OnVisitListener{
        void onVisit(String visit);
    }
}
