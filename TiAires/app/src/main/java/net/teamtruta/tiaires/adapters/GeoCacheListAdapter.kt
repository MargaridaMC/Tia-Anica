package net.teamtruta.tiaires.adapters

import android.content.Context
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.extensions.GeoCacheAttributeIcon.Companion.getGeoCacheAttributeIcon
import net.teamtruta.tiaires.extensions.GeoCacheIcon.Companion.getIconDrawable
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCache
import net.teamtruta.tiaires.data.models.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.models.GeoCacheWithLogsAndAttributesAndWaypoints
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum
import net.teamtruta.tiaires.viewModels.TourViewModel
import net.teamtruta.tiaires.viewModels.TourViewModelFactory
import java.time.Instant
import java.util.*

class GeoCacheListAdapter(private val editOnClickListener: EditOnClickListener?,
                                   private val goToOnClickListener: GoToOnClickListener?,
                                   private val context: Context,
                            val viewModel: TourViewModel) :
        RecyclerView.Adapter<GeoCacheListAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    fun setGeoCacheInTourList(list: List<GeoCacheInTourWithDetails?>) {
        geoCacheInTourList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return geoCacheInTourList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val geoCacheInTour = geoCacheInTourList[position]!!.geoCacheInTour
        val geoCacheWithLogsAndAttributes = geoCacheInTourList[position]!!.geoCache
        val geoCache = geoCacheWithLogsAndAttributes.geoCache

        // 1. Set geoCache name
        holder.geoCacheName.text = geoCache.name

        // 2. Set geoCache type symbol
        val drawableID: Int = if (geoCacheInTour.currentVisitOutcome === VisitOutcomeEnum.Found) {
            R.drawable.geo_cache_icon_found
        } else if (geoCacheInTour.currentVisitOutcome === VisitOutcomeEnum.DNF) {
            R.drawable.geo_cache_icon_dnf
        } else if (geoCacheInTour.currentVisitOutcome === VisitOutcomeEnum.Disabled) {
            R.drawable.geo_cache_icon_disabled
        } else {
            getIconDrawable(geoCache.type)
        }
        val geoCacheSymbolDrawable = ContextCompat.getDrawable(holder.view.context, drawableID)
        holder.geoCacheSymbol.setImageDrawable(geoCacheSymbolDrawable)

        // 3. Set information line 1: Code, difficulty and terrain and Size
        holder.geoCacheCode.text = geoCache.code
        val difTerString = context.getString(R.string.geo_cache_dif_ter)
        val difficulty = geoCache.difficulty
        val terrain = geoCache.terrain
        var diffString = String.format(Locale.getDefault(), "%.1f", difficulty)
        var terString = String.format(Locale.getDefault(), "%.1f", terrain)
        diffString = if (difficulty > 4) "<font color='red'>$diffString</font>/" else diffString
        terString = if (terrain > 4) "<font color='red'>$terString</font>/" else terString
        holder.geoCacheDifTer.text = HtmlCompat.fromHtml(String.format(difTerString, diffString, terString),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        val sizeString = context.getString(R.string.geo_cache_size)
        holder.geoCacheSize.text = String.format(sizeString, geoCache.size)


        // 4. Set information line 2: Favorites and whether geoCache has hint
        val favString = context.getString(R.string.geo_cache_favs)
        holder.geoCacheFavs.text = String.format(favString, geoCache.favourites)
        if (geoCache.hasHint()) {
            val hintString = context.getString(R.string.geo_cache_has_hint)
            holder.geoCacheHasHint.text = HtmlCompat.fromHtml(
                    hintString,
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            holder.geoCacheHasHint.text = HtmlCompat.fromHtml(context.getString(R.string.geo_cache_has_no_hint),
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
        val last10Logs = geoCacheWithLogsAndAttributes.getLastNLogs(10)
        var i = 0
        for ((_, logType) in last10Logs) {
            val tv = holder.lastLogsLayout.getChildAt(i) as TextView
            when {
                logType === VisitOutcomeEnum.Found -> {
                    tv.setTextColor(holder.view.context.getColor(R.color.colorPrimary))
                }
                logType === VisitOutcomeEnum.DNF -> {
                    tv.setTextColor(holder.view.context.getColor(R.color.red))
                }
                else -> {
                    tv.setTextColor(holder.view.context.getColor(R.color.blue))
                }
            }
            i++
        }


        // 5. GeoCache Hint
        holder.hint.text = getHintText(geoCache)


        // 6. Set DNF information if required
        if (geoCacheWithLogsAndAttributes.isDNFRisk()) {
            var dnfString = context.getString(R.string.dnf_risk)
            dnfString = String.format(dnfString, geoCacheWithLogsAndAttributes.dNFRisk)
            holder.dnfInfo.text = HtmlCompat.fromHtml(
                    dnfString,
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            holder.dnfInfo.visibility = View.VISIBLE
        } else {
            holder.dnfInfo.text = ""
            holder.dnfInfo.visibility = View.GONE
        }

        // 7. Set listener for edit button
        holder.editButton.setOnClickListener {editOnClickListener?.onEditClick(geoCacheInTour.id)}

        // 8. Set listener for go-to-geoCache button
        holder.goToButton.setOnClickListener {goToOnClickListener?.onGoToClick(geoCache) }

        // 9. Make section grey if geoCache has been visited
        if (geoCacheInTour.currentVisitOutcome === VisitOutcomeEnum.Found || geoCacheInTour.currentVisitOutcome === VisitOutcomeEnum.DNF || geoCacheInTour.currentVisitOutcome === VisitOutcomeEnum.Disabled) {
            holder.layout.setBackgroundColor(context.getColor(R.color.light_grey))
        } else {
            holder.layout.setBackgroundColor(context.getColor(R.color.white))
        }

        // 10. Setup expansion
        holder.extraInfoArrow.setOnClickListener {holderExpansionOnClicklistener(holder, geoCacheWithLogsAndAttributes) }

        // 11. Add Cache Attributes
        holder.attributeList.removeAllViews()
        if (geoCacheWithLogsAndAttributes.attributes.isNotEmpty()) {
            for ((_, attributeType) in geoCacheWithLogsAndAttributes.attributes) {
                val iv = ImageView(context)
                val attributeIconID = getGeoCacheAttributeIcon(attributeType)
                iv.setImageDrawable(ContextCompat.getDrawable(context, attributeIconID))
                // DEPRECATED:
                //iv.setImageDrawable(App.getContext().getDrawable(attributeIconID));
                val layoutParams = LinearLayout.LayoutParams(
                        context.resources.getDimension(R.dimen.medium_icon_size).toInt(),
                        context.resources.getDimension(R.dimen.medium_icon_size).toInt())
                layoutParams.marginEnd = context.resources.getDimension(R.dimen.tiny_padding).toInt()
                iv.layoutParams = layoutParams
                holder.attributeList.addView(iv)
            }
        }
    }

    private fun getHintText(geoCache: GeoCache): Spanned {
        val hintString = "<strong>HINT</strong>: <i>" + geoCache.hint + "</i>"
        return HtmlCompat.fromHtml(hintString, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun holderExpansionOnClicklistener(holder: ViewHolder, geoCacheWithLogsAndAttributesAndWaypoints: GeoCacheWithLogsAndAttributesAndWaypoints) {
        if (holder.extraInfoArrow.isChecked || holder.extraInfoLayout.visibility != View.VISIBLE) {
            expandHolder(holder, geoCacheWithLogsAndAttributesAndWaypoints)
        } else {
            unexpandHolder(holder)
        }
    }

    private fun expandHolder(holder: ViewHolder, geoCacheWithLogsAndAttributesAndWaypoints: GeoCacheWithLogsAndAttributesAndWaypoints) {

        // Remove hint indication from line 1
        if (geoCacheWithLogsAndAttributesAndWaypoints.geoCache.hasHint()) {
            holder.geoCacheHasHint.visibility = View.GONE
            holder.hint.visibility = View.VISIBLE
        }

        // Show extra information
        holder.extraInfoLayout.visibility = View.VISIBLE
        holder.extraInfoArrow.isChecked = true

        // Show attributes
        if (geoCacheWithLogsAndAttributesAndWaypoints.attributes.isNotEmpty())
            holder.attributeList.visibility = View.VISIBLE
    }

    private fun unexpandHolder(holder: ViewHolder) {
        // Hide extra information
        holder.geoCacheHasHint.visibility = View.VISIBLE
        holder.hint.visibility = View.GONE
        holder.extraInfoLayout.visibility = View.GONE
        holder.extraInfoArrow.isChecked = false

        // Hide attributes
        holder.attributeList.visibility = View.GONE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.element_geo_cache_layout, parent, false)
        return ViewHolder(v)
    }

    //@Override
    //public void onItemDismiss(int position) {
    //tour._tourCaches.remove(position);
    //notifyItemRemoved(position);
    //}
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(geoCacheInTourList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onMoveEnded() {
        viewModel.reorderTourCaches(geoCacheInTourList)
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var geoCacheName: TextView = view.findViewById(R.id.geo_cache_title)
        var geoCacheSymbol: ImageView = view.findViewById(R.id.geo_cache_symbol)
        var geoCacheCode: TextView = view.findViewById(R.id.geo_cache_code)
        var geoCacheDifTer: TextView = view.findViewById(R.id.geo_cache_dif_ter)
        var geoCacheSize: TextView = view.findViewById(R.id.geo_cache_size)
        var geoCacheFavs: TextView = view.findViewById(R.id.geo_cache_favs)
        var geoCacheHasHint: TextView = view.findViewById(R.id.geo_cache_has_hint)
        var dnfInfo: TextView = view.findViewById(R.id.dnf_risk)
        var dnfInfoExpanded: TextView = view.findViewById(R.id.dnf_info_expanded)
        var hint: TextView = view.findViewById(R.id.hint)
        var editButton: Button = view.findViewById(R.id.edit_button)
        var goToButton: Button = view.findViewById(R.id.go_to_button)
        var layout: ConstraintLayout = view.findViewById(R.id.element_geo_cache_layout)
        var extraInfoLayout: ConstraintLayout = view.findViewById(R.id.expandable_info)
        var extraInfoArrow: CheckBox = view.findViewById(R.id.extra_info_arrow)
        var lastLogsLayout: GridLayout = view.findViewById(R.id.last10LogsSquares)
        var attributeList: LinearLayout = view.findViewById(R.id.geo_cache_attributes)

    }

    interface EditOnClickListener {
        fun onEditClick(geoCacheID: Long)
    }

    interface GoToOnClickListener {
        fun onGoToClick(geoCache: GeoCache?)
    }

    interface OnVisitListener {
        fun onVisit(visit: String?)
    }

    companion object {
        private var geoCacheInTourList: List<GeoCacheInTourWithDetails?> = listOf()


        //var onVisitListener: OnVisitListener? = null

        fun visitItem(position: Int, visit: VisitOutcomeEnum) {

            // Save the deleted item in case user wants to undo the action;

            // Useful if we want to add an undo functionality here
            // recentlyVisitedCache = tour.getCacheInTour(position);
            // recentlyVisitedCachePosition = position;
            val selectedGeoCache = geoCacheInTourList[position]!!.geoCacheInTour
            selectedGeoCache.currentVisitOutcome = visit
            selectedGeoCache.currentVisitDatetime = Instant.now()

            val viewModel: TourViewModel = TourViewModelFactory(App().repository)
                    .create(TourViewModel::class.java)

            viewModel.updateGeoCacheInTour(selectedGeoCache)
        }
    }

}