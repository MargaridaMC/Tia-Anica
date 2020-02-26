package net.teamtruta.tiaires;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TourList {

    public static ArrayList<GeocachingTour> fromFile (File file){

        String allToursFromFile;
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        allToursFromFile = new String(bytes);

        String[] tours = allToursFromFile.split(";");
        ArrayList<GeocachingTour> tourList = new ArrayList<>();
        for(String tourString : tours){
            if(tourString.equals("")) continue;
            JSONObject tourJSON = null;
            try {
                tourJSON = new JSONObject(tourString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            GeocachingTour tour = new GeocachingTour("name");
            tour.fromMetaDataJSON(tourJSON);
            tourList.add(tour);
        }

        return tourList;
    }

    public static boolean toFile (ArrayList<GeocachingTour> tourList, File file){

        String newTourString = "";
        for(GeocachingTour tour:tourList){
            newTourString += (tour.getMetaDataJSON().toString() + ";");
        }

        try {
            FileOutputStream os = new FileOutputStream(file, false);
            os.write(newTourString.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    public static boolean toFile (String newTourString, File file){

        try {
            FileOutputStream os = new FileOutputStream(file);
            os.write(newTourString.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


}
