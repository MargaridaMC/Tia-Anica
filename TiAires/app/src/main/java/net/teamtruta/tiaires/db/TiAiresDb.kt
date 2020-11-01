package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import net.teamtruta.tiaires.*
import java.text.DateFormat
import java.text.SimpleDateFormat

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
    //private val SQL_CREATE_CACHE_TABLE = "CREATE TABLE cache(id INTEGER PRIMARY KEY,foundDate TEXT,needsMaintenance INTEGER,visit TEXT,notes TEXT,foundTrackable INTEGER,droppedTrackable INTEGER,favouritePoint INTEGER,orderBy INTEGER,tourID_FK INTEGER,cacheDetailID_FK INTEGER,FOREIGN KEY(tourID_FK) REFERENCES tour ON DELETE CASCADE,FOREIGN KEY(cacheDetailID_FK) REFERENCES cacheDetail ON DELETE CASCADE)"
    private val SQL_CREATE_CACHE_TABLE = "CREATE TABLE ${CacheEntry.TABLE_NAME}(" +
            "${CacheEntry._ID} INTEGER PRIMARY KEY," +
            "${CacheEntry.FOUND_DATE_COL} TEXT," + // Can be changed to REAL or INTEGER according to convenience
            "${CacheEntry.NEEDS_MAINTENANCE_COL} INTEGER," + // No Boolean in SQLITE
            "${CacheEntry.VISIT_COL} TEXT," + // Matches an Enum
            "${CacheEntry.NOTES_COL} TEXT," +
            "${CacheEntry.FOUND_TRACKABLE_COL} TEXT," + // Previously: INTEGER
            "${CacheEntry.DROPPED_TRACKABLE_COL} TEXT," + // Previously: INTEGER
            "${CacheEntry.FAV_POINT_COL} INTEGER," +
            "${CacheEntry.ORDER_COL} INTEGER," +
            "${CacheEntry.IMAGE_COL} TEXT," +
            "${CacheEntry.TOUR_ID_FK_COL} INTEGER," + // REFERENCES ${TourEntry.TABLE_NAME}(${TourEntry._ID}) ON DELETE CASCADE," +
            "${CacheEntry.CACHE_DETAIL_ID_FK_COL} INTEGER," + // REFERENCES ${CacheDetailEntry.TABLE_NAME}(${CacheDetailEntry._ID})" +
            "FOREIGN KEY(${CacheEntry.TOUR_ID_FK_COL}) REFERENCES ${TourEntry.TABLE_NAME}," +
            "FOREIGN KEY(${CacheEntry.CACHE_DETAIL_ID_FK_COL}) REFERENCES ${CacheDetailEntry.TABLE_NAME}," +
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

    // 5. Attribute Table
    private  val SQL_CREATE_ATTRIBUTE_TABLE: String = "CREATE TABLE ${AttributeEntry.TABLE_NAME}(" +
            "${AttributeEntry._ID} INTEGER PRIMARY KEY," +
            "${AttributeEntry.ATTRIBUTE_TYPE} TEXT," +
            "${AttributeEntry.CACHE_DETAIL_ID_FK_COL} INTEGER REFERENCES ${CacheDetailEntry.TABLE_NAME}" +
            ")"

    // Query to delete all tables
    private val SQL_DELETE_TABLES = "DROP TABLE IF EXISTS ${TourEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${CacheEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${CacheDetailEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${LogEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${AttributeEntry.TABLE_NAME};"

    override fun onCreate(db: SQLiteDatabase?) {
        // Create all the tables required

        Log.d(TAG, "Calling TiAiresDb onCreate")

        db?.execSQL(SQL_CREATE_TOUR_TABLE)
        db?.execSQL(SQL_CREATE_CACHE_DETAIL_TABLE)
        db?.execSQL(SQL_CREATE_CACHE_TABLE)
        db?.execSQL(SQL_CREATE_LOG_TABLE)
        db?.execSQL(SQL_CREATE_ATTRIBUTE_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Called TiAiresDb onUpgrade")

        if(oldVersion <= 10){
            // What needs to be done:
            // - Remove on delete cascade from tour_id_fk and cache_id_fk
            // - Create constraint for UNIQUE(tour_id_fk, cache_id_fk) and erase duplicates
            // To do that in SQLite we need to create a new table with the constraints we want
            // and transfer the data there.

            // Rename old table
            val tempTableName = "${CacheEntry.TABLE_NAME}OLD"
            val SQL_RENAME_TABLE = "ALTER TABLE ${CacheEntry.TABLE_NAME} " +
                    "RENAME TO $tempTableName"
            db?.execSQL(SQL_RENAME_TABLE)

            // Create new one with desired constraints
            db?.execSQL(SQL_CREATE_CACHE_TABLE)

            // Transfer data from old one to the new one
            val SQL_TRANSFER_DATA = "INSERT INTO ${CacheEntry.TABLE_NAME} (" +
                    "${CacheEntry._ID}, ${CacheEntry.FOUND_DATE_COL}, " +
                    "${CacheEntry.NEEDS_MAINTENANCE_COL}, ${CacheEntry.VISIT_COL}, " +
                    "${CacheEntry.NOTES_COL}, ${CacheEntry.FOUND_TRACKABLE_COL}, " +
                    "${CacheEntry.DROPPED_TRACKABLE_COL}, ${CacheEntry.FAV_POINT_COL}, " +
                    "${CacheEntry.ORDER_COL}, ${CacheEntry.TOUR_ID_FK_COL}, " +
                    "${CacheEntry.CACHE_DETAIL_ID_FK_COL})" +
                    "SELECT ${CacheEntry._ID}, ${CacheEntry.FOUND_DATE_COL}, " +
                    "${CacheEntry.NEEDS_MAINTENANCE_COL}, ${CacheEntry.VISIT_COL}, " +
                    "${CacheEntry.NOTES_COL}, ${CacheEntry.FOUND_TRACKABLE_COL}, " +
                    "${CacheEntry.DROPPED_TRACKABLE_COL}, ${CacheEntry.FAV_POINT_COL}, " +
                    "${CacheEntry.ORDER_COL}, ${CacheEntry.TOUR_ID_FK_COL}, " +
                    CacheEntry.CACHE_DETAIL_ID_FK_COL +
                    " FROM (" +
                    " SELECT * FROM $tempTableName " +
                    "GROUP BY ${CacheEntry.TOUR_ID_FK_COL}, ${CacheEntry.CACHE_DETAIL_ID_FK_COL})"
            db?.execSQL(SQL_TRANSFER_DATA)

            // DELETE old table
            db?.execSQL("DROP TABLE IF EXISTS $tempTableName")
        }

        // Fill in order column if it is currently null or 0
        if(oldVersion <= 11){

            val cursor = db?.doQuery(CacheEntry.TABLE_NAME, arrayOf(CacheEntry._ID),
                    "${CacheEntry.ORDER_COL} IS NULL OR ${CacheEntry.ORDER_COL} = 0",
                    arrayOf())
                    ?: return

            while(cursor.moveToNext()){
                val id = cursor.getLong(CacheEntry._ID)
                val values = ContentValues()
                values.put(CacheEntry.ORDER_COL, id*1000)
                db.update(CacheEntry.TABLE_NAME, values,
                        "${CacheEntry._ID} = ?", arrayOf("$id"))
            }

            cursor.close()
        }

        // Update date format in Log Table
        if(oldVersion <= 12){
            val cursor = db?.doQuery(LogEntry.TABLE_NAME,
                    arrayOf(LogEntry._ID, LogEntry.LOG_DATE_COL))

            val dateFormat: DateFormat = SimpleDateFormat("dd.MMM.yyyy")
            while (cursor!= null && cursor.moveToNext()){
                val id = cursor.getLong(LogEntry._ID)
                val dateString = cursor.getString(LogEntry.LOG_DATE_COL)
                val date = dateFormat.parse(dateString)
                val values = ContentValues()
                values.put(LogEntry.LOG_DATE_COL, date.toFormattedString())
                db.update(LogEntry.TABLE_NAME, values,
                        "${LogEntry._ID} = ?", arrayOf("$id"))
            }

            cursor?.close()
        }

        // Change type  of columns FoundTrackable and Dropped Trackable to TEXT
        // so we can save the trackable codes
        if(oldVersion <= 13){

            // Rename old table to a different name
            val tempTableName = "${CacheEntry.TABLE_NAME}OLD"
            val SQL_RENAME_TABLE = "ALTER TABLE ${CacheEntry.TABLE_NAME} " +
                    "RENAME TO $tempTableName"
            db?.execSQL(SQL_RENAME_TABLE)

            // Create new table with appropriate structure
            db?.execSQL(SQL_CREATE_CACHE_TABLE)

            // Transfer data from old one to the new one
            val SQL_TRANSFER_DATA = "INSERT INTO ${CacheEntry.TABLE_NAME} (" +
                    "${CacheEntry._ID}, ${CacheEntry.FOUND_DATE_COL}, " +
                    "${CacheEntry.NEEDS_MAINTENANCE_COL}, ${CacheEntry.VISIT_COL}, " +
                    "${CacheEntry.NOTES_COL}, ${CacheEntry.FOUND_TRACKABLE_COL}, " +
                    "${CacheEntry.DROPPED_TRACKABLE_COL}, ${CacheEntry.FAV_POINT_COL}, " +
                    "${CacheEntry.ORDER_COL}, ${CacheEntry.TOUR_ID_FK_COL}, " +
                    "${CacheEntry.CACHE_DETAIL_ID_FK_COL})" +
                    "SELECT ${CacheEntry._ID}, ${CacheEntry.FOUND_DATE_COL}, " +
                    "${CacheEntry.NEEDS_MAINTENANCE_COL}, ${CacheEntry.VISIT_COL}, " +
                    "${CacheEntry.NOTES_COL}, NULL, NULL, ${CacheEntry.FAV_POINT_COL}, " +
                    "${CacheEntry.ORDER_COL}, ${CacheEntry.TOUR_ID_FK_COL}, " +
                    CacheEntry.CACHE_DETAIL_ID_FK_COL +
                    " FROM  $tempTableName "
            db?.execSQL(SQL_TRANSFER_DATA)

            // Delete original table
            db?.execSQL("DROP TABLE IF EXISTS $tempTableName")
        }

        // Add column for image path in cache table
        if(oldVersion <= 14){
            val SQL_ADD_COLUMN = "ALTER TABLE ${CacheEntry.TABLE_NAME} " +
                    "ADD ${CacheEntry.IMAGE_COL} TEXT"
            db?.execSQL(SQL_ADD_COLUMN)
        }

        if(oldVersion <= 15){
            // Create table for attributes
            db?.execSQL(SQL_CREATE_ATTRIBUTE_TABLE)
        }
    }

    override fun onOpen(db: SQLiteDatabase?) {
        Log.d(TAG, "Calling TiAiresDb onOpen")
        super.onOpen(db)
        // Foreign Key support is not enabled by default.
        // This command enables it every time the database is opened
        db?.execSQL("PRAGMA foreign_keys=ON")

    }

}