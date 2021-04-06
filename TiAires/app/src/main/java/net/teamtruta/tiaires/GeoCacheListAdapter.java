package net.teamtruta.tiaires;

import android.content.Context;
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

import net.teamtruta.tiaires.data.GeoCache;
import net.teamtruta.tiaires.data.GeoCacheAttribute;
import net.teamtruta.tiaires.data.GeoCacheInTour;
import net.teamtruta.tiaires.data.GeoCacheInTourWithDetails;
import net.teamtruta.tiaires.data.GeoCacheLog;
import net.teamtruta.tiaires.data.GeoCacheWithLogsAndAttributes;
import net.teamtruta.tiaires.viewModels.TourViewModel;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


class GeoCacheListAdapter extends RecyclerView.Adapter<GeoCacheListAdapter.ViewHolder> implements ItemTouchHelperAdapter{

    private static List<GeoCacheInTourWithDetails> geoCacheInTourList = new ArrayList();
    private final EditOnClickListener editOnClickListener;
    private final GoToOnClickListener goToOnClickListener;
    private final Context context;
    static TourViewModel viewModel;

    static OnVisitListener onVisitListener;


    GeoCacheListAdapter(EditOnClickListener editOnClickListener,
                        GoToOnClickListener goToOnClickListener,
                        Context applicationContext, TourViewModel viewModel){
        this.editOnClickListener = editOnClickListener;
        this.goToOnClickListener = goToOnClickListener;
        this.context = applicationContext;
        this.viewModel = viewModel;
    }

    public void setGeoCacheInTourList(List<GeoCacheInTourWithDetails> list){
        this.geoCacheInTourList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){
        return geoCacheInTourList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){

        GeoCacheInTour geoCacheInTour = geoCacheInTourList.get(position).getGeoCacheInTour();
        GeoCacheWithLogsAndAttributes geoCacheWithLogsAndAttributes = geoCacheInTourList.get(position).getGeoCache();
        GeoCache geoCache = geoCacheWithLogsAndAttributes.getGeoCache();

        // 1. Set geoCache name
        holder.geoCacheName.setText(geoCache.getName());

        // 2. Set geoCache type symbol
        int drawableID;

        if(geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.Found){
            drawableID = R.drawable.geo_cache_icon_found;
        } else if(geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.DNF){
            drawableID = R.drawable.geo_cache_icon_dnf;
        } else if(geoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.Disabled){
            drawableID = R.drawable.geo_cache_icon_disabled;
        } else {
            drawableID = GeoCacheIcon.Companion.getIconDrawable(geoCache.getType());
        }

        Drawable geoCacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), drawableID);
        holder.geoCacheSymbol.setImageDrawable(geoCacheSymbolDrawable);

        // 3. Set information line 1: Code, difficulty and terrain and Size
        holder.geoCacheCode.setText(geoCache.getCode());

        String difTerString = context.getString(R.string.geo_cache_dif_ter);
        double difficulty = geoCache.getDifficulty();
        double terrain = geoCache.getTerrain();
        
        String diffString = String.format(Locale.getDefault(), "%.1f", difficulty);
        String terString = String.format(Locale.getDefault(), "%.1f", terrain);
        diffString = ((difficulty > 4) ?
                "<font color='red'>" + diffString + "</font>/" : diffString);
        terString = ((terrain > 4) ?
                "<font color='red'>" + terString + "</font>/" : terString);
        holder.geoCacheDifTer.setText( HtmlCompat.fromHtml(String.format(difTerString, diffString, terString),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        String sizeString = context.getString(R.string.geo_cache_size);
        holder.geoCacheSize.setText(String.format(sizeString, geoCache.getSize()));


        // 4. Set information line 2: Favorites and whether geoCache has hint
        String favString = context.getString(R.string.geo_cache_favs);
        holder.geoCacheFavs.setText(String.format(favString, geoCache.getFavourites()));
        if(geoCache.hasHint()){
            String hintString = context.getString(R.string.geo_cache_has_hint);
            holder.geoCacheHasHint.setText(HtmlCompat.fromHtml(
                    hintString,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            holder.geoCacheHasHint.setText(HtmlCompat.fromHtml(context.getString(R.string.geo_cache_has_no_hint),
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

        List<GeoCacheLog> last10Logs = geoCacheWithLogsAndAttributes.getLastNLogs(10);
        int i = 0;
        for (GeoCacheLog log:last10Logs) {
            TextView tv = (TextView) holder.lastLogsLayout.getChildAt(i);
            if(log.getLogType() == VisitOutcomeEnum.Found){
                tv.setTextColor(holder.view.getContext().getColor(R.color.colorPrimary));
            } else if( log.getLogType() == VisitOutcomeEnum.DNF ){
                tv.setTextColor(holder.view.getContext().getColor(R.color.red));
            } else {
                tv.setTextColor(holder.view.getContext().getColor(R.color.blue));
            }
            i++;
        }


        // 5. GeoCache Hint
        holder.hint.setText(getHintText(geoCache));


        // 6. Set DNF information if required
        if(geoCacheWithLogsAndAttributes.isDNFRisk()){
            String dnfString = context.getString(R.string.dnf_risk);
            dnfString = String.format(dnfString, geoCacheWithLogsAndAttributes.getDNFRisk());

            holder.dnfInfo.setText(HtmlCompat.fromHtml(
                    dnfString,
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
            holder.dnfInfo.setVisibility(View.VISIBLE);
        } else {
            holder.dnfInfo.setText("");
            holder.dnfInfo.setVisibility(View.GONE);
        }

       // 7. Set listener for edit button
        holder.editButton.setOnClickListener(v -> {
            if(editOnClickListener!=null){
                editOnClickListener.onEditClick(geoCacheInTour.getId());
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
            holder.layout.setBackgroundColor(context.getColor(R.color.light_grey));
        } else {
            holder.layout.setBackgroundColor(context.getColor(R.color.white));
        }

        // 10. Setup expansion
        holder.extraInfoArrow.setOnClickListener(v -> holderExpansionOnClicklistener(holder, geoCacheWithLogsAndAttributes));

        // 11. Add Cache Attributes
        holder.attributeList.removeAllViews();
        if(geoCacheWithLogsAndAttributes.getAttributes().size() > 0){
            for(GeoCacheAttribute attribute: geoCacheWithLogsAndAttributes.getAttributes()){
                ImageView iv = new ImageView(context);
                int attributeIconID = GeoCacheAttributeIcon.Companion.getGeoCacheAttributeIcon(attribute.getAttributeType());
                iv.setImageDrawable(ContextCompat.getDrawable(context, attributeIconID));
                // DEPRECATED:
                //iv.setImageDrawable(App.getContext().getDrawable(attributeIconID));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        (int) context.getResources().getDimension(R.dimen.medium_icon_size),
                        (int) context.getResources().getDimension(R.dimen.medium_icon_size));
                layoutParams.setMarginEnd((int) context.getResources().getDimension(R.dimen.tiny_padding));
                iv.setLayoutParams(layoutParams);
                holder.attributeList.addView(iv);
            }
        }
    }


    private Spanned getHintText(GeoCache geoCache) {

        String hintString = "<strong>HINT</strong>: <i>" + geoCache.getHint() + "</i>";
        return HtmlCompat.fromHtml(hintString, HtmlCompat.FROM_HTML_MODE_LEGACY);

    }

    void holderExpansionOnClicklistener(ViewHolder holder, GeoCacheWithLogsAndAttributes geoCacheWithLogsAndAttributes){
        if(holder.extraInfoArrow.isChecked() || holder.extraInfoLayout.getVisibility() != View.VISIBLE){
            expandHolder(holder, geoCacheWithLogsAndAttributes);
        } else {
            unexpandHolder(holder);
        }
    }
    void expandHolder(ViewHolder holder, GeoCacheWithLogsAndAttributes geoCacheWithLogsAndAttributes){

        // Remove hint indication from line 1
        if(geoCacheWithLogsAndAttributes.getGeoCache().hasHint()){
            holder.geoCacheHasHint.setVisibility(View.GONE);
            holder.hint.setVisibility(View.VISIBLE);
        }

        // Show extra information
        holder.extraInfoLayout.setVisibility(View.VISIBLE);

        holder.extraInfoArrow.setChecked(true);

        // Show attributes
        if(geoCacheWithLogsAndAttributes.getAttributes().size() != 0)
            holder.attributeList.setVisibility(View.VISIBLE);

    }

    void unexpandHolder(ViewHolder holder){
        // Hide extra information
        holder.geoCacheHasHint.setVisibility(View.VISIBLE);
        holder.hint.setVisibility(View.GONE);

        holder.extraInfoLayout.setVisibility(View.GONE);
        holder.extraInfoArrow.setChecked(false);

        // Hide attributes
        holder.attributeList.setVisibility(View.GONE);
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

        GeoCacheInTour selectedGeoCache = geoCacheInTourList.get(position).getGeoCacheInTour();
        selectedGeoCache.setCurrentVisitOutcome(visit);
        selectedGeoCache.setCurrentVisitDatetime(Instant.now());
        viewModel.updateGeoCacheInTour(selectedGeoCache);

        onVisitListener.onVisit(visit.toString());
    }

    //@Override
    //public void onItemDismiss(int position) {
        //tour._tourCaches.remove(position);
        //notifyItemRemoved(position);
    //}

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        Collections.swap(geoCacheInTourList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        /*
        tour.swapCachePositions(fromPosition, toPosition);
        tour.updateTourCaches();*/

    }

    public void onMoveEnded(){
        /*tour.updateTourCaches();*/
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
