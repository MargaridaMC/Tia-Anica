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
    private static String _allToursFile = "alltours.txt";

    /**
     * Read a list of GeoCachingTour Summaries from a file and return it as an array list
     * The file has the following format: note: not final, there may be other fields there
     * {"tourName":"Mytour","numDNF":0,"numFound":1,"size":3};{"tourName":"Mytour2","numDNF":0,"numFound":1,"size":3};
     */
    public static ArrayList<GeocachingTourSummary> read(String folderPath)
    {
        // Read the full content of the file

        File file = new File(folderPath, _allToursFile);

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
     * Save a set of tour list summaries to storage
     * @param folder folder where to save the caches
     * @param tourList list of tours to save
     * @return Always true
     */
    public static boolean write(String folder, ArrayList<GeocachingTourSummary> tourList)
    {
        // concatenate all the strings together
        String newTourString = "";
        for(GeocachingTourSummary tourSummary : tourList){
            newTourString += (tourSummary.serialize() + ";");
        }

        write(folder, newTourString);

        return true;
    }

    /**
     * Write the string received as parameter to the all tours file
     * @param folder TODO
     * @param newTourString TODO
     * @return Always true
     */
    public static boolean write(String folder, String newTourString)
    {
        File file = new File(folder, _allToursFile);

        try {
            FileOutputStream os = new FileOutputStream(file, false);
            os.write(newTourString.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static void update(String folder, GeocachingTourSummary gts)
    {
        ArrayList<GeocachingTourSummary> tourList = TourList.read(folder);

        for(int i = 0; i< tourList.size(); i++){
            if(tourList.get(i).getName().equals(gts.getName())){
                tourList.set(i, gts);
                break;
            }
        }

        TourList.write(folder, tourList);
    }

    public static void append(String folder, GeocachingTourSummary tour)
    {
        ArrayList<GeocachingTourSummary> allTours = TourList.read(folder);

        // Check if this tour is already in the list and if so replace it
        for (int i = 0; i < allTours.size(); i++) {

            String n = allTours.get(i).getName();

            if (n.equals(tour.getName())) {

                allTours.set(i, tour);
                TourList.write(folder, allTours);
                return;
            }
        }

        // otherwise, append it to the list
        allTours.add(tour);
        TourList.write(folder, allTours);
    }

    /**
     * Remove a tour from the TourList master file
     * @param folder folder where the file is stored
     * @param tourName name of the tour to remove from it
     */
    public static void removeTour(String folder, String tourName)
    {
        ArrayList<GeocachingTourSummary> tourList = TourList.read(folder);

        tourList.removeIf(t -> t.getName().equals(tourName));

        TourList.write(folder, tourList);
    }
}
