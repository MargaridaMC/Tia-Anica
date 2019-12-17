package app;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("**** TESTER FOR GEOCACHING SCREEN SCRAPPING ****");

        // Uncomment to enable Fiddler
        // System.setProperty("http.proxyHost", "127.0.0.1");
        // System.setProperty("https.proxyHost", "127.0.0.1");
        // System.setProperty("http.proxyPort", "8888");
        // System.setProperty("https.proxyPort", "8888");

        // https://stackoverflow.com/questions/8549749/how-to-capture-https-with-fiddler-in-java
        // note: file is generated in the current folder
        // System.setProperty("javax.net.ssl.trustStore","C:\\PROJECTOS\\Tia-Anica\\POC\\GeoScrapping\\Geoscrapping\\FiddlerKeystore");
        // System.setProperty("javax.net.ssl.trustStorePassword","banana");

        GeocachingScrapper gs = new GeocachingScrapper("lokijota", "geojota#");
        System.out.println("Login result = " + gs.Login());

        Geocache gc = gs.GetGeocacheDetails("GC6B4AK");
        System.out.println("Latitude: " + gc.latitude);
        System.out.println("Longitude: " + gc.longitude);
    }
}