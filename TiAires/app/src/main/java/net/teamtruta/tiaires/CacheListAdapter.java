package net.teamtruta.tiaires;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;


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
        return tour.getSize();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){

        final GeocacheInTour cache = tour.getCacheInTour(position);

        // Set cache name
        holder.cacheName.setText(cache.geocache.name);

        // Set cache type symbol
        CacheTypeEnum cacheType = cache.geocache.type;
        Drawable cacheSymbolDrawable;

        switch (cacheType){
            case Traditional:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_traditional);
                break;
            case Mystery:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_mystery);
                break;
            case Multi:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_multi);
                break;
            case Earth:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_earth);
                break;
            case Letterbox:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_letterbox);
                break;
            case Event:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_event);
                break;
            case CITO:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_cito);
                break;
            case Mega:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_mega);
                break;
            case Giga:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_giga);
                break;
            case Wherigo:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_wherigo);
                break;
            case HQ:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_hq);
                break;
            case GPSAdventures:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_gps_adventures);
                break;
            case HQCelebration:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_hq_celebration);
                break;
            case HQBlockParty:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_hq_blockparty);
                break;
            case CommunityCelebration:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_community_event);
                break;
            case Virtual:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_virtual);
                break;
            case Webcam:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_webcam);
                break;
            case ProjectAPE:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_project_ape);
                break;
            case Locationless:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_locationless);
                break;
            default:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.shrug);
                break;
        }

        holder.cacheSymbol.setImageDrawable(cacheSymbolDrawable);

        holder.cacheInfo0.setText(getCacheInfoLine0(cache));

        // Set information line 1: Found: date - \heart nFavs - Hint/No hint
        holder.cacheInfo1.setText(getCacheInfoLine1(cache, true));

        // Set information line 2: DNFs in last 10 logs: _
        holder.cacheInfo2.setText(getCacheInfoLine2(cache));

        // Set height of hint area to 0 -- should only be visible when clicked on
        //holder.hint.setHeight(0);

        // Set listener for edit button
        holder.editButton.setOnClickListener(v -> {
            if(editOnClickListener!=null){
                editOnClickListener.onClick(position);
            }
        });

        // TODO: set listener for go to button
        holder.goToButton.setOnClickListener(v -> {
            if(goToOnClickListener!=null){
                goToOnClickListener.onGoToClick(cache.geocache.code);
            }
        });

        // Make section grey if cache has been visited
        if(cache.getVisit() == FoundEnumType.Found || cache.getVisit() == FoundEnumType.DNF){
            holder.layout.setBackgroundColor(App.getContext().getColor(R.color.light_grey));
        }

        holder.view.setOnClickListener(expandCacheDetail(position, holder));

    }

    private View.OnClickListener expandCacheDetail(int position, ViewHolder holder) {

        GeocacheInTour cache = tour.getCacheInTour(position);

        return v -> {

            if(!cache.geocache.hasHint()) {

                Toast t = Toast.makeText(App.getContext(), "This cache doesn't have a hint available.", Toast.LENGTH_SHORT);
                t.show();

            } else if(!holder.hintVisible){

                String hintString = "HINT: <i>" + cache.geocache.hint + "</i>";
                holder.hint.setText(HtmlCompat.fromHtml(hintString, HtmlCompat.FROM_HTML_MODE_LEGACY));
                float textSize = App.getContext().getResources().getDimension(R.dimen.small_text_size);
                holder.hint.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                holder.hintVisible = true;
                holder.cacheInfo1.setText(getCacheInfoLine1(cache, false));

            } else {

                holder.hint.setText("");
                holder.hint.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0);
                holder.cacheInfo1.setText(getCacheInfoLine1(cache, true));
                holder.hintVisible = false;

            }

        };
    }

    private Spanned getCacheInfoLine0(GeocacheInTour cache){
        // Set information line 0: code - D/T: _/_ - Size: _
        String info = cache.geocache.code + " - D/T: ";

        double difficulty = Double.parseDouble(cache.geocache.difficulty);
        double terrain = Double.parseDouble(cache.geocache.terrain);

        String red = String.valueOf(App.getContext().getColor(R.color.red));//"#CF2A27";

        if(difficulty >= 4){
            info += "<font color=\"" + red + "\">" + cache.geocache.difficulty + "</font>" + "/";
        } else {
            info += cache.geocache.difficulty + "/";
        }

        if(terrain >= 4){
            info += "<font color=\"" + red + "\">" + cache.geocache.terrain + "</font>";
        } else {
            info += cache.geocache.terrain;
        }

        info += " - Size: " + cache.geocache.size;

        return HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_LEGACY);

    }

    private Spanned getCacheInfoLine1(GeocacheInTour cache, boolean withHint){

        String red = String.valueOf(App.getContext().getColor(R.color.red));//"#CF2A27";

        // TODO: replace Found info with date of last find -- obtain from recent logs
        String info = "Found: " + "ND" + "- &#9825; " + cache.geocache.favourites;

        if(withHint){

            info += " - ";

            if(!cache.geocache.hasHint()){
                info += "<font color=\"" + red+ "\">NO HINT</font>";
            } else {
                info += "HINT";
            }
        }

        return HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }


    private String getCacheInfoLine2(GeocacheInTour cache){
        // TODO: include number of DNFs from recentlogs
        return "DNFs in last 10 logs: ";
    }

    public CacheListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_cache_layout, parent, false);
        return new ViewHolder(v);
    }

    static void visitItem(int position, FoundEnumType visit){

        // Save the deleted item in case user wants to undo the action;

        // Useful if we want to add an undo functionality here
        // recentlyVisitedCache = tour.getCacheInTour(position);
        // recentlyVisitedCachePosition = position;

        tour.getCacheInTour(position).setVisit(visit);

        String rootPath = App.getTourRoot();
        tour.toFile(rootPath);

        // update the element we just changed
        TourList.update(rootPath, tour);

        onVisitListener.onVisit(visit.toString());
    }

    @Override
    public void onItemDismiss(int position) {
        //tour._tourCaches.remove(position);
        //notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(tour._tourCaches, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(tour._tourCaches, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        tour.toFile(App.getTourRoot());

        return true;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView cacheName;
        ImageView cacheSymbol;
        TextView cacheInfo0;
        TextView cacheInfo1;
        TextView cacheInfo2;
        Button editButton;
        Button goToButton;
        ConstraintLayout layout;
        TextView hint;
        boolean hintVisible = false;

        ViewHolder(View v){
            super(v);
            view = v;

            cacheName = v.findViewById(R.id.cache_title);
            cacheSymbol = v.findViewById(R.id.cache_symbol);
            cacheInfo0 = v.findViewById(R.id.cache_info_0);
            cacheInfo1 = v.findViewById(R.id.cache_info_1);
            cacheInfo2 = v.findViewById(R.id.cache_info_2);
            editButton = v.findViewById(R.id.edit_button);
            goToButton = v.findViewById(R.id.go_to_button);
            layout = v.findViewById(R.id.element_cache_layout);
            hint = v.findViewById(R.id.hint);

        }

    }

    interface EditOnClickListener {
        void onClick(int position);
    }

    interface GoToOnClickListener{
        void onGoToClick(String code);
    }

    interface OnVisitListener{
        void onVisit(String visit);
    }
}
