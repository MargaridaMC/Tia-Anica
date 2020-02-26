package net.teamtruta.tiaires;

import android.os.AsyncTask;

import java.util.List;

public class GeocachingScrappingTask extends AsyncTask<String, Void, Integer> {

    private GeocachingScrapper scrapper;
    private List<String> geocacheCodesList;
    PostGeocachingScrapping delegate;
    private GeocachingTour tour;

    GeocachingScrappingTask(GeocachingScrapper scrapper, List<String> geocacheCodesList){
        this.scrapper = scrapper;
        this.geocacheCodesList = geocacheCodesList;
    }

    @Override
    protected Integer doInBackground(String... tourName) {

        // Check that we can login
        /*
        try {
            boolean login = scrapper.login();
            if(!login) return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        // Create tour and fill it with the data
        tour = new GeocachingTour(tourName[0]);

        for(String code:geocacheCodesList){

            try {
                Geocache gc = scrapper.getGeocacheDetails(code);
                tour.addToTour(gc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return 1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if(result == 1){
            delegate.onGeocachingScrappingTaskResult(tour);
        }
    }
}
