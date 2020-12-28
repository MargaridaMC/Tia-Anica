package net.teamtruta.tiaires;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.appcenter.analytics.Analytics;

import java.util.HashMap;
import java.util.Map;

public class LoginTask extends AsyncTask<GeocachingScrapper, Void, Integer> {

    String TAG = LoginTask.class.getSimpleName();

    private boolean _success = false;
    private final GeocachingLogin _delegate;
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
            Log.d(TAG, "Running login task");
            // Login can be done either with username and password or with an authentication cookie
            if(_username != null && _password != null){
                _success = gs.login(_username, _password);

                Map<String, String> properties = new HashMap<>();
                properties.put("LoginType", "Username/Password");
                properties.put("Result", Boolean.toString(_success));
                Analytics.trackEvent("LoginTask.doInBackground", properties);
            } else {
                // Assume that login with username and password has already been done and we can use the Authentication Cookie to do the login
               _success = gs.login();

                Map<String, String> properties = new HashMap<>();
                properties.put("LoginType", "Token Reuse");
                properties.put("Result", Boolean.toString(_success));
                Analytics.trackEvent(String.format("LoginTask.doInBackground", properties));
            }

            return 1;

        } catch (Exception e){
            e.fillInStackTrace();
            Log.d(TAG, "Something went wrong");
            return 0;
        }
    }

    protected void onPostExecute(Integer result){

        if(result == 1){
            _delegate.geocachingLogin(_username, _success, gs.getAuthenticationCookie());
        }

    }


}
