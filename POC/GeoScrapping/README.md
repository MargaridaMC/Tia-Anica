# GeoScrapping

Code to do screenscrapping of authenticated pages of geocaching.com .

Currently getting:

    public String name;
    public String latitude;
    public String longitude;
    public String size; // ex: Other, Small
    public String difficulty; // eg, 1.5
    public String terrain; // eg, 2.5
    public String type; // Normal, etc.

    public int foundIt; // 0 - no, 1 - DNF, 2 - yes

The following pages were very useful:

To explain the atrociously bad HTTP classes:
https://www.baeldung.com/java-http-request

To get Fiddler to capture the Java requests:
https://stackoverflow.com/questions/8549749/how-to-capture-https-with-fiddler-in-java
