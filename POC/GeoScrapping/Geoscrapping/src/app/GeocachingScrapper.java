package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * GeocachingScrapper
 * Useful site: https://www.baeldung.com/java-http-request
 */
public class GeocachingScrapper {

    private String _username, _password;

    private static final String GEOCACHING_URL = "https://www.geocaching.com";
    private static final String LOGIN_PAGE = "/account/signin";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3980.0 Safari/537.36 Edg/80.0.355.1";
    private String _requestVerificationCookie;
    private String _groundspeakAuthCookie;

    public GeocachingScrapper(String user, String password) {
        _username = user;
        _password = password;
    }

    /*
    * User-Agent is passed because otherwise Java SDK sends "Java something"
    * groundspeak token is sent as a response to the auth / follow redirects has to be disabled otherwise I can't capture that cookie
    */
    public Boolean Login() throws IOException
    {
        // 01. get the login page and extract the relevant information from it
        URL url = new URL(GEOCACHING_URL + LOGIN_PAGE);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

        httpConnection.setRequestMethod("GET");

        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
 
        int status = httpConnection.getResponseCode(); // this causes the request to be done

        StringBuffer pageContent = ReadHttpRequest(httpConnection);
        String tokenValue = GetTokenFromHtmlBody(pageContent);
        _requestVerificationCookie = httpConnection.getHeaderField("Set-Cookie").split(";")[0];

        System.out.println("status GET= " + status);
        System.out.println("Token in HTML = " + tokenValue);
        System.out.println("RV Cookie token = " + _requestVerificationCookie);

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
        parameters.put("UsernameOrEmail", _username);
        parameters.put("Password", _password);
        
        // write to body of message request
        httpConnection.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(httpConnection.getOutputStream());
        out.writeBytes(GetParamsString(parameters));
        out.flush();
        out.close();
        
        httpConnection.setInstanceFollowRedirects(false); // or else we're redirected to ReturnUrl and loose the gspkauth cookie
        status = httpConnection.getResponseCode();

        _groundspeakAuthCookie = httpConnection.getHeaderField("Set-Cookie");
        System.out.println("Groundspeak cookie received after post: " + _groundspeakAuthCookie);

        System.out.println("status POST= " + status);
        
        httpConnection.disconnect();
        
        // 03 - validate by getting the profile page
        URL profilepage = new URL(GEOCACHING_URL + "/account/settings/profile");
        httpConnection = (HttpURLConnection) profilepage.openConnection();
        httpConnection.setRequestMethod("GET");

        // header - cookie
        httpConnection.setRequestProperty("Cookie", _groundspeakAuthCookie);
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
        status = httpConnection.getResponseCode();
        System.out.println("status GET= " + status);

        // PrintWriter pw = new PrintWriter("output.html", "UTF-8");
        // pw.write(ReadHttpRequest(httpConnection).toString());
        // pw.close();

        return status == 200;
    }

    public Geocache GetGeocacheDetails(String geocacheCode)
    {
        // TO-DO
        // Refactor Login() as appropriate
        return new Geocache();
    }

    private String GetTokenFromHtmlBody(StringBuffer htmlPage)
    {
        // The relevant HTML snipet is:
        // <form action="/account/signin" id="SignupSignin" method="post"><input name="__RequestVerificationToken" type="hidden" value="3smnw46ATdX1c__TgiNxUAnPQ50MPeXaJet0DrUUEaww7ttLR_gIL3Z1G0dZimKQ3maWXTHNOPdOhNJP1cLDLNntWS01" /><input Length="5" id="ReturnUrl" name="ReturnUrl" type="hidden" value="/play" />            <div class="margin-center" style="max-width: 90%">

        String marker = "__RequestVerificationToken\" type=\"hidden\" value=\"";
        String exampleToken = "3smnw46ATdX1c__TgiNxUAnPQ50MPeXaJet0DrUUEaww7ttLR_gIL3Z1G0dZimKQ3maWXTHNOPdOhNJP1cLDLNntWS01";

        int whereIsItPosition = htmlPage.indexOf(marker);
        return htmlPage.substring(whereIsItPosition + marker.length(), whereIsItPosition + marker.length() + exampleToken.length());
    }

    // from: https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-networking-2/src/main/java/com/baeldung/httprequest/ParameterStringBuilder.java
    public static String GetParamsString(Map<String, String> params) throws UnsupportedEncodingException {
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
    
    private StringBuffer ReadHttpRequest(HttpURLConnection httpConnection) throws IOException
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
}