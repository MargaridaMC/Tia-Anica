package net.teamtruta.tiaires;


import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.UnsupportedEncodingException;


class CacheListAdapter extends RecyclerView.Adapter<CacheListAdapter.ViewHolder> {

    private GeocachingTour tour;

    CacheListAdapter(GeocachingTour tour){
        this.tour = tour;
    }
    @Override
    public int getItemCount(){
        return tour.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){

        GeocacheInTour cache = tour.getCacheInTour(position);

        // Set cache name
        holder.cacheName.setText(cache.geocache.name);

        // Set cache type symbol
        String cacheType = cache.geocache.type;
        cacheType = cacheType.toLowerCase();
        Drawable cacheSymbolDrawable;

        switch (cacheType){
            case "traditional":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_traditional);
                break;
            case "mystery":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_mystery);
                break;
            case "multi":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_multi);
                break;
            case "wherigo":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_wherigo);
                break;
            case "cito":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_cito);
                break;
            case "earth":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_earth);
                break;
            case "event":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_event);
                break;
            case "letterbox":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_letterbox);
                break;
            default:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_traditional);
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

        holder.cacheInfo0.setText(Html.fromHtml(info));

        // Set information line 1: Found: date - \heart nFavs - Hint/No hint
        // TODO: replace Found info with date of last find -- obtain from recent logs
        info = "Found: " + "ND" + "- \u2764 " + cache.geocache.favourites + " - ";
        if(cache.geocache.hint.equals("NO MATCH")){
            info += "<font color=\"" + red + "\">NO HINT</font>";
        } else {
            info += "HINT";
        }
        holder.cacheInfo1.setText(Html.fromHtml(info));

        // Set information line 2: DNFs in last 10 logs: _
        // TODO: include number of DNFs from recentlogs
        info = "DNFs in last 10 logs: ";
        holder.cacheInfo2.setText(info);

    }

    public CacheListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_cache_layout, parent, false);

        return new ViewHolder(v);
    }


    static class ViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView cacheName;
        ImageView cacheSymbol;
        TextView cacheInfo0;
        TextView cacheInfo1;
        TextView cacheInfo2;

        ViewHolder(View v){
            super(v);
            view = v;

            cacheName = v.findViewById(R.id.cache_title);
            cacheSymbol = v.findViewById(R.id.cache_symbol);
            cacheInfo0 = v.findViewById(R.id.cache_info_0);
            cacheInfo1 = v.findViewById(R.id.cache_info_1);
            cacheInfo2 = v.findViewById(R.id.cache_info_2);

        }


    }
}
