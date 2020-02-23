package net.teamtruta.tiaires;

import android.os.AsyncTask;
import android.util.Log;

public class LoginTask extends AsyncTask<GeocachingScrapper, Void, Integer> {

    private boolean success = false;
    GeocachingLogin delegate = null;

    @Override
    protected Integer doInBackground(GeocachingScrapper... params){

        GeocachingScrapper gs = params[0];

        try{
            this.success = gs.login();
            Log.d("TAG", "Here we are in Async");
            return 1;

        } catch (Exception e){
            e.fillInStackTrace();
            Log.d("TAG", "Something went wrong");
            return 0;
        }
    }

    protected void onPostExecute(Integer result){

        if(result == 1) delegate.geocachingLogin(success);

    }


}
