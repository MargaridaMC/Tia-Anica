package net.teamtruta.tiaires;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }

    public static String getTourRoot(){
        String path = mContext.getFilesDir().toString() + "/" + mContext.getString(R.string.tour_folder);
        return path;
    }

}