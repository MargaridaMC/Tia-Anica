package net.teamtruta.tiaires;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.appcenter.analytics.Analytics;

public class LoginTask extends AsyncTask<GeocachingScrapper, Void, Integer> {

    private boolean _success = false;
    private GeocachingLogin _delegate;
    private GeocachingScrapper gs;

    private String _username;
    private String _password;

    LoginTask(GeocachingLogin delegate){
        _delegate = delegate;
    }

    LoginTask(String username, String password, GeocachingLogin delegate){
        _username = username;
        _password = password;
        _delegate = delegate;
    }

    @Override
    protected Integer doInBackground(GeocachingScrapper... params){

        gs = params[0];

        try{
            Log.d("TAG", "Here we are in Async");
            // Login can be done either with username and password or with an authentication cookie
            if(_username != null && _password != null){
                _success = gs.login(_username, _password);
                Analytics.trackEvent(String.format("Login with u/p has result %s", _success));
            } else {
                // Assume that login with username and password has already been done and we can use the Authentication Cookie to do the login
               _success = gs.login();
                Analytics.trackEvent(String.format("Login with stored authentication cookie had result %s", _success));
            }

            return 1;

        } catch (Exception e){
            e.fillInStackTrace();
            Log.d("TAG", "Something went wrong");
            return 0;
        }
    }

    protected void onPostExecute(Integer result){

        if(result == 1){
            _delegate.geocachingLogin(_username, _success, gs.getAuthenticationCookie());
        }

    }


}
