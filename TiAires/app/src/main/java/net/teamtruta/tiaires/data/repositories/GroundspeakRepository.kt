package net.teamtruta.tiaires.data.repositories

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.appcenter.analytics.Analytics
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCacheWithLogsAndAttributesAndWaypoints
import net.teamtruta.tiaires.extensions.Event
import net.teamtruta.tiaires.integration.GeocachingScrapper
import okhttp3.*
import java.io.File
import java.io.IOException
import java.lang.Exception
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
        return try {
            scrapper.getGeoCacheDetails(code)
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    private fun getAuthenticationCookie() : String? {

        val context = App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Application.MODE_PRIVATE)
        return sharedPreferences.getString(context.getString(R.string.authentication_cookie_key), null)
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

    fun login(): Boolean{

        val authenticationCookie = getAuthenticationCookie() ?: return false
        val gs = GeocachingScrapper(authenticationCookie)
        return gs.login()

    }

    fun logout(): Boolean {
        val context =  App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(USERNAME)
        editor.remove(context.getString(R.string.authentication_cookie_key))
        editor.apply()
        return true
    }

    fun uploadDrafts(draftFileAbsolutePath: String): Event<Any> {
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
                Event(false, "Geocaching did not find any new drafts to upload.")
            else {
                Event(true, "Your drafts were successfully uploaded!")
            }
        } catch (e: IOException){
            e.printStackTrace()
            Event(false, "There was an error uploading your drafts")
        }

    }


}