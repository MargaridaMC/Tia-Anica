package net.teamtruta.tiaires;


import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


class CacheListAdapter extends RecyclerView.Adapter<CacheListAdapter.ViewHolder> {

    private GeocachingTour tour;

    public CacheListAdapter(GeocachingTour tour){
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
            case "mystery":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_mystery);
            case "multi":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_multi);
            case "wherigo":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_wherigo);
            case "cito":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_cito);
            case "earth":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_earth);
            case "event":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_event);
            case "letterbox":
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_letterbox);
            default:
                cacheSymbolDrawable = ContextCompat.getDrawable(holder.view.getContext(), R.drawable.cache_icon_type_traditional);
        }

        holder.cacheSymbol.setImageDrawable(cacheSymbolDrawable);



    }

    public CacheListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_cache_layout, parent, false);

        return new ViewHolder(v);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView cacheName;
        ImageView cacheSymbol;
        TextView cacheInfo0;
        TextView cacheInfo1;
        TextView cacheInfo2;

        public ViewHolder(View v){
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
