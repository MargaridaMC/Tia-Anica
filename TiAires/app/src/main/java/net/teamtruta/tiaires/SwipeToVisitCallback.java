package net.teamtruta.tiaires;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToVisitCallback extends ItemTouchHelper.SimpleCallback {

    private CacheListAdapter cacheListAdapter;

    public SwipeToVisitCallback(CacheListAdapter adapter){
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.cacheListAdapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }
}
