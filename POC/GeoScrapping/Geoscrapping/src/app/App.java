package app;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("**** TESTER FOR GEOCACHING SCREEN SCRAPPING ****");

        System.out.println("** TEST OTHER CLASSES **");

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

        return;

        // System.out.println("** TEST GETTING CACHE DATA **");

        // GeocachingScrapper gs = new GeocachingScrapper("lokijota", "geojota#");
        // System.out.println("Login result = " + gs.login());

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