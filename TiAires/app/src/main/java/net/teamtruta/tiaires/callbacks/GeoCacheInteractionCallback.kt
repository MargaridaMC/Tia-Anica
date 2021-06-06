package net.teamtruta.tiaires.callbacks

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.adapters.GeoCacheListAdapter
import net.teamtruta.tiaires.adapters.GeoCacheListAdapter.Companion.visitItem
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum

// Useful link: https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
class GeoCacheInteractionCallback(private val context : Context,
                                  private val geoCacheListAdapter: GeoCacheListAdapter) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private var dragStarted = false
    override fun isLongPressDragEnabled(): Boolean {
        //return true
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView,
                                  viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    } //206-555-0123

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        geoCacheListAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //touchAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        val position = viewHolder.adapterPosition
        Log.d("TAG", "Swiped in direction: $direction")

        // Direction: 32 means swiped right, 16 means swiped left
        if (direction == 32) {
            visitItem(position, VisitOutcomeEnum.Found)
        } else if (direction == 16) {
            visitItem(position, VisitOutcomeEnum.DNF)
        }
        geoCacheListAdapter.notifyItemChanged(viewHolder.adapterPosition)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                             actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20
        val iconMargin: Int
        val iconTop: Int
        val iconBottom: Int
        val iconLeft: Int
        val iconRight: Int

        when {
            dX > 0 -> { // Swiping to the right
                val icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.geo_cache_icon_found)
                val background = ColorDrawable(context.getColor(R.color.light_grey))

                iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                iconTop = itemView.top + iconMargin
                iconBottom = iconTop + icon.intrinsicHeight
                iconLeft = itemView.left + iconMargin
                iconRight = iconLeft + icon.intrinsicWidth
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setBounds(itemView.left, itemView.top,
                        itemView.left + dX.toInt() + backgroundCornerOffset,
                        itemView.bottom)
                background.draw(c)
                icon.draw(c)
            }
            dX < 0 -> { // Swiping to the left
                val icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.geo_cache_icon_dnf)
                val background = ColorDrawable(context.getColor(R.color.light_grey))

                iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                iconTop = itemView.top + iconMargin
                iconBottom = iconTop + icon.intrinsicHeight
                iconRight = itemView.right - iconMargin
                iconLeft = iconRight - icon.intrinsicWidth
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setBounds(itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom)

                background.draw(c)
                icon.draw(c)
            }
            else -> { // view is unSwiped
                val background = ColorDrawable(context.getColor(R.color.light_grey))
                background.setBounds(0, 0, 0, 0)
                background.draw(c)
            }
        }

    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        // Called when the user's movement is over.
        // Save the tour
        super.clearView(recyclerView, viewHolder)
        if (dragStarted) {
            viewHolder.itemView.alpha = 1.0f
            dragStarted = false
            geoCacheListAdapter.onMoveEnded()
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // Make the selected row somewhat transparent so the user knows they can move it
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder!!.itemView.alpha = 0.5f
            dragStarted = true
        }
    }

}