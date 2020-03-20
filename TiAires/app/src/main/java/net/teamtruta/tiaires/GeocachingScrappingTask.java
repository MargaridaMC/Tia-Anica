package net.teamtruta.tiaires;

import android.os.AsyncTask;

import com.microsoft.appcenter.analytics.Analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeocachingScrappingTask extends AsyncTask<String, Void, Integer> {

    private GeocachingScrapper scrapper;
    private List<String> geocacheCodesList;
    PostGeocachingScrapping delegate;
    private List<Geocache> caches = new ArrayList<Geocache>();

    GeocachingScrappingTask(GeocachingScrapper scrapper, List<String> geocacheCodesList){
        this.scrapper = scrapper;
        this.geocacheCodesList = geocacheCodesList;
    }

    @Override
    protected Integer doInBackground(String... tourName)
    {
        Map<String, String> properties = new HashMap<>();
        properties.put("TourName", tourName[0]);
        properties.put("NumCaches", Integer.toString(geocacheCodesList.size()));
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

        for(String code : geocacheCodesList)
        {
            try {
                Geocache geocache = scrapper.getGeocacheDetails(code);
                caches.add(geocache);
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
            delegate.onGeocachingScrappingTaskResult(caches);
        }
    }
}
