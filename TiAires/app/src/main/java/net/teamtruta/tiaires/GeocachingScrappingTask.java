package net.teamtruta.tiaires;

import android.os.AsyncTask;

import com.microsoft.appcenter.analytics.Analytics;

import java.util.ArrayList;
import java.util.List;

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
        Analytics.trackEvent("GeocachingScrappingTask.doBackground - geoscrapping caches");
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

                caches.add(scrapper.getGeocacheDetails(code));

            } catch (Exception e)
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
