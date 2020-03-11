package net.teamtruta.tiaires;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

// Useful link: https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf

public class CacheInteractionCallback extends ItemTouchHelper.SimpleCallback {

    private CacheListAdapter cacheListAdapter;
    private Drawable foundIcon;
    private Drawable dnfIcon;
    private final ColorDrawable background;

    CacheInteractionCallback(CacheListAdapter adapter){
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.cacheListAdapter = adapter;

        foundIcon = App.getContext().getDrawable(R.drawable.cache_icon_found);
        dnfIcon = App.getContext().getDrawable(R.drawable.cache_icon_dnf);

        background = new ColorDrawable(App.getContext().getColor(R.color.light_grey));

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        cacheListAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //touchAdapter.onItemDismiss(viewHolder.getAdapterPosition());

        int position = viewHolder.getAdapterPosition();
        Log.d("TAG", "Swiped in direction: " + direction);

        // Direction: 32 means swiped right, 16 means swiped left
        if(direction == 32){
            CacheListAdapter.visitItem(position, FoundEnumType.Found);
        } else if(direction == 16){
            CacheListAdapter.visitItem(position, FoundEnumType.DNF);
        }

        cacheListAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        int iconMargin;
        int iconTop;
        int iconBottom;

        int iconLeft;
        int iconRight;

        Drawable icon = foundIcon;

        if (dX > 0) { // Swiping to the right

            iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            iconTop = itemView.getTop() + iconMargin;
            iconBottom = iconTop + icon.getIntrinsicHeight();

            iconLeft = itemView.getLeft() + iconMargin;
            iconRight = iconLeft +  icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom());


        } else if (dX < 0) { // Swiping to the left

            icon = dnfIcon;

            iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            iconTop = itemView.getTop() + iconMargin;
            iconBottom = iconTop + icon.getIntrinsicHeight();

            iconRight = itemView.getRight() - iconMargin;
            iconLeft = iconRight - icon.getIntrinsicWidth();

            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());

        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }

}
