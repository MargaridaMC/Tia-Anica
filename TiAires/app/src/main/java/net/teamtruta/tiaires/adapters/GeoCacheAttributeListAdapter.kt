package net.teamtruta.tiaires.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import net.teamtruta.tiaires.extensions.GeoCacheAttributeIcon.Companion.getGeoCacheAttributeIcon
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCacheAttributeEnum

class GeoCacheAttributeListAdapter(private val context: Context,
                                   private val attributes: List<GeoCacheAttributeEnum>) : BaseAdapter() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return attributes.size
    }

    override fun getItem(position: Int): Any {
        return attributes[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View? {
        var convertView = view
        val holder: ViewHolder
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.element_attribute, parent, false)
            holder = ViewHolder()
            holder.attributeIcon = convertView.findViewById(R.id.attribute_icon)
            holder.attributeText = convertView.findViewById(R.id.attribute_text)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val attributeIconID = getGeoCacheAttributeIcon(attributes[position])
        holder.attributeIcon!!.setImageDrawable(
                ContextCompat.getDrawable(context, attributeIconID))
        // DEPRECATED:
        //context.getDrawable(attributeIconID));
        holder.attributeText!!.text = attributes[position].attributeString
        return convertView
    }

    internal class ViewHolder {
        var attributeIcon: ImageView? = null
        var attributeText: TextView? = null
    }

}