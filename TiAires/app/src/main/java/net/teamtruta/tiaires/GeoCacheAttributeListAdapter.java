package net.teamtruta.tiaires;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class GeoCacheAttributeListAdapter extends BaseAdapter {

    private final ArrayList<GeoCacheAttributeEnum> attributes;
    private final LayoutInflater layoutInflater;
    private final Context context;

    public GeoCacheAttributeListAdapter(Context context, ArrayList<GeoCacheAttributeEnum> attributes) {
        this.attributes = attributes;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }


    @Override
    public int getCount() {
        return attributes.size();
    }

    @Override
    public Object getItem(int position) {
        return attributes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.attribute_element_layout, null);
            holder = new ViewHolder();
            holder.attributeIcon = convertView.findViewById(R.id.attribute_icon);
            holder.attributeText = convertView.findViewById(R.id.attribute_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.attributeIcon.setImageDrawable(
                context.getDrawable(GeoCacheAttributeIcon.Companion.getGeoCacheAttributeIcon(
                        attributes.get(position))));
        holder.attributeText.setText(attributes.get(position).getAttributeString());

        return convertView;
    }

    static class ViewHolder {
        ImageView attributeIcon;
        TextView attributeText;
    }
}