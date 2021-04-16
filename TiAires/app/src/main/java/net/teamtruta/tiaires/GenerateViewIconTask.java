package net.teamtruta.tiaires;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;

import net.teamtruta.tiaires.views.MapActivity;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * AsyncTask to generate Bitmap from Views to be used as iconImage in a SymbolLayer.
 * <p>
 * Call be optionally be called to update the underlying data source after execution.
 * </p>
 * <p>
 * Generating Views on background thread since we are not going to be adding them to the view hierarchy.
 * </p>
 */
public class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_DIFFICULTY = "difficulty";
    private static final String PROPERTY_TERRAIN = "terrain";
    private static final String PROPERTY_FAVOURITES = "favourites";

    private final WeakReference<MapActivity> activityRef;
    private final boolean refreshSource;
    private final HashMap<String, View> viewMap;

    public GenerateViewIconTask(MapActivity activity, boolean refreshSource,  HashMap<String, View> viewMap) {
        this.activityRef = new WeakReference<>(activity);
        this.refreshSource = refreshSource;
        this.viewMap = viewMap;
    }


    @SuppressWarnings("WrongThread")
    @Override
    protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
        MapActivity activity = activityRef.get();
        if (activity != null) {
            HashMap<String, Bitmap> imagesMap = new HashMap<>();
            LayoutInflater inflater = LayoutInflater.from(activity);

            FeatureCollection featureCollection = params[0];

            for (Feature feature : featureCollection.features()) {

                BubbleLayout bubbleLayout = (BubbleLayout)
                        inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);

                String name = feature.getStringProperty(PROPERTY_NAME);
                TextView titleTextView = bubbleLayout.findViewById(R.id.geo_cache_title);
                titleTextView.setText(name);

                TextView descriptionTextView = bubbleLayout.findViewById(R.id.geo_cache_description);
                descriptionTextView.setText(String.format(activity.getString(R.string.geo_cache_description_box),
                        feature.getStringProperty(PROPERTY_DIFFICULTY),
                        feature.getStringProperty(PROPERTY_TERRAIN),
                        feature.getStringProperty(PROPERTY_FAVOURITES)));

                int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                bubbleLayout.measure(measureSpec, measureSpec);

                float measuredWidth = bubbleLayout.getMeasuredWidth();

                bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

                Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                imagesMap.put(name, bitmap);
                viewMap.put(name, bubbleLayout);
            }

            return imagesMap;
        } else {
            return null;
        }
    }



    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
        super.onPostExecute(bitmapHashMap);
        MapActivity activity = activityRef.get();
        if (activity != null && bitmapHashMap != null) {
            activity.setImageGenResults(bitmapHashMap);
            if (refreshSource) {
                activity.refreshSource();
            }
        }
    }
}

/**
 * Utility class to generate Bitmaps for Symbol.
 */
class SymbolGenerator {

    /**
     * Generate a Bitmap from an Android SDK View.
     *
     * @param view the View to be drawn to a Bitmap
     * @return the generated bitmap
     */
    static Bitmap generate(@NonNull View view) {
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(measureSpec, measureSpec);

        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();

        view.layout(0, 0, measuredWidth, measuredHeight);
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}