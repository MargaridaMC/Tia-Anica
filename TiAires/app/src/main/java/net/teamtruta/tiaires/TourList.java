package net.teamtruta.tiaires;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class that represents a list of tours, with methods to read/persist to storage.
 **/
public class TourList
{
    private static String _allToursFile = "alltours.txt";

    /**
     * Check if the TourList file exists
     * @param folder Folder where the file should be stored
     * @return true or false according to the file existing or not. If empty but exists, returns True
     */
    public static boolean exists(String folder)
    {
        File allToursFile = new File(folder, _allToursFile);
        return allToursFile.exists();
    }

    /**
     * Read a list of GeoCachingTour Summaries from a file and return it as an array list
     * The file has the following format: note: not final, there may be other fields there
     * {"tourName":"Mytour","numDNF":0,"numFound":1,"size":3};{"tourName":"Mytour2","numDNF":0,"numFound":1,"size":3};
     */
    public static ArrayList<GeocachingTourSummary> read(String folder)
    {
        // Read the full content of the file
        try {
            // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return new ObjectMapper().readValue(new File(folder, _allToursFile), new TypeReference<ArrayList<GeocachingTourSummary>>(){});
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return new ArrayList<GeocachingTourSummary>(); // return an empty list
        }
    }

    /**
     * Save a set of tour list summaries to storage
     * @param folder folder where to save the caches
     * @param tourList list of tours to save
     * @return Always true
     */
    public static boolean write(String folder, ArrayList<GeocachingTourSummary> tourList)
    {
        try
        {
            tourList.sort(new GeocachingTourSummaryNameSorter());
            new ObjectMapper().writeValue(new File(folder, _allToursFile), tourList);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update a tour in a given tour list, matching them by name.
     * If the tour doesn't exist with the same name, the method does nothing
     * @param folder Data file folder
     * @param gts Tour to update in the TourList
     */
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

    /**
     * Add a tour to the tour list data store
     * @param folder Data file folder
     * @param tour Tour to add in the TourList
     */
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
