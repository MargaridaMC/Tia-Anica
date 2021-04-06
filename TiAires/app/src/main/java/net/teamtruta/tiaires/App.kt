package net.teamtruta.tiaires

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.teamtruta.tiaires.data.Repository
import net.teamtruta.tiaires.data.TiAiresDatabase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    val database by lazy { TiAiresDatabase.getDatabase(this) }
    val repository by lazy {Repository(database.geocachingTourDao(), database.geoCacheInTourDao(),
            database.geoCacheDao(), database.geoCacheLogDao(), database.geoCacheAttributeDao())}

    companion object {
        public var context: Context? = null

        public const val TOUR_ID_EXTRA: String = "tourID"
        public const val EDIT_EXTRA = "edit"
        public const val GEOCACHE_ID_EXTRA = "geoCacheID"

        val authenticationCookie: String?
            get() {
                val sharedPreferences = context!!.getSharedPreferences(context!!.getString(R.string.preference_file_key), MODE_PRIVATE)
                val authCookie = sharedPreferences.getString(context!!.getString(R.string.authentication_cookie_key), "")
                if (authCookie == "") {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Login information is missing. Please input your credentials in the login screen.")
                    builder.setPositiveButton(context!!.getString(R.string.ok)) { dialog: DialogInterface?, id: Int ->
                        val intent = Intent(context, LoginActivity::class.java)
                        context!!.startActivity(intent)
                    }
                    builder.setNegativeButton(context!!.getString(R.string.cancel)) { dialog: DialogInterface?, which: Int -> }
                    val dialog = builder.create()
                    dialog.show()
                }
                return authCookie
            }
    }
}