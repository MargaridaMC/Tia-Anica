package net.teamtruta.tiaires.integration;

import android.util.Log;

import net.teamtruta.tiaires.data.models.Coordinate;
import net.teamtruta.tiaires.data.models.GeoCache;
import net.teamtruta.tiaires.data.models.GeoCacheAttribute;
import net.teamtruta.tiaires.data.models.GeoCacheAttributeEnum;
import net.teamtruta.tiaires.data.models.GeoCacheLog;
import net.teamtruta.tiaires.data.models.GeoCacheTypeEnum;
import net.teamtruta.tiaires.data.models.GeoCacheWithLogsAndAttributesAndWaypoints;
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum;
import net.teamtruta.tiaires.data.models.Waypoint;
import net.teamtruta.tiaires.extensions.Rot13;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Pair;

/**
 * GeocachingScrapper Useful site: https://www.baeldung.com/java-http-request
 */
public class GeocachingScrapper {

    private String _username, _password;

    private final String TAG = GeocachingScrapper.class.getSimpleName();

    private static final String GEOCACHING_URL = "https://www.geocaching.com";
    private static final String LOGIN_PAGE = "/account/signin";
    private static final String GEOCACHE_PAGE = "/geocache/"; // eg, https://www.geocaching.com/geocache/GC6B4AK
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3980.0 Safari/537.36 Edg/80.0.355.1";
    private String _groundspeakAuthCookie;
    

    public GeocachingScrapper(String user, String password) {
        _username = user;
        _password = password;
    }

    public GeocachingScrapper(String AuthCookie){
        _groundspeakAuthCookie = AuthCookie;
    }

    /*
     * User-Agent is passed because otherwise Java SDK sends "Java something"
     * groundspeak token is sent as a response to the auth / follow redirects has to
     * be disabled otherwise I can't capture that cookie
     */
    public Boolean login(String username, String password) throws IOException {
        // 01. get the login page and extract the relevant information from it
        URL url = new URL(GEOCACHING_URL + LOGIN_PAGE);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

        httpConnection.setRequestMethod("GET");

        httpConnection.setRequestProperty("User-Agent", USER_AGENT);

        int status = httpConnection.getResponseCode(); // this causes the request to be done

        StringBuffer pageContent = readHttpRequest(httpConnection);
        String tokenValue = getTokenFromHtmlBody(pageContent);
        String _requestVerificationCookie = httpConnection.getHeaderField("Set-Cookie").split(";")[0];

        // System.out.println("status GET= " + status);
        // System.out.println("Token in HTML = " + tokenValue);
        // System.out.println("RV Cookie token = " + _requestVerificationCookie);

        httpConnection.disconnect();

        // 02. post to the login page
        httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("POST");

        // header - cookie
        httpConnection.setRequestProperty("Cookie", _requestVerificationCookie);
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);

        // Content of post
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("__RequestVerificationToken", tokenValue);
        parameters.put("ReturnUrl", "/play");
        parameters.put("UsernameOrEmail", username);
        parameters.put("Password", password);

        // write to body of message request
        httpConnection.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(httpConnection.getOutputStream());
        out.writeBytes(getParamsString(parameters));
        out.flush();
        out.close();

        httpConnection.setInstanceFollowRedirects(false); // or else we're redirected to ReturnUrl and loose the
        // gspkauth cookie
        status = httpConnection.getResponseCode();

        String pageContents = readHttpRequest(httpConnection).toString();
        if(pageContents.contains("Your password is incorrect") || pageContents.contains("Your password or username/email is incorrect")) return false;

        _groundspeakAuthCookie = httpConnection.getHeaderField("Set-Cookie");
        // System.out.println("Groundspeak cookie received after post: " +
        // _groundspeakAuthCookie);

        // System.out.println("status POST= " + status);
        status = httpConnection.getResponseCode();

        httpConnection.disconnect();

        // 03 - validate by getting the profile page
        URL profilepage = new URL(GEOCACHING_URL + "/account/settings/profile");
        httpConnection = (HttpURLConnection) profilepage.openConnection();
        httpConnection.setRequestMethod("GET");

        // header - cookie
        httpConnection.setRequestProperty("Cookie", _groundspeakAuthCookie);
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);

        status = httpConnection.getResponseCode();
        // System.out.println("status GET= " + status);

        // PrintWriter pw = new PrintWriter("output.html", "UTF-8");
        // pw.write(ReadHttpRequest(httpConnection).toString());
        // pw.close();

        httpConnection.disconnect();

        return status == 200;
    }

    public Boolean login() throws IOException{
        // Added my Mg
        // Login using the Authentication Cookie (or rather, check that this authentication cookie is valid)

        // Check that this object does have an Authentication Token
        if (_groundspeakAuthCookie == null) return false;

        URL url = new URL(GEOCACHING_URL + LOGIN_PAGE);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("POST");

        // Copied from previous method
        // 03 - validate by getting the profile page
        URL profilepage = new URL(GEOCACHING_URL + "/account/settings/profile");
        httpConnection = (HttpURLConnection) profilepage.openConnection();
        httpConnection.setRequestMethod("GET");

        // header - cookie
        httpConnection.setRequestProperty("Cookie", _groundspeakAuthCookie);
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
        int status = httpConnection.getResponseCode();
        // System.out.println("status GET= " + status);

        // PrintWriter pw = new PrintWriter("output.html", "UTF-8");
        // pw.write(ReadHttpRequest(httpConnection).toString());
        // pw.close();

        httpConnection.disconnect();

        System.out.println("AuthCookie: " + _groundspeakAuthCookie);

        return status == 200;
    }

    public GeoCacheWithLogsAndAttributesAndWaypoints getGeoCacheDetails(String code) throws IOException
    {
        code = code.toUpperCase();

        Log.d(TAG, "Getting geoCache " + code);
        //GeoCache gc = new GeoCache();

        //gc.setCode(code);

        // Obtain the HTML of the page, sending the authentication cookie
        URL geoCachePage = new URL(GEOCACHING_URL + GEOCACHE_PAGE + code);
        HttpURLConnection httpConnection = (HttpURLConnection) geoCachePage.openConnection();

        httpConnection.setRequestMethod("GET");
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);

        // header - cookie
        httpConnection.setRequestProperty("Cookie", _groundspeakAuthCookie);
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
        int status = httpConnection.getResponseCode();

        String pageContents = readHttpRequest(httpConnection).toString();

        // 1. Get cache name
        String regexNamePattern = "<span id=\"ctl00_ContentBody_CacheName\" class=\"tex2jax_ignore\">(.+?)</span>";
        Pattern pattern = Pattern.compile(regexNamePattern);
        Matcher matcher = pattern.matcher(pageContents);

        String name;
        if (matcher.find( )) {
            name = matcher.group(1);
        } else {
            name = "NO MATCH";
        }

        // 2. Get coordinates. eg: <span id="uxLatLon">N 48° 08.192 E 011° 33.158</span>
        String regexLatLongPattern = "<span id=\"uxLatLon\">([NS] [0-9]+° [0-9]+.[0-9]+) ([EW] [0-9]+° [0-9]+.[0-9]+)</span>";
        pattern = Pattern.compile(regexLatLongPattern);
        matcher = pattern.matcher(pageContents);

        Coordinate latitude, longitude;
        if (matcher.find()) {
            String latitudeString = matcher.group(1);
            String longitudeString = matcher.group(2);
            latitude = new Coordinate(latitudeString);
            longitude = new Coordinate(longitudeString);
        } else {
            latitude = null;
            longitude = null;
        }

        // 3. Get Size
        String regexSize = "/images/icons/container/(.+?).gif";
        pattern = Pattern.compile(regexSize);
        matcher = pattern.matcher(pageContents);

        String size;
        if (matcher.find()) {
            size = matcher.group(1);
            size = size.substring(0,1).toUpperCase() + size.substring(1);
        } else {
            size = "NO MATCH";
        }

        // 4. Get Difficulty and Terrain (both use the same regex, repeated instances)
        String regexDifficulty = "<img src=\"/images/stars/stars[_0-9]+.gif\" alt=\"(.*?) out of 5\" />";

        pattern = Pattern.compile(regexDifficulty);
        matcher = pattern.matcher(pageContents);

        double difficulty, terrain;
        if (matcher.find()) {
            difficulty = Double.parseDouble(matcher.group(1));

            if(matcher.find()) {
                terrain = Double.parseDouble(matcher.group(1));
            }
            else {
                terrain = -1;
            }
        } else {
            difficulty = -1;
            terrain = -1;
        }

        // 5. Get the cache type
        String regexType = "<a href=\"/about/cache_types.aspx\" target=\"_blank\" title=\"([a-zA-Z\\s\\(\\)]+).*?\" class=\"cacheImage\">";
        pattern = Pattern.compile(regexType);
        matcher = pattern.matcher(pageContents);

        GeoCacheTypeEnum type;
        if (matcher.find()) {
            type = GeoCacheTypeEnum.Companion.valueOfString(matcher.group(1));
        } else {
            type = GeoCacheTypeEnum.Other;
        }

        // If this is a mystery cache then check if it is solved
        if(type == GeoCacheTypeEnum.Mystery){

            String regexMysterySolved = "\"isUserDefined\":([a-z]+),";
            pattern = Pattern.compile(regexMysterySolved);
            matcher = pattern.matcher(pageContents);
            if (matcher.find()) {
                boolean isSolved =  matcher.group(1).equals("true");
                if(isSolved){
                    type = GeoCacheTypeEnum.Solved;
                }
            }
        }


        // 6. Have I found it?
        String regexFound = "<strong id=\"ctl00_ContentBody_GeoNav_logText\">(Did Not Find|Found It!)</strong>";
        pattern = Pattern.compile(regexFound);
        matcher = pattern.matcher(pageContents);

        VisitOutcomeEnum visit;
        if (matcher.find()) {
            visit = matcher.group(1).contains("Found It!") ? VisitOutcomeEnum.Found : VisitOutcomeEnum.DNF;
        } else {
            visit = VisitOutcomeEnum.NotAttempted;
        }

        // Check if it is disabled
        String regexDisabled = "This cache is temporarily unavailable.";
        pattern = Pattern.compile(regexDisabled);
        matcher = pattern.matcher(pageContents);
        if (matcher.find()){
            visit = VisitOutcomeEnum.Disabled;
        }


        // 7. Hint. Note: \x28 is "("" and \x29 is ")"
        //String regexHint = "<a id=\"ctl00_ContentBody_lnkDH\" onclick=\"dht\\(this\\);return&#32;false;\" title=\"Decrypt\" href=\"../seek/#\">Decrypt</a>\\) </p><div id=\"div_hint\" class=\"span-8 WrapFix\">\\s*(.*?)</div><div id='dk'";
        String regexHint = "class=\"span-8 WrapFix\">(.*?)</div><div";
        pattern = Pattern.compile(regexHint);
        matcher = pattern.matcher(pageContents);

        String hint;
        if (matcher.find()) {
            String group = matcher.group(1);
            String temp = group;
            //temp = temp.replaceAll(" ", "");
            temp = temp.trim();
            System.out.println(temp.length());
            if(temp.length()==0) {
                hint = "NO MATCH";
                //gc.setHint("NO MATCH");
            }
            else {
                hint = Rot13.Decode(group);
                hint = hint.trim();
                hint = hint.replaceAll("<oe>", "\n");
                //gc.setHint(hint.replaceAll("<oe>", "\n"));
            }

        } else {
            hint = "NO MATCH";
            //gc.setHint("NO MATCH");
        }


        // 8. Favourites
        String regexFavourites = "<span class=\"favorite-value\">\\s*(.*?)\\s*</span>";
        pattern = Pattern.compile(regexFavourites);
        matcher = pattern.matcher(pageContents);

        int favourites;
        if (matcher.find()) {
            favourites = Integer.parseInt(matcher.group(1));
            //gc.setFavourites(Integer.parseInt(matcher.group(1)));
        } else {
            favourites = 0;
            //gc.setFavourites(0);
        }

        // 9. Last logs and their dates
        int logsParsed = 0, maxLogsToParse = 10;
        String regexLogType = "\"LogType\":\"([a-zA-Z'\\s]+)\"";
        String regexLogDate = "\"Visited\":\"([A-Za-z0-9/\\.]+)";

        pattern = Pattern.compile(regexLogType);
        matcher = pattern.matcher(pageContents);

        Pattern patternDates = Pattern.compile(regexLogDate);
        Matcher matcherDates = patternDates.matcher(pageContents);

        // It seems that the log dates are captured in different ways in different accounts
        SimpleDateFormat dateFormatter0 = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dateFormatter1 = new SimpleDateFormat("dd.MMM.yyyy");
        ArrayList<GeoCacheLog> recentLogs = new ArrayList<>();

        try{ // Try first date format
            while(matcher.find() && matcherDates.find() && logsParsed < maxLogsToParse)
            {

                VisitOutcomeEnum logType = VisitOutcomeEnum.Companion.valueOfString( matcher.group(1));
                Date logDate = dateFormatter0.parse(matcherDates.group(1));
                GeoCacheLog log = new GeoCacheLog(logType, logDate);

                recentLogs.add(log);
                logsParsed++;
            }
        } catch (ParseException e0){

            try{ // Try second date format

                matcher = pattern.matcher(pageContents);
                matcherDates = patternDates.matcher(pageContents);

                while(matcher.find() && matcherDates.find() && logsParsed < maxLogsToParse)
                {

                    VisitOutcomeEnum logType = VisitOutcomeEnum.Companion.valueOfString( matcher.group(1));
                    Date logDate = dateFormatter1.parse(matcherDates.group(1));
                    GeoCacheLog log = new GeoCacheLog(logType, logDate);

                    recentLogs.add(log);
                    logsParsed++;
                }
            } catch (ParseException e1){
                e1.printStackTrace();
            }

        }

        // 10. Get cache attributes
        List<GeoCacheAttribute> attributes = new ArrayList();
        Pattern attributePageSectionPattern = Pattern.compile("Attributes((.|\\n)*)Attributes");
        Matcher attributePageSectionMatcher =  attributePageSectionPattern.matcher(pageContents);
        if(attributePageSectionMatcher.find()){
            String attributePageSection = attributePageSectionMatcher.group(0);
            for(GeoCacheAttributeEnum attribute: GeoCacheAttributeEnum.values()){
                String atString = attribute.getAttributeString();
                pattern = Pattern.compile(atString);
                matcher = pattern.matcher(attributePageSection);
                if(matcher.find()){
                    attributes.add(new GeoCacheAttribute(attribute));
                }
            }
        }

        // 11. Get Cache Waypoints if they exist
        String regexAdditionalWaypointsSection = "<table class=\"Table alternating-row-stacked\" id=\"ctl00_ContentBody_Waypoints\">(.*?)</tbody> </table>";
        Pattern waypointSectionPattern = Pattern.compile(regexAdditionalWaypointsSection);
        Matcher waypointSectionMatcher = waypointSectionPattern.matcher(pageContents);

        ArrayList<Waypoint> waypoints = new ArrayList<>();

        if(waypointSectionMatcher.find()){
            // Then this cache has an "Additional Waypoint" section
            String additionalWaypointSection = waypointSectionMatcher.group(0);

            Document doc = Jsoup.parse(additionalWaypointSection);
            // Loop through each row in the "Additional Waypoint" table
            Elements trElements = doc.select("tr");

            // Starting at index 1 as the first one is just the titles of the columns
            for(int i = 1; i < trElements.size(); i+=2){

                Element row = trElements.get(i);
                Elements tds = row.select("td");
                String waypointType = tds.get(4).text();
                if(waypointType.contains("Reference Point")) {
                    continue;
                }
                String waypointName = tds.get(4).select("a").text();
                String coordinates = tds.get(5).text();

                Coordinate waypointLatitude, waypointLongitude;
                if(coordinates.equals("???")){
                    waypointLatitude = null;
                    waypointLongitude = null;
                } else {
                    Pair<Coordinate, Coordinate> waypointCoordinates = Coordinate.Companion.fromFullCoordinates(coordinates);
                    waypointLatitude = waypointCoordinates.getFirst();
                    waypointLongitude = waypointCoordinates.getSecond();
                }

                Elements notesSection = trElements.get(i + 1).select("td");
                String notes = notesSection.get(2).text();

                Waypoint waypoint = new Waypoint(
                        waypointName,
                        waypointLatitude,
                        waypointLongitude,
                        Waypoint.Companion.getWAYPOINT_NOT_ATTEMPTED(),
                        waypointType.contains("Parking Area"),
                        notes);
                waypoints.add(waypoint);

            }

        }

        // Refactor Login() as appropriate
        httpConnection.disconnect();


        GeoCache geoCache = new GeoCache(code, name, latitude, longitude, size,
                difficulty, terrain, type, visit,
                hint, favourites);
        return new GeoCacheWithLogsAndAttributesAndWaypoints(geoCache, recentLogs, attributes, waypoints);
    }

    private String getTokenFromHtmlBody(StringBuffer htmlPage)
    {
        // The relevant HTML snipet is:
        // <form action="/account/signin" id="SignupSignin" method="post"><input name="__RequestVerificationToken" type="hidden" value="3smnw46ATdX1c__TgiNxUAnPQ50MPeXaJet0DrUUEaww7ttLR_gIL3Z1G0dZimKQ3maWXTHNOPdOhNJP1cLDLNntWS01" /><input Length="5" id="ReturnUrl" name="ReturnUrl" type="hidden" value="/play" />            <div class="margin-center" style="max-width: 90%">

        String marker = "__RequestVerificationToken\" type=\"hidden\" value=\"";
        String exampleToken = "3smnw46ATdX1c__TgiNxUAnPQ50MPeXaJet0DrUUEaww7ttLR_gIL3Z1G0dZimKQ3maWXTHNOPdOhNJP1cLDLNntWS01";

        int whereIsItPosition = htmlPage.indexOf(marker);
        return htmlPage.substring(whereIsItPosition + marker.length(), whereIsItPosition + marker.length() + exampleToken.length());
    }

    // from: https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-networking-2/src/main/java/com/baeldung/httprequest/ParameterStringBuilder.java
    public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }

    private StringBuffer readHttpRequest(HttpURLConnection httpConnection) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));

        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        return content;
    }

    public String getAuthenticationCookie(){
        return _groundspeakAuthCookie;
    }
}