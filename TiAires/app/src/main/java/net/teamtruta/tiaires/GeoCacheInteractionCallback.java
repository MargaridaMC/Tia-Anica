package net.teamtruta.tiaires;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG;

// Useful link: https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf

public class GeoCacheInteractionCallback extends ItemTouchHelper.SimpleCallback {

    private final GeoCacheListAdapter geoCacheListAdapter;
    private final Drawable foundIcon;
    private final Drawable dnfIcon;
    private final ColorDrawable background;
    private boolean dragStarted;

    GeoCacheInteractionCallback(GeoCacheListAdapter adapter){
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.geoCacheListAdapter = adapter;

        foundIcon = ContextCompat.getDrawable(App.getContext(), R.drawable.geo_cache_icon_found);
        dnfIcon = ContextCompat.getDrawable(App.getContext(), R.drawable.geo_cache_icon_dnf);
        // DEPRECATED:
        //foundIcon = App.getContext().getDrawable(R.drawable.geo_cache_icon_found);
        //dnfIcon = App.getContext().getDrawable(R.drawable.geo_cache_icon_dnf);

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
    public int getMovementFlags(@NotNull RecyclerView recyclerView,
                                @NotNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }//206-555-0123

    @Override
    public boolean onMove(@NotNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        geoCacheListAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //touchAdapter.onItemDismiss(viewHolder.getAdapterPosition());

        int position = viewHolder.getAdapterPosition();
        Log.d("TAG", "Swiped in direction: " + direction);

        // Direction: 32 means swiped right, 16 means swiped left
        if(direction == 32){
            GeoCacheListAdapter.visitItem(position, VisitOutcomeEnum.Found);
        } else if(direction == 16){
            GeoCacheListAdapter.visitItem(position, VisitOutcomeEnum.DNF);
        }

        geoCacheListAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView,
                            @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive){
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

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Called when the user's movement is over.
        // Save the tour
        super.clearView(recyclerView, viewHolder);
        if (dragStarted){
            viewHolder.itemView.setAlpha(1.0f);
            dragStarted = false;
            geoCacheListAdapter.onMoveEnded();
        }

    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        // Make the selected row somewhat transparent so the user knows they can move it
        super.onSelectedChanged(viewHolder, actionState);

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG){
            viewHolder.itemView.setAlpha(0.5f);
            dragStarted = true;
        }
    }
}
