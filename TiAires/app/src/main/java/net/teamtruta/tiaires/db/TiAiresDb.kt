package net.teamtruta.tiaires.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TiAiresDb (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val TAG = TiAiresDb::class.simpleName

    // Useful documentation on data types supported by SQLITE: https://www.sqlite.org/datatype3.html#:~:text=Date%20and%20Time%20Datatype,DD%20HH%3AMM%3ASS.
    // Docs on foreign keys: https://www.sqlite.org/foreignkeys.html

    // SQL queries to create all necessary tables
    // 1. Tour table
    private val SQL_CREATE_TOUR_TABLE = "CREATE TABLE ${TourEntry.TABLE_NAME}(" +
            "${TourEntry._ID} INTEGER PRIMARY KEY," +
            "${TourEntry.NAME_COL} TEXT," +
            "${TourEntry.CURRENT_TOUR_COL} INTEGER" +
            ")"

    // 2. Cache table
    private val SQL_CREATE_CACHE_TABLE = "CREATE TABLE ${CacheEntry.TABLE_NAME}(" +
            "${CacheEntry._ID} INTEGER PRIMARY KEY," +
            "${CacheEntry.FOUND_DATE_COL} TEXT," + // Can be changed to REAL or INTEGER according to convenience
            "${CacheEntry.NEEDS_MAINTENANCE_COL} INTEGER," + // No Boolean in SQLITE
            "${CacheEntry.VISIT_COL} TEXT," + // Matches an Enum
            "${CacheEntry.NOTES_COL} TEXT," +
            "${CacheEntry.FOUND_TRACKABLE_COL} INTEGER," +
            "${CacheEntry.DROPPED_TRACKABLE_COL} INTEGER," +
            "${CacheEntry.FAV_POINT_COL} INTEGER," +
            "${CacheEntry.ORDER_COL} INTEGER," +
            "${CacheEntry.TOUR_ID_FK_COL} INTEGER," + // REFERENCES ${TourEntry.TABLE_NAME}(${TourEntry._ID}) ON DELETE CASCADE," +
            "${CacheEntry.CACHE_DETAIL_ID_FK_COL} INTEGER," + // REFERENCES ${CacheDetailEntry.TABLE_NAME}(${CacheDetailEntry._ID})" +
            "FOREIGN KEY(${CacheEntry.TOUR_ID_FK_COL}) REFERENCES ${TourEntry.TABLE_NAME} ON DELETE CASCADE," +
            "FOREIGN KEY(${CacheEntry.CACHE_DETAIL_ID_FK_COL}) REFERENCES ${CacheDetailEntry.TABLE_NAME} ON DELETE CASCADE," +
            "UNIQUE (${CacheEntry.CACHE_DETAIL_ID_FK_COL}, ${CacheEntry.TOUR_ID_FK_COL})" +
            ")"



    // 3. Cache Detail table
    private val SQL_CREATE_CACHE_DETAIL_TABLE = "CREATE TABLE ${CacheDetailEntry.TABLE_NAME}(" +
            "${CacheDetailEntry._ID} INTEGER PRIMARY KEY," +
            "${CacheDetailEntry.NAME_COL} TEXT," +
            "${CacheDetailEntry.CODE_COL} TEXT UNIQUE," +
            "${CacheDetailEntry.TYPE_COL} INTEGER," + // Matches an Enum
            "${CacheDetailEntry.SIZE_COL} TEXT," +
            "${CacheDetailEntry.TERRAIN_COL} TEXT," +
            "${CacheDetailEntry.DIF_COL} TEXT," +
            "${CacheDetailEntry.FIND_COL} TEXT," +
            "${CacheDetailEntry.HINT_COL} TEXT," +
            "${CacheDetailEntry.LAT_COL} REAL," + // ?
            "${CacheDetailEntry.LON_COL} REAL," +
            "${CacheDetailEntry.FAV_COL} INTEGER" +
            ")"

    // 4. Log Table
    private val SQL_CREATE_LOG_TABLE = "CREATE TABLE ${LogEntry.TABLE_NAME}(" +
            "${LogEntry._ID} INTEGER PRIMARY KEY," +
            "${LogEntry.LOG_DATE_COL} TEXT," +
            "${LogEntry.LOG_TYPE_COL} TEXT," +
            "${LogEntry.CACHE_DETAIL_ID_FK_COL} INTEGER REFERENCES ${CacheDetailEntry.TABLE_NAME}" +
            ")"

    // Query to delete all tables
    private val SQL_DELETE_TABLES = "DROP TABLE IF EXISTS ${TourEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${CacheEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${CacheDetailEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${LogEntry.TABLE_NAME};"



    override fun onCreate(db: SQLiteDatabase?) {
        // Create all the tables required

        Log.d(TAG, "Calling TiAiresDb onCreate")

        db?.execSQL(SQL_CREATE_TOUR_TABLE)
        db?.execSQL(SQL_CREATE_CACHE_DETAIL_TABLE)
        db?.execSQL(SQL_CREATE_CACHE_TABLE)
        db?.execSQL(SQL_CREATE_LOG_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Called TiAiresDb onUpgrade")
        db?.execSQL(SQL_DELETE_TABLES)
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        Log.d(TAG, "Calling TiAiresDb onOpen")
        super.onOpen(db)
        // Foreign Key support is not enabled by default.
        // This command enables it every time the database is opened
        db?.execSQL("PRAGMA foreign_keys=ON")
    }

}