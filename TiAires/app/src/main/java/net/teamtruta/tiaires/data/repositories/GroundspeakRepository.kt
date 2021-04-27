package net.teamtruta.tiaires.data.repositories

import android.app.Application
import android.util.Log
import com.microsoft.appcenter.analytics.Analytics
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCacheWithLogsAndAttributesAndWaypoints
import net.teamtruta.tiaires.extensions.Resource
import net.teamtruta.tiaires.extensions.Status
import net.teamtruta.tiaires.integration.GeocachingScrapper
import okhttp3.*
import java.io.File
import java.io.IOException
import java.util.HashMap

class GroundspeakRepository {

    private val tag: String = GroundspeakRepository::class.java.simpleName

    val USERNAME = "username"

    fun getGeoCacheFromCode(code: String): GeoCacheWithLogsAndAttributesAndWaypoints? {
        // val authCookie = dbRepository.getAuthenticationCookie()
        val authCookie = getAuthenticationCookie()
        val scrapper = GeocachingScrapper(authCookie)
        val login: Boolean = scrapper.login()
        if(!login)
            return null
        return scrapper.getGeoCacheDetails(code)
    }

    private fun getAuthenticationCookie() : String? {

        val context = App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Application.MODE_PRIVATE)
        return sharedPreferences.getString(context.getString(R.string.authentication_cookie_key), "")
    }

    fun login(username: String, password: String): Boolean{

        val gs = GeocachingScrapper(getAuthenticationCookie())

        try
        {
            // Login can be done either with username and password or with an authentication cookie
            val success = gs.login(username, password)

            val properties: MutableMap<String, String> = HashMap()
            properties["LoginType"] = "Username/Password"
            properties["Result"] = success.toString()
            Analytics.trackEvent("LoginTask.doInBackground", properties)

            // If successful
            setAuthenticationCookie(gs.authenticationCookie)
            setUsername(username)

            return true
        }catch (e: java.lang.Exception)
        {
            e.fillInStackTrace()
            Log.d(tag, "Something went wrong")
            return false
        }
    }

    private fun setAuthenticationCookie(authCookie: String){
        val context =  App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Application.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.authentication_cookie_key), authCookie)
        editor.apply()
    }

    fun getUsername(): String?{
        val context =  App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Application.MODE_PRIVATE)
        return sharedPreferences.getString(USERNAME, "")
    }

    fun setUsername(username: String){
        val context =  App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Application.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USERNAME, username)
        editor.apply()
    }

    fun login(): Boolean{

        val authenticationCookie = getAuthenticationCookie() ?: return false
        val gs = GeocachingScrapper(authenticationCookie)
        return gs.login()

    }

    fun uploadDrafts(draftFileAbsolutePath: String): Resource<Boolean> {
        val draftUploadURL = "https://www.geocaching.com/api/proxy/web/v1/LogDrafts/upload"

        val client = OkHttpClient().newBuilder()
                .build()

        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file-0", draftFileAbsolutePath,
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                File(draftFileAbsolutePath)))
                .build()

        val request = Request.Builder()
                .url(draftUploadURL)
                .method("POST", body)
                .addHeader("Cookie", getAuthenticationCookie())
                .build()


        return try {
            val response = client.newCall(request).execute()
            val requestResponse = response.body()?.string()
            if (requestResponse == null || requestResponse == "[]")
                Resource(Status.ERROR, null, "Geocaching did not find any new drafts to upload.")
            else {
                Resource(Status.SUCCESS, null, "Your drafts were successfully uploaded!")
            }
        } catch (e: IOException){
            e.printStackTrace()
            Resource(Status.ERROR, null, "There was an error uploading your drafts")
        }

    }


}