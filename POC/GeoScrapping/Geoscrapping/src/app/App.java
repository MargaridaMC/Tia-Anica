package app;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    public static void main(String[] args) throws Exception {
        
        // Test reading and writting tour list to file
        
        GeocachingTour tour = new GeocachingTour("My tour");
        GeocachingScrapper scrapper = new GeocachingScrapper();
        boolean loginSuccess = scrapper.login("mgthesilversardine", "12142guida");
        System.out.println("Login = " + loginSuccess);

        Geocache gc1 = scrapper.getGeocacheDetails("GC3AK7Y");
        Geocache gc2 = scrapper.getGeocacheDetails("GC3443H");

        tour.addToTour(gc1);
        tour.addToTour(gc2);

        JSONObject tourJSON = tour.toJSON();
        System.out.println(tourJSON);

        GeocachingTour newTour = GeocachingTour.fromJSON(tourJSON);
        System.out.println(newTour.getName());

        /*
        String allTours = tour0.getMetaDataJSON().toString() + ";" + tour1.getMetaDataJSON().toString();
        System.out.print(allTours);

        File file = new File("allTours.txt");

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(allTours.getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
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

        System.out.println(allToursFromFile);

        String[] tours = allToursFromFile.split(";");
        ArrayList<GeocachingTour> tourList = new ArrayList<>();
        for(String tourString : tours){
            JSONObject tourJSON = new JSONObject(tourString);
            GeocachingTour tour = new GeocachingTour("name");
            tour.fromMetaDataJSON(tourJSON);
            tourList.add(tour);
        }

        System.out.println(tourList);

        FileOutputStream os = new FileOutputStream(file, true);
        String newTourString = ";" + tour0.getMetaDataJSON().toString();
        os.write(newTourString.getBytes(), 0, newTourString.length());
        os.close();
*/
        /*
        System.out.println("**** TESTER FOR GEOCACHING SCREEN SCRAPPING AND WRITING TO FILE ****"); 
        String name = "Limpar Schwabing";
        GeocachingTour tour = new GeocachingTour(name);
        GeocachingScrapper scrapper = new GeocachingScrapper();
        boolean loginSuccess = scrapper.login("mgthesilversardine", "12142guida");
        System.out.println("Login = " + loginSuccess);

        Geocache gc1 = scrapper.getGeocacheDetails("GC3AK7Y");
        Geocache gc2 = scrapper.getGeocacheDetails("GC3443H");

        tour.addToTour(gc1);
        tour.addToTour(gc2);
        */
        // Test to and from JSON
        /*
        JSONObject tourJSON = tour.toJSON();

        System.out.println(tourJSON.toString());

        GeocachingTour tourFromJSON = new GeocachingTour("New Tour");
        tourFromJSON.fromJSON(tourJSON);

        System.out.println("From JSON: " + tourFromJSON.toString());
        */ 

        // Test to and from file
        /*
        File file = new File(".");
        tour.toFile(file);

        GeocachingTour newTour = GeocachingTour.fromFile(file, name);
        System.out.println(newTour.toJSON());
        */

        // System.out.println("**** TESTER FOR GEOCACHING SCREEN SCRAPPING ****");

        // System.out.println("** TEST OTHER CLASSES **");
        
        /*
        GeocachingTour tour = new GeocachingTour("Limpar Schwabing");

        Geocache gc1 = new Geocache(); gc1.code = "GC1";
        Geocache gc2 = new Geocache(); gc2.code = "GC2";

        System.out.println("Size: " + tour.size());

        tour.addToTour(gc1);
        tour.addToTour(gc2);
        tour.addToTour(gc2);

        System.out.println("Size: " + tour.size());

        tour.getCacheInTour("gc1").setVisit(FoundEnumType.Found, "muita boa adorei", false);

        System.out.println(tour.getCacheInTour("gc1").getNotes());

        tour.removeFromTour("gc2");
        System.out.println("gc está na tour?" + tour.getCacheInTour("gc2") != null ? true : false);
        */

        //#region Uncomment to enable Fiddler    
        
        /*
        // System.setProperty("http.proxyHost", "127.0.0.1");
        // System.setProperty("https.proxyHost", "127.0.0.1");
        // System.setProperty("http.proxyPort", "8888");
        // System.setProperty("https.proxyPort", "8888");

        // https://stackoverflow.com/questions/8549749/how-to-capture-https-with-fiddler-in-java
        // note: file is generated in the current folder
        // System.setProperty("javax.net.ssl.trustStore","C:\\PROJECTOS\\Tia-Anica\\POC\\GeoScrapping\\Geoscrapping\\FiddlerKeystore");
        // System.setProperty("javax.net.ssl.trustStorePassword","banana");
        */

        //#endregion

        //return;

        //System.out.println("** TEST GETTING CACHE DATA **");

        //GeocachingScrapper gs = new GeocachingScrapper("gspkauth=o43gsoeqknyRROoCPZCHw0CO4LdrL7onbXRCNC0KceIK8PXB5OhIMoQrudQQWqLvVJyS8cKn9FBXkqm01pqQxJUzUqWnEGR7-7Gr5DwRtgpzE6PI00P9BVxaDL_nZwTwymL66qBJc2GBcop3BKw9YNk0C2F8mFlQXZDpfbZP0hA1; domain=.geocaching.com; expires=Wed, 25-Mar-2020 10:26:08 GMT; path=/; secure; HttpOnly");
        //System.out.println("Login result from Authentication Cookie = " + gs.login());
        
        // Should be false, since object doesn't have an authentication cookie yet
        // System.out.println("Login result from Authentication Cookie = " + gs.login());

        // GeocachingScrapper gs = new GeocachingScrapper();
        // Logging in with username and password retrieves an authentication cookie
        // System.out.println("Login result = " + gs.login("lokijota", "geojota#"));
        // System.out.println("Login result = " + gs.login("mgthesilversardine", "gre"));

        // Now we should be able to access the page using only the authentication cookie
        // System.out.println("Login result from Authentication Cookie = " + gs.login());

        // long startTime = System.currentTimeMillis();
        // Geocache gc = gs.getGeocacheDetails("GC3YA65"); // GC1RG9M"); // GC6VZ9C"); // GC37M58");
        // long endTime = System.currentTimeMillis();
        // System.out.println("\nGet cache details took " + (endTime - startTime) + " ms\n");

        // System.out.println(" Name: " + gc.name);
        // System.out.println(" Latitude: " + gc.latitude);
        // System.out.println(" Longitude: " + gc.longitude);
        // System.out.println(" Size: " + gc.size);
        // System.out.println(" Difficulty: " + gc.difficulty);
        // System.out.println(" Terrain: " + gc.terrain);
        // System.out.println(" Type: " + gc.type);
        // System.out.println(" Found It? " + gc.foundIt);
        // System.out.println(" Hint: " + gc.hint);
        // System.out.println(" Favourites: " + gc.favourites);
        // System.out.println(" Nb logs: " + gc.recentLogs.size());

        // System.out.println(" Days since last log: "+ gc.CountDaysSinceLastFind());
        // System.out.println(" Average days between finds: "+ gc.AverageDaysBetweenFinds());

        // TODO: remove HTML tags from Hint in case it has them.
    }
}