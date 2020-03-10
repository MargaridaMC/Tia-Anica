package net.teamtruta.tiaires;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;


class CacheListAdapter extends RecyclerView.Adapter<CacheListAdapter.ViewHolder> implements ItemTouchHelperAdapter{

    private static GeocachingTour tour;
    private EditOnClickListener editOnClickListener;
    private GoToOnClickListener goToOnClickListener;
    private static GeocacheInTour recentlyVisitedCache;
    private static int recentlyVisitedCachePosition;


    CacheListAdapter(GeocachingTour tour, EditOnClickListener editOnClickListener, GoToOnClickListener goToOnClickListener){
        this.tour = tour;
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

        // Set information line 0: code - D/T: _/_ - Size: _
        String info = cache.geocache.code + " - D/T: ";

        double difficulty = Double.parseDouble(cache.geocache.difficulty);
        double terrain = Double.parseDouble(cache.geocache.terrain);
        String red = "#CF2A27";

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

        holder.cacheInfo0.setText(HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_LEGACY));
        //holder.cacheInfo0.setText(Html.fromHtml(info));

        // Set information line 1: Found: date - \heart nFavs - Hint/No hint
        // TODO: replace Found info with date of last find -- obtain from recent logs
        info = "Found: " + "ND" + "- &#9825; " + cache.geocache.favourites + " - ";
        if(cache.geocache.hint.equals("NO MATCH")){
            info += "<font color=\"" + red + "\">NO HINT</font>";
        } else {
            info += "HINT";
        }
        holder.cacheInfo1.setText(HtmlCompat.fromHtml(info, HtmlCompat.FROM_HTML_MODE_LEGACY));
        //holder.cacheInfo1.setText(Html.fromHtml(info));

        // Set information line 2: DNFs in last 10 logs: _
        // TODO: include number of DNFs from recentlogs
        info = "DNFs in last 10 logs: ";
        holder.cacheInfo2.setText(info);

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
            holder.layout.setBackgroundColor(Color.parseColor("#DCDCDC"));
        }

    }

    public CacheListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_cache_layout, parent, false);

        return new ViewHolder(v);
    }

    public static void visitItem(int position, FoundEnumType visit){

        // Save the deleted item in case user wants to undo the action;
        recentlyVisitedCache = tour.getCacheInTour(position);
        recentlyVisitedCachePosition = position;

        tour.getCacheInTour(position).setVisit(visit);

        String rootPath = App.getTourRoot();
        tour.toFile(rootPath);

        // update the element we just changed
        TourList.update(rootPath, tour);

        // Remove cache from tour
        // tour.removeFromTour(recentlyVisitedCache.geocache.code);
        // notifyItemRemoved(position);
        // showUndoSnackbar();
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
        return true;
    }
/*
    private void showUndoSnackbar() {
        View view = TourActivity.findViewById(R.id.coordinator_layout);
        Snackbar snackbar = Snackbar.make(view, R.string.snack_bar_text, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.snack_bar_undo, v -> undoDelete());
        snackbar.show();
    }

    private void undoDelete() {
        tour.addToTour(recentlyVisitedCache.geocache);
        notifyItemInserted(recentlyVisitedCachePosition);
    }
*/

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

        }

    }

    interface EditOnClickListener {
        void onClick(int position);
    }

    interface GoToOnClickListener{
        void onGoToClick(String code);
    }
}
