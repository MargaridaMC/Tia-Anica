package net.teamtruta.tiaires;

import android.os.AsyncTask;

import com.microsoft.appcenter.analytics.Analytics;

import net.teamtruta.tiaires.data.GeoCache;
import net.teamtruta.tiaires.data.GeoCacheWithLogsAndAttributes;
import net.teamtruta.tiaires.data.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeocachingScrappingTask extends AsyncTask<Void, Void, Integer> {

    private final GeocachingScrapper scrapper;
    private final List<String> geoCacheCodesList;
    private final List<GeoCacheWithLogsAndAttributes> geoCaches = new ArrayList<>();
    Repository delegate;
    Long tourID;
    Map<String, Integer> geoCacheOrder;

    public GeocachingScrappingTask(GeocachingScrapper scrapper, List<String> geoCacheCodesList,
                                   Repository delegate, Long tourID, Map<String, Integer> geoCacheOrder){
        this.scrapper = scrapper;
        this.geoCacheCodesList = geoCacheCodesList;
        this.delegate = delegate;
        this.tourID = tourID;
        this.geoCacheOrder = geoCacheOrder;
    }

    @Override
    protected Integer doInBackground(Void... voids)
    {
        Map<String, String> properties = new HashMap<>();
        //properties.put("TourName", tourName[0]);
        properties.put("NumCaches", Integer.toString(geoCacheCodesList.size()));
        Analytics.trackEvent("GeocachingScrappingTask.doBackground", properties);

        // Check that we can login
        /*
        try {
            boolean login = scrapper.login();
            if(!login) return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        for(String code : geoCacheCodesList)
        {
            try {
                GeoCacheWithLogsAndAttributes geoCache = scrapper.getGeoCacheDetails(code);
                geoCaches.add(geoCache);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return 1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if(result == 1){
            delegate.onGeoCachesObtained(geoCaches, tourID, geoCacheOrder);
        }
    }
}
