package net.teamtruta.tiaires;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GeocachingScrapper Useful site: https://www.baeldung.com/java-http-request
 */
public class GeocachingScrapper {

    //private String _username, _password;

    private static final String GEOCACHING_URL = "https://www.geocaching.com";
    private static final String LOGIN_PAGE = "/account/signin";
    private static final String GEOCACHE_PAGE = "/geocache/"; // eg, https://www.geocaching.com/geocache/GC6B4AK
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3980.0 Safari/537.36 Edg/80.0.355.1";
    private String _requestVerificationCookie;
    private String _groundspeakAuthCookie;

    GeocachingScrapper() {
        //_username = user;
        //_password = password;
    }

    // For saving and reading purposes
    GeocachingScrapper(String AuthCookie) {
        _groundspeakAuthCookie = AuthCookie;
    }

    /*
     * User-Agent is passed because otherwise Java SDK sends "Java something"
     * groundspeak token is sent as a response to the auth / follow redirects has to
     * be disabled otherwise I can't capture that cookie
     */
    Boolean login(String username, String password) throws IOException {
        // 01. get the login page and extract the relevant information from it
        URL url = new URL(GEOCACHING_URL + LOGIN_PAGE);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

        httpConnection.setRequestMethod("GET");

        httpConnection.setRequestProperty("User-Agent", USER_AGENT);

        httpConnection.getResponseCode(); // this causes the request to be done

        StringBuffer pageContent = readHttpRequest(httpConnection);
        String tokenValue = getTokenFromHtmlBody(pageContent);
        _requestVerificationCookie = httpConnection.getHeaderField("Set-Cookie").split(";")[0];

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
        httpConnection.getResponseCode();

        String pageContents = readHttpRequest(httpConnection).toString();
        if(pageContents.contains("Your password is incorrect")) return false;

        _groundspeakAuthCookie = httpConnection.getHeaderField("Set-Cookie");
        // System.out.println("Groundspeak cookie received after post: " +
        // _groundspeakAuthCookie);

        // System.out.println("status POST= " + status);

        httpConnection.disconnect();

        // 03 - validate by getting the profile page
        URL profilepage = new URL(GEOCACHING_URL + "/account/settings/profile");
        httpConnection = (HttpURLConnection) profilepage.openConnection();
        httpConnection.setRequestMethod("GET");

        // header - cookie
        httpConnection.setRequestProperty("Cookie", _groundspeakAuthCookie);
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
        int status = httpConnection.getResponseCode();
        // pageContents = readHttpRequest(httpConnection).toString();
        // System.out.println("status GET= " + status);

        // PrintWriter pw = new PrintWriter("output.html", "UTF-8");
        // pw.write(ReadHttpRequest(httpConnection).toString());
        // pw.close();

        httpConnection.disconnect();

        return status == 200;
    }

    Boolean login() throws IOException{
        // Added my Mg
        // Login using the Authentication Cookie (or rather, check that this authentication cookie is valid)

        // Check that this object does have an Authetication Token
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

        return status == 200;
    }

    public Geocache getGeocacheDetails(String code) throws IOException, ParseException
    {
        code = code.toUpperCase();

        System.out.println("Getting cache " + code);
        Geocache gc = new Geocache();

        gc.code = code;

        // Obtain the HTML of the page, sending the authentication cookie
        URL geocachePage = new URL(GEOCACHING_URL + GEOCACHE_PAGE + code);
        HttpURLConnection httpConnection = (HttpURLConnection) geocachePage.openConnection();

        httpConnection.setRequestMethod("GET");
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);

        // header - cookie
        httpConnection.setRequestProperty("Cookie", _groundspeakAuthCookie);
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
        int status = httpConnection.getResponseCode();
        // System.out.println("Status GetGeocacheDetails GET= " + status);

        String pageContents = readHttpRequest(httpConnection).toString();

        // 1. Get cache name
        String regexNamePattern = "<span id=\"ctl00_ContentBody_CacheName\">(.+?)</span>";
        Pattern pattern = Pattern.compile(regexNamePattern);
        Matcher matcher = pattern.matcher(pageContents);

        if (matcher.find( )) {
            gc.name = matcher.group(1);
        } else {
            gc.latitude = "NO MATCH";
        }

        // 2. Get coordinates. eg: <span id="uxLatLon">N 48° 08.192 E 011° 33.158</span>
        String regexLatLongPattern = "<span id=\"uxLatLon\">([NS] [0-9]+° [0-9]+.[0-9]+) ([EW] [0-9]+° [0-9]+.[0-9]+)</span>";
        pattern = Pattern.compile(regexLatLongPattern);
        matcher = pattern.matcher(pageContents);

        if (matcher.find()) {
            gc.latitude = matcher.group(1);
            gc.longitude = matcher.group(2);
        } else {
            gc.latitude = "NO MATCH";
            gc.longitude = "NO MATCH";
        }

        // 3. Get Size
        String regexSize = "/images/icons/container/(.+?).gif";
        pattern = Pattern.compile(regexSize);
        matcher = pattern.matcher(pageContents);

        if (matcher.find()) {
            gc.size = matcher.group(1);
            gc.size = gc.size.substring(0,1).toUpperCase() + gc.size.substring(1, gc.size.length());
        } else {
            gc.size = "NO MATCH";
        }

        // 4. Get Difficulty and Terrain (both use the same regex, repeated instances)
        String regexDifficulty = "<img src=\"/images/stars/stars[_0-9]+.gif\" alt=\"(.*?) out of 5\" />";

        pattern = Pattern.compile(regexDifficulty);
        matcher = pattern.matcher(pageContents);

        if (matcher.find()) {
            gc.difficulty = matcher.group(1);

            if(matcher.find()) {
                gc.terrain = matcher.group(1);
            }
            else {
                gc.terrain = "NO MATCH";
            }
        } else {
            gc.difficulty = "NO MATCH";
            gc.terrain = "NO MATCH";
        }

        // 5. Get the cache type
        String regexType = "<a href=\"/about/cache_types.aspx\" target=\"_blank\" title=\"([a-zA-Z\\s\\(\\)]+).*?\" class=\"cacheImage\">";
        pattern = Pattern.compile(regexType);
        matcher = pattern.matcher(pageContents);

        if (matcher.find()) {
            gc.type = CacheTypeEnum.valueOfTypeString(matcher.group(1));
        } else {
            gc.type = CacheTypeEnum.Other;
        }

        // 6. Have I found it?
        String regexFound = "<strong id=\"ctl00_ContentBody_GeoNav_logText\">(Did Not Find|Found It!)</strong>";
        pattern = Pattern.compile(regexFound);
        matcher = pattern.matcher(pageContents);

        if (matcher.find()) {
            gc.foundIt = matcher.group(1).contains("Found It!") ? 2 : 1;
        } else {
            gc.foundIt = 0;
        }

        // 7. Hint. Note: \x28 is "("" and \x29 is ")"
        String regexHint = "<a id=\"ctl00_ContentBody_lnkDH\" onclick=\"dht\\(this\\);return&#32;false;\" title=\"Decrypt\" href=\"../seek/#\">Decrypt</a>\\) </p><div id=\"div_hint\" class=\"span-8 WrapFix\">\\s*(.*?)</div><div id='dk'";
        pattern = Pattern.compile(regexHint);
        matcher = pattern.matcher(pageContents);

        if (matcher.find()) {
            gc.hint = Rot13.Decode(matcher.group(1));
        } else {
            gc.hint = "NO MATCH";
        }

        // 8. Favourites
        String regexFavourites = "<span class=\"favorite-value\">\\s*(.*?)\\s*</span>";
        pattern = Pattern.compile(regexFavourites);
        matcher = pattern.matcher(pageContents);

        if (matcher.find()) {
            gc.favourites = Integer.parseInt(matcher.group(1));
        } else {
            gc.favourites = 0;
        }

        // 9. Last logs and their dates
        int logsParsed = 0, maxLogsToParse = 25;
        String regexLogType = "\"LogType\":\"([a-zA-Z ]+)\"";
        String regexLogDate = "\"Visited\":\"([a-zA-Z0-9\\.]+)\"";

        pattern = Pattern.compile(regexLogType);
        matcher = pattern.matcher(pageContents);

        Pattern patternDates = Pattern.compile(regexLogDate);
        Matcher matcherDates = patternDates.matcher(pageContents);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MMM.yyyy"); // log dates are in format "05.Dec.2019"
        while(matcher.find() && matcherDates.find() && logsParsed < maxLogsToParse)
        {
            GeocacheLog log = new GeocacheLog();
            log.logType = matcher.group(1);
            log.logDate = dateFormatter.parse(matcherDates.group(1));

            gc.recentLogs.add(log);
            logsParsed++;
        }
        // else do nothing -- the collection will be non-null but empty


        // PrintWriter pw = new PrintWriter(geocacheCode + ".html", "UTF-8");
        // pw.write(ReadHttpRequest(httpConnection).toString());
        // pw.close();

        // Refactor Login() as appropriate
        httpConnection.disconnect();
        return gc;
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
    private static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
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

    String getAuthenticationCookie(){
        return _groundspeakAuthCookie;
    }
}