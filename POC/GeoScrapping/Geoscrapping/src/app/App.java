package app;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {
        
        System.out.println("**** TESTER FOR GEOCACHING SCREEN SCRAPPING AND WRITING TO FILE ****"); 

        GeocachingTour tour = new GeocachingTour("Limpar Schwabing");
        GeocachingScrapper scrapper = new GeocachingScrapper();
        boolean loginSuccess = scrapper.login("mgthesilversardine", "12142guida");
        System.out.println("Login = " + loginSuccess);

        Geocache gc1 = scrapper.getGeocacheDetails("GC3AK7Y");
        Geocache gc2 = scrapper.getGeocacheDetails("GC3443H");

        tour.addToTour(gc1);
        tour.addToTour(gc2);

        System.out.println("Size: " + tour.size());

        JSONArray cacheArray = tour.toJSON();

        System.out.println(cacheArray.toJSONString());

        GeocachingTour newTour = new GeocachingTour("New tour");
        newTour.fromJSON(cacheArray);

        System.out.println(newTour.size());


/*
        //Write JSON file
        try (FileWriter file = new FileWriter("cacheTour.json")) {
 
            file.write(cacheArray.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }


        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader("cacheTour.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            JSONArray employeeList = (JSONArray) obj;
            System.out.println("From file:");
            System.out.println(employeeList);
            
        } catch (Exception e) {
            e.printStackTrace();
        } 

        GeocachingTour newTour = new GeocachingTour("New tour");
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