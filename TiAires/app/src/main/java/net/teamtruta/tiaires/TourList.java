package net.teamtruta.tiaires;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class that represents a list of tours, with methods to read/persist to storage.
 **/
public class TourList
{
    /**
     * Read a list of GeoCachingTour Summaries from a file and return it as an array list
     * The file has the following format:
     * {"tourName":"Mytour","numDNF":0,"numFound":1,"size":3};{"tourName":"Mytour2","numDNF":0,"numFound":1,"size":3};
     * TODO: this could be a single json document without the ; separator
     */
    public static ArrayList<GeocachingTourSummary> fromFile (File file)
    {
        // Read the full content of the file
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

        String[] tourSummaryJsons = allToursFromFile.split(";");
        ArrayList<GeocachingTourSummary> tourList = new ArrayList<>();

        for(String tourJson : tourSummaryJsons)
        {
            if(tourJson.equals("")) continue;

            tourList.add(GeocachingTourSummary.deserialize(tourJson));
        }

        return tourList;
    }

    /**
     * Save a list of caches to a specified file,in Json format
     * TODO - return true always?
     */
    public static boolean toFile (ArrayList<GeocachingTourSummary> tourList, File file)
    {
        // concatenate all the strings together (TODO: best to save as json object?...)
        String newTourString = "";
        for(GeocachingTourSummary tourSummary : tourList){
            newTourString += (tourSummary.serialize() + ";");
        }

        toFile(newTourString, file);
        // save to file
        //try {
        //    FileOutputStream os = new FileOutputStream(file, false);
        //   os.write(newTourString.getBytes());
        //    os.close();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        return true;
    }

    /**
     * Write the string received as parameter to the specified file
     * TODO - return true always?
     * @param newTourString String to write
     * @param file File to write to
     * @return Always true
     */
    public static boolean toFile (String newTourString, File file)
    {
        try {
            FileOutputStream os = new FileOutputStream(file);
            os.write(newTourString.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static void appendToFile(GeocachingTourSummary tour, File file)
    {
        ArrayList<GeocachingTourSummary> allTours = TourList.fromFile(file);

        // Check if this tour is already in the list
        for (int i = 0; i < allTours.size(); i++) {

            String n = allTours.get(i).getName();

            if (n.equals(tour.getName())) {

                allTours.set(i, tour);
                TourList.toFile(allTours, file);
                return;
            }

        }

        // Else just append it to the list
        allTours.add(tour);
        TourList.toFile(allTours, file);
    }

}
