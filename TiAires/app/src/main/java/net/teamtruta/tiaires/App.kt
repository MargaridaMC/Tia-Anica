package net.teamtruta.tiaires

import android.app.Application
import android.content.Context
import net.teamtruta.tiaires.data.TiAiresDatabase
import net.teamtruta.tiaires.data.repositories.GroundspeakRepository
import net.teamtruta.tiaires.data.repositories.Repository


class App : Application() {

    val database by lazy { TiAiresDatabase.getDatabase(this) }
    val repository by lazy {
        Repository(database.geocachingTourDao(),
                database.geoCacheInTourDao(),
                database.geoCacheDao(),
                database.geoCacheLogDao(),
                database.geoCacheAttributeDao(),
                database.waypointDao())
    }
    val groundspeakRepository by lazy { GroundspeakRepository() }

    init {
        instance = this
    }

    companion object {
        private var instance: App? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        const val TOUR_ID_EXTRA: String = "tourID"
        const val EDIT_EXTRA = "edit"
        const val GEOCACHE_IN_TOUR_ID_EXTRA = "geoCacheID"
    }

    override fun onCreate() {
        super.onCreate()
        // initialize for any

        // Use ApplicationContext.
        // example: SharedPreferences etc...
        val context: Context = App.applicationContext()
    }


}
