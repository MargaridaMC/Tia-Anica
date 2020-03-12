package app;

import java.util.ArrayList;

public class App {
    public static void main(String[] args) throws Exception {
        
        
        GeocachingTour tour = new GeocachingTour("My tour");
        GeocachingScrapper scrapper = new GeocachingScrapper();
        boolean loginSuccess = scrapper.login("mgthesilversardine", "12142guida");
        System.out.println("Login = " + loginSuccess);

        Geocache gc1 = scrapper.getGeocacheDetails("GC40");

        System.out.println(gc1.getRecentLogs());
        
        /*
        Geocache gc2 = scrapper.getGeocacheDetails("GC23EH1");

        tour.addToTour(new Geocache[] {gc1, gc2});

        GeocachingTour.write(".", tour);

        GeocachingTour newTour = GeocachingTour.read(".", tour.getName());
        System.out.println(tour.getName());
    */
        /*

        System.out.println("** TEST OBTAINED CACHE TYPES**");

        // Traditional
        gc1 = scrapper.getGeocacheDetails("GC7GX91");
        System.out.println("Type: " + gc1.type + " - should be Traditional");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        
        // Mystery
        ;
        gc1 = scrapper.getGeocacheDetails("GC23EH1");
        System.out.println("Type: " + gc1.type + " - should be Mystery");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Multi
        gc1 = scrapper.getGeocacheDetails("GCM2RJ")println("Type: " + gc1.type + " - should be Multi");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Earth
        gc1 = scrapper.getGeocacheDetails("GC8F4JH");
        System.out.println("Type: " + gc1.type + " - should be Earth");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Letterbox
        gc1 = scrapper.getGeocacheDetails("GC35AKX");
        System.out.println("Type: " + gc1.type + " - should be Letterbox");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Event
        gc1 = scrapper.getGeocacheDetails("GC8KFF5");
        System.out.println("Type: " + gc1.type + " - should be Event");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // CITO
        gc1 = scrapper.getGeocacheDetails("GC8JEEZ");
        System.out.println("Type: " + gc1.type + " - should be Cache In Trash Out Event");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Mega
        gc1 = scrapper.getGeocacheDetails("GC84EA4");
        System.out.println("Type: " + gc1.type + " - should be Mega Event");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Giga
        gc1 = scrapper.getGeocacheDetails("GC7WWWW");
        System.out.println("Type: " + gc1.type + " - should be Giga Event");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Wherigo
        gc1 = scrapper.getGeocacheDetails("GC5KN51");
        System.out.println("Type: " + gc1.type + " - should be Wherigo");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Geocaching HQ
        gc1 = scrapper.getGeocacheDetails("GCK25B");
        System.out.println("Type: " + gc1.type + " - should be HQ");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // GPS Adventures Maze Exhibit
        gc1 = scrapper.getGeocacheDetails("GC13A70");
        System.out.println("Type: " + gc1.type + " - should be  GPS Adventures Maze Exhibit");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Lab
        //gc1 = scrapper.getGeocacheDetails("GCK25B");
        //System.out.println("Type: " + gc1.type + " - should be lab");

        // Geocaching HQ Celebration
        gc1 = scrapper.getGeocacheDetails("GC896PK");
        System.out.println("Type: " + gc1.type + " - should be Geocaching HQ Celebration");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Geocaching HQ Block Party
        gc1 = scrapper.getGeocacheDetails("GC5G4X5");
        System.out.println("Type: " + gc1.type + " - should be Geocaching HQ Block Party");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Community Celebration Event
        gc1 = scrapper.getGeocacheDetails("GC8K0ZE");
        System.out.println("Type: " + gc1.type + " - should be Community Celebration Event");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Virtual
        gc1 = scrapper.getGeocacheDetails("GC7F57");
        System.out.println("Type: " + gc1.type + " - should be Virtual");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Webcam
        gc1 = scrapper.getGeocacheDetails("GCHNBF");
        System.out.println("Type: " + gc1.type + " - should be Webcam");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Project A.P.E. Cache
        gc1 = scrapper.getGeocacheDetails("GC12AC");
        System.out.println("Type: " + gc1.type + " - should be Project A.P.E. Cache");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));

        // Locationless (Reverse) Cache
        gc1 = scrapper.getGeocacheDetails("GC8FR0G");
        System.out.println("Type: " + gc1.type + " - should be Locationless (Reverse) Cache");
        System.out.println(CacheTypeEnum.valueOfTypeString(gc1.type));
        
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