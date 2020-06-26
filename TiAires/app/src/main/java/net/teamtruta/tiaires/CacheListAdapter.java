package net.teamtruta.tiaires;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

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

        // Set cache name
        holder.cacheName.setText(geocache.getName());

        // Set cache type symbol
        CacheTypeEnum cacheType = geocache.getType();
        int drawableID;

        if(geocacheInTour.getVisit() == FoundEnumType.Found){
            drawableID = R.drawable.cache_icon_found;
        } else if(geocacheInTour.getVisit() == FoundEnumType.DNF){
            drawableID = R.drawable.cache_icon_dnf;
        } else {

            switch (cacheType){
                case Traditional:
                    drawableID = R.drawable.cache_icon_type_traditional;
                    break;
                case Mystery:
                    drawableID = R.drawable.cache_icon_type_mystery;
                    break;
                case Multi:
                    drawableID = R.drawable.cache_icon_type_multi;
                    break;
                case Earth:
                    drawableID = R.drawable.cache_icon_type_earth;
                    break;
                case Letterbox:
                    drawableID = R.drawable.cache_icon_type_letterbox;
                    break;
                case Event:
                    drawableID = R.drawable.cache_icon_type_event;
                    break;
                case CITO:
                    drawableID = R.drawable.cache_icon_type_cito;
                    break;
                case Mega:
                    drawableID = R.drawable.cache_icon_type_mega;
                    break;
                case Giga:
                    drawableID = R.drawable.cache_icon_type_giga;
                    break;
                case Wherigo:
                    drawableID = R.drawable.cache_icon_type_wherigo;
                    break;
                case HQ:
                    drawableID = R.drawable.cache_icon_type_hq;
                    break;
                case GPSAdventures:
                    drawableID = R.drawable.cache_icon_type_gps_adventures;
                    break;
                case HQCelebration:
                    drawableID = R.drawable.cache_icon_type_hq_celebration;
                    break;
                case HQBlockParty:
                    drawableID = R.drawable.cache_icon_type_hq_blockparty;
                    break;
                case CommunityCelebration:
                    drawableID = R.drawable.cache_icon_type_community_event;
                    break;
                case Virtual:
                    drawableID = R.drawable.cache_icon_type_virtual;
                    break;
                case Webcam:
                    drawableID = R.drawable.cache_icon_type_webcam;
                    break;
                case ProjectAPE:
                    drawableID = R.drawable.cache_icon_type_project_ape;
                    break;
                case Locationless:
                    drawableID = R.drawable.cache_icon_type_locationless;
                    break;
                case Solved:
                    drawableID = R.drawable.cache_icon_solved;
                    break;
                default:
                    drawableID = R.drawable.shrug;
                    break;
            }
        }


        Drawable cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), drawableID);
        holder.cacheSymbol.setImageDrawable(cacheSymbolDrawable);

        holder.cacheInfo0.setText(getCacheInfoLine0(geocache));

        // Set information line 1: Found: date - \heart nFavs - Hint/No hint
        holder.cacheInfo1.setText(getCacheInfoLine1(geocache, true));

       List<GeocacheLog> last10Logs = geocache.getLastNLogs(10);
        for(int i=0; i<10;i++){
            Drawable square;
            if(last10Logs.get(i).getLogType() == FoundEnumType.Found){
                square = App.getContext().getDrawable(R.drawable.green_square);
            } else if( last10Logs.get(i).getLogType() == FoundEnumType.DNF ){
                square = App.getContext().getDrawable(R.drawable.red_square);
            } else {
                square = App.getContext().getDrawable(R.drawable.blue_square);
            }

            Context context = App.getContext();
            ImageView squareImageView = new ImageView(context);
            squareImageView.setImageDrawable(square);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(4,0,4,0);
            squareImageView.setLayoutParams(layoutParams);
            holder.lastLogsLayout.addView(squareImageView);

        }

      holder.hint.setText(getHintText(geocache));


        // Set DNF information if required
        Spanned dnfInfo = getDNFInfo(geocache);
        if(!dnfInfo.toString().equals("")) { // Cache is a DNF risk
            holder.dnfInfo.setText(dnfInfo);
            holder.dnfInfo.setVisibility(View.VISIBLE);

            String dnfRiskString = "<b>DNF Risk</b>:  <i>" + geocache.getDNFRisk() + "</i>";
            holder.dnfInfoExpanded.setText(HtmlCompat.fromHtml(dnfRiskString, HtmlCompat.FROM_HTML_MODE_LEGACY));
            holder.dnfInfoExpanded.setVisibility(View.VISIBLE);

        }


        // Set listener for edit button
        holder.editButton.setOnClickListener(v -> {
            if(editOnClickListener!=null){
                editOnClickListener.onEditClick(geocacheInTour.get_id());
            }
        });

        holder.goToButton.setOnClickListener(v -> {
            if(goToOnClickListener!=null){
                goToOnClickListener.onGoToClick(geocache);
            }
        });

        // Make section grey if cache has been visited
        if(geocacheInTour.getVisit() == FoundEnumType.Found || geocacheInTour.getVisit() == FoundEnumType.DNF){
            holder.layout.setBackgroundColor(App.getContext().getColor(R.color.light_grey));
        } else {
            holder.layout.setBackgroundColor(App.getContext().getColor(R.color.white));
        }

        holder.view.setOnClickListener(expandCacheDetail(position, holder));

    }

    private Spanned getHintText(Geocache geocache) {

        String hintString = "<strong>HINT</strong>: <i>" + geocache.getHint() + "</i>";
        return HtmlCompat.fromHtml(hintString, HtmlCompat.FROM_HTML_MODE_LEGACY);

    }

    private View.OnClickListener expandCacheDetail(int position, ViewHolder holder) {

        Geocache geocache = tour._tourCaches.get(position).getGeocache();

        return v -> {

            if(!holder.expanded){

                holder.expanded = true;

                // Remove hint indication from line 1
                if(geocache.hasHint()){
                    holder.cacheInfo1.setText(getCacheInfoLine1(geocache, false));
                    holder.hint.setVisibility(View.VISIBLE);
                }

                // Change arrow on the left
                holder.extraInfoArrow.setImageDrawable(App.getContext().getDrawable(R.drawable.double_arrow_up));

                // Show extra information
                holder.extraInfoLayout.setVisibility(View.VISIBLE);

                // Show DNF information if needed
                if(geocache.isDNFRisk())
                    holder.dnfInfo.setVisibility(View.GONE);

            } else {

                // Hide extra information
                if(geocache.hasHint()){
                    holder.cacheInfo1.setText(getCacheInfoLine1(geocache, true));
                    holder.hint.setVisibility(View.GONE);
                }
                holder.expanded = false;
                holder.extraInfoArrow.setImageDrawable(App.getContext().getDrawable(R.drawable.double_arrow_down));
                holder.extraInfoLayout.setVisibility(View.GONE);
                if(geocache.isDNFRisk())
                    holder.dnfInfo.setVisibility(View.VISIBLE);

            }

        };
    }

    private Spanned getCacheInfoLine0(Geocache geocache){
        // Set information line 0: code - D/T: _/_ - Size: _
        String info = geocache.getCode() + " - D/T: ";

        double difficulty = Double.parseDouble(geocache.getDifficulty());
        double terrain = Double.parseDouble(geocache.getTerrain());

        String red = String.valueOf(App.getContext().getColor(R.color.red));//"#CF2A27";

        if(difficulty >= 4){
            info += "<font color=\"" + red + "\">" + geocache.getDifficulty() + "</font>" + "/";
        } else {
            info += geocache.getDifficulty() + "/";
        }

        if(terrain >= 4){
            info += "<font color=\"" + red + "\">" + geocache.getTerrain() + "</font>";
        } else {
            info += geocache.getTerrain();
        }

        info += " - Size: " + geocache.getSize();

        return HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_LEGACY);

    }

    private Spanned getCacheInfoLine1(Geocache geocache, boolean withHint){

        String red = String.valueOf(App.getContext().getColor(R.color.red));//"#CF2A27";

       /* Date lastLogDate = geocache.getLastLogDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd/MMM/yy");
        String info = "Last log: " + simpleDateFormat.format(lastLogDate) + "- &#9825; " + geocache.getFavourites();
*/

       String info = "- &#9825; " + geocache.getFavourites();
        if(withHint){

            info += " - ";

            if(!geocache.hasHint()){
                info += "<font color=\"" + red+ "\">NO HINT</font>";
            } else {
                info += "HINT";
            }
        }

        return HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_LEGACY);
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

       /* tour.swapCachePositions(fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);

        GeocachingTour.write(App.getTourRoot(), tour);*/

        return true;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView cacheName;
        ImageView cacheSymbol;
        TextView cacheInfo0;
        TextView cacheInfo1;
        TextView dnfInfo;
        TextView dnfInfoExpanded;
        TextView hint;
        Button editButton;
        Button goToButton;
        ConstraintLayout layout;
        boolean expanded = false;
        ConstraintLayout extraInfoLayout;
        ImageView extraInfoArrow;
        LinearLayout lastLogsLayout;

        ViewHolder(View v){
            super(v);
            view = v;

            cacheName = v.findViewById(R.id.cache_title);
            cacheSymbol = v.findViewById(R.id.cache_symbol);
            cacheInfo0 = v.findViewById(R.id.cache_info_0);
            cacheInfo1 = v.findViewById(R.id.cache_info_1);
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
