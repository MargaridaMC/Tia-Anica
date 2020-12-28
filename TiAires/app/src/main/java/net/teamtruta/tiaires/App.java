package net.teamtruta.tiaires;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class App extends Application {

    private static Context mContext;

    static String TOUR_ID_EXTRA = "tourID";
    static String EDIT_EXTRA = "edit";
    static String GEOCACHE_ID_EXTRA = "geoCacheID";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }

    public static String getAuthenticationCookie(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String authCookie = sharedPreferences.getString(mContext.getString(R.string.authentication_cookie_key), "");

        if (authCookie.equals("")) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Login information is missing. Please input your credentials in the login screen.");
            builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, id) -> {
                Intent intent = new Intent(mContext, LoginActivity.class);
                mContext.startActivity(intent);
            });
            builder.setNegativeButton(mContext.getString(R.string.cancel), ((dialog, which) -> {
            }));

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return authCookie;
    }

}