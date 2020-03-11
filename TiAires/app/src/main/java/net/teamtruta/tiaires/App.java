package net.teamtruta.tiaires;

import android.app.Application;
import android.content.Context;
import android.util.TypedValue;

public class App extends Application {

    private static Context mContext;
    private static String _allToursFile = "alltours.txt";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }

    public static String getTourRoot(){
        return mContext.getFilesDir().toString() + "/" + mContext.getString(R.string.tour_folder);
    }

    public static String getAllToursFilePath(){
        return getTourRoot() + "/" + _allToursFile;
    }

    public static int spToPx(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, mContext.getResources().getDisplayMetrics());
    }
}