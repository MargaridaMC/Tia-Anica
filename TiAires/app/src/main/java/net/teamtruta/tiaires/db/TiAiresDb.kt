package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import net.teamtruta.tiaires.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant

class TiAiresDb (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val TAG = TiAiresDb::class.simpleName

    // Useful documentation on data types supported by SQLITE: https://www.sqlite.org/datatype3.html#:~:text=Date%20and%20Time%20Datatype,DD%20HH%3AMM%3ASS.
    // Docs on foreign keys: https://www.sqlite.org/foreignkeys.html

    // SQL queries to create all necessary tables
    // 1. Tour table
    private val SQL_CREATE_TOUR_TABLE = "CREATE TABLE ${TourEntry.TABLE_NAME}(" +
            "${TourEntry._ID} INTEGER PRIMARY KEY," +
            "${TourEntry.NAME_COL} TEXT," +
            "${TourEntry.CURRENT_TOUR_COL} INTEGER," +
            "${TourEntry.STARTING_POINT_LAT} REAL," +
            "${TourEntry.STARTING_POINT_LON} REAL" +
            ")"

    // 2. GeoCache table
    //private val SQL_CREATE_CACHE_TABLE = "CREATE TABLE cache(id INTEGER PRIMARY KEY,foundDate TEXT,needsMaintenance INTEGER,visit TEXT,notes TEXT,foundTrackable INTEGER,droppedTrackable INTEGER,favouritePoint INTEGER,orderBy INTEGER,tourID_FK INTEGER,cacheDetailID_FK INTEGER,FOREIGN KEY(tourID_FK) REFERENCES tour ON DELETE CASCADE,FOREIGN KEY(cacheDetailID_FK) REFERENCES cacheDetail ON DELETE CASCADE)"
    private val SQL_CREATE_CACHE_TABLE = "CREATE TABLE ${GeoCacheEntry.TABLE_NAME}(" +
            "${GeoCacheEntry._ID} INTEGER PRIMARY KEY," +
            "${GeoCacheEntry.VISIT_DATETIME_COL} TEXT," + // Can be changed to REAL or INTEGER according to convenience
            "${GeoCacheEntry.NEEDS_MAINTENANCE_COL} INTEGER," + // No Boolean in SQLITE
            "${GeoCacheEntry.VISIT_COL} TEXT," + // Matches an Enum
            "${GeoCacheEntry.NOTES_COL} TEXT," +
            "${GeoCacheEntry.FOUND_TRACKABLE_COL} TEXT," + // Previously: INTEGER
            "${GeoCacheEntry.DROPPED_TRACKABLE_COL} TEXT," + // Previously: INTEGER
            "${GeoCacheEntry.FAV_POINT_COL} INTEGER," +
            "${GeoCacheEntry.ORDER_COL} INTEGER," +
            "${GeoCacheEntry.IMAGE_COL} TEXT," +
            "${GeoCacheEntry.DRAFT_UPLOADED_COL} INTEGER NOT NULL DEFAULT 0, " +
            "${GeoCacheEntry.TOUR_ID_FK_COL} INTEGER," + // REFERENCES ${TourEntry.TABLE_NAME}(${TourEntry._ID}) ON DELETE CASCADE," +
            "${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL} INTEGER," + // REFERENCES ${CacheDetailEntry.TABLE_NAME}(${CacheDetailEntry._ID})" +
            "FOREIGN KEY(${GeoCacheEntry.TOUR_ID_FK_COL}) REFERENCES ${TourEntry.TABLE_NAME}," +
            "FOREIGN KEY(${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL}) REFERENCES ${GeoCacheDetailEntry.TABLE_NAME}," +
            "UNIQUE (${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL}, ${GeoCacheEntry.TOUR_ID_FK_COL})" +
            ")"



    // 3. GeoCache Detail table
    private val SQL_CREATE_CACHE_DETAIL_TABLE = "CREATE TABLE ${GeoCacheDetailEntry.TABLE_NAME}(" +
            "${GeoCacheDetailEntry._ID} INTEGER PRIMARY KEY," +
            "${GeoCacheDetailEntry.NAME_COL} TEXT," +
            "${GeoCacheDetailEntry.CODE_COL} TEXT UNIQUE," +
            "${GeoCacheDetailEntry.TYPE_COL} INTEGER," + // Matches an Enum
            "${GeoCacheDetailEntry.SIZE_COL} TEXT," +
            "${GeoCacheDetailEntry.TERRAIN_COL} REAL," + //TEXT," +
            "${GeoCacheDetailEntry.DIF_COL} REAL," + //TEXT," +
            "${GeoCacheDetailEntry.FIND_COL} TEXT," +
            "${GeoCacheDetailEntry.HINT_COL} TEXT," +
            "${GeoCacheDetailEntry.LAT_COL} REAL," + // ?
            "${GeoCacheDetailEntry.LON_COL} REAL," +
            "${GeoCacheDetailEntry.FAV_COL} INTEGER" +
            ")"

    // 4. Log Table
    private val SQL_CREATE_LOG_TABLE = "CREATE TABLE ${LogEntry.TABLE_NAME}(" +
            "${LogEntry._ID} INTEGER PRIMARY KEY," +
            "${LogEntry.LOG_DATE_COL} TEXT," +
            "${LogEntry.LOG_TYPE_COL} TEXT," +
            "${LogEntry.GEO_CACHE_DETAIL_ID_FK_COL} INTEGER REFERENCES ${GeoCacheDetailEntry.TABLE_NAME}" +
            ")"

    // 5. Attribute Table
    private  val SQL_CREATE_ATTRIBUTE_TABLE: String = "CREATE TABLE ${AttributeEntry.TABLE_NAME}(" +
            "${AttributeEntry._ID} INTEGER PRIMARY KEY," +
            "${AttributeEntry.ATTRIBUTE_TYPE} TEXT," +
            "${AttributeEntry.GEO_CACHE_DETAIL_ID_FK_COL} INTEGER REFERENCES ${GeoCacheDetailEntry.TABLE_NAME}" +
            ")"

    // Query to delete all tables
    private val SQL_DELETE_TABLES = "DROP TABLE IF EXISTS ${TourEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${GeoCacheEntry.TABLE_NAME};" +
            "DROP TABLE IF EXISTS ${GeoCacheDetailEntry.TABLE_NAME};" +
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
            val tempTableName = "${GeoCacheEntry.TABLE_NAME}OLD"
            db?.execSQL("ALTER TABLE ${GeoCacheEntry.TABLE_NAME} " +
                    "RENAME TO $tempTableName")

            // Create new one with desired constraints
            db?.execSQL(SQL_CREATE_CACHE_TABLE)

            // Transfer data from old one to the new one
            db?.execSQL("INSERT INTO ${GeoCacheEntry.TABLE_NAME} (" +
                            "${GeoCacheEntry._ID}, ${GeoCacheEntry.FOUND_DATE_COL}, " +
                            "${GeoCacheEntry.NEEDS_MAINTENANCE_COL}, ${GeoCacheEntry.VISIT_COL}, " +
                            "${GeoCacheEntry.NOTES_COL}, ${GeoCacheEntry.FOUND_TRACKABLE_COL}, " +
                            "${GeoCacheEntry.DROPPED_TRACKABLE_COL}, ${GeoCacheEntry.FAV_POINT_COL}, " +
                            "${GeoCacheEntry.ORDER_COL}, ${GeoCacheEntry.TOUR_ID_FK_COL}, " +
                            "${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL}) " +
                            "SELECT ${GeoCacheEntry._ID}, ${GeoCacheEntry.FOUND_DATE_COL}, " +
                            "${GeoCacheEntry.NEEDS_MAINTENANCE_COL}, ${GeoCacheEntry.VISIT_COL}, " +
                            "${GeoCacheEntry.NOTES_COL}, ${GeoCacheEntry.FOUND_TRACKABLE_COL}, " +
                            "${GeoCacheEntry.DROPPED_TRACKABLE_COL}, ${GeoCacheEntry.FAV_POINT_COL}, " +
                            "${GeoCacheEntry.ORDER_COL}, ${GeoCacheEntry.TOUR_ID_FK_COL}, " +
                            GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL +
                            " FROM (" +
                            " SELECT * FROM $tempTableName " +
                            "GROUP BY ${GeoCacheEntry.TOUR_ID_FK_COL}, ${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL})")

            // DELETE old table
            db?.execSQL("DROP TABLE IF EXISTS $tempTableName")
        }

        // Fill in order column if it is currently null or 0
        if(oldVersion <= 11){

            val cursor = db?.doQuery(GeoCacheEntry.TABLE_NAME, arrayOf(GeoCacheEntry._ID),
                    "${GeoCacheEntry.ORDER_COL} IS NULL OR ${GeoCacheEntry.ORDER_COL} = 0",
                    arrayOf())
                    ?: return

            while(cursor.moveToNext()){
                val id = cursor.getLong(GeoCacheEntry._ID)
                val values = ContentValues()
                values.put(GeoCacheEntry.ORDER_COL, id*1000)
                db.update(GeoCacheEntry.TABLE_NAME, values,
                        "${GeoCacheEntry._ID} = ?", arrayOf("$id"))
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
            val tempTableName = "${GeoCacheEntry.TABLE_NAME}OLD"
            db?.execSQL("ALTER TABLE ${GeoCacheEntry.TABLE_NAME} " +
                    "RENAME TO $tempTableName")

            // Create new table with appropriate structure
            db?.execSQL(SQL_CREATE_CACHE_TABLE)

            // Transfer data from old one to the new one
            db?.execSQL("INSERT INTO ${GeoCacheEntry.TABLE_NAME} (" +
                    "${GeoCacheEntry._ID}, ${GeoCacheEntry.FOUND_DATE_COL}, " +
                    "${GeoCacheEntry.NEEDS_MAINTENANCE_COL}, ${GeoCacheEntry.VISIT_COL}, " +
                    "${GeoCacheEntry.NOTES_COL}, ${GeoCacheEntry.FOUND_TRACKABLE_COL}, " +
                    "${GeoCacheEntry.DROPPED_TRACKABLE_COL}, ${GeoCacheEntry.FAV_POINT_COL}, " +
                    "${GeoCacheEntry.ORDER_COL}, ${GeoCacheEntry.TOUR_ID_FK_COL}, " +
                    "${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL}) " +
                    "SELECT ${GeoCacheEntry._ID}, ${GeoCacheEntry.FOUND_DATE_COL}, " +
                    "${GeoCacheEntry.NEEDS_MAINTENANCE_COL}, ${GeoCacheEntry.VISIT_COL}, " +
                    "${GeoCacheEntry.NOTES_COL}, NULL, NULL, ${GeoCacheEntry.FAV_POINT_COL}, " +
                    "${GeoCacheEntry.ORDER_COL}, ${GeoCacheEntry.TOUR_ID_FK_COL}, " +
                    GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL +
                    " FROM  $tempTableName ")

            // Delete original table
            db?.execSQL("DROP TABLE IF EXISTS $tempTableName")
        }

        // Add column for image path in cache table
        if(oldVersion <= 14){
            db?.execSQL("ALTER TABLE ${GeoCacheEntry.TABLE_NAME} " +
                    "ADD ${GeoCacheEntry.IMAGE_COL} TEXT")
        }

        if(oldVersion <= 15){
            // Create table for attributes
            db?.execSQL(SQL_CREATE_ATTRIBUTE_TABLE)
        }

        // Change type of difficulty and terrain columns to real
        if(oldVersion <= 16){
            // Rename old table to a different name
            val tempTableName = "${GeoCacheDetailEntry.TABLE_NAME}OLD"

            db?.execSQL("ALTER TABLE ${GeoCacheDetailEntry.TABLE_NAME} " +
                    "RENAME TO $tempTableName")

            // Create new table with appropriate structure
            db?.execSQL(SQL_CREATE_CACHE_DETAIL_TABLE)

            // Transfer data from old one to the new one
            db?.execSQL( "INSERT INTO ${GeoCacheDetailEntry.TABLE_NAME} (" +
                    "${GeoCacheDetailEntry._ID}, " +
                    "${GeoCacheDetailEntry.NAME_COL}, " +
                    "${GeoCacheDetailEntry.CODE_COL}, " +
                    "${GeoCacheDetailEntry.TYPE_COL}, " +
                    "${GeoCacheDetailEntry.SIZE_COL}, " +
                    "${GeoCacheDetailEntry.TERRAIN_COL}, " +
                    "${GeoCacheDetailEntry.DIF_COL}, " +
                    "${GeoCacheDetailEntry.FIND_COL}, " +
                    "${GeoCacheDetailEntry.HINT_COL}, " +
                    "${GeoCacheDetailEntry.LAT_COL}, " +
                    "${GeoCacheDetailEntry.LON_COL}, " +
                    GeoCacheDetailEntry.FAV_COL +
                    ") " +
                    "SELECT " +
                    "${GeoCacheDetailEntry._ID}, " +
                    "${GeoCacheDetailEntry.NAME_COL}, " +
                    "${GeoCacheDetailEntry.CODE_COL}, " +
                    "${GeoCacheDetailEntry.TYPE_COL}, " +
                    "${GeoCacheDetailEntry.SIZE_COL}, " +
                    "${GeoCacheDetailEntry.TERRAIN_COL}, " +
                    "${GeoCacheDetailEntry.DIF_COL}, " +
                    "${GeoCacheDetailEntry.FIND_COL}, " +
                    "${GeoCacheDetailEntry.HINT_COL}, " +
                    "${GeoCacheDetailEntry.LAT_COL}, " +
                    "${GeoCacheDetailEntry.LON_COL}, " +
                    GeoCacheDetailEntry.FAV_COL +
                    " FROM $tempTableName")

            // Delete original table
            db?.execSQL("DROP TABLE IF EXISTS $tempTableName")
        }

        // Add column for starting point in tour
        if(oldVersion <= 17){
            db?.execSQL("ALTER TABLE ${TourEntry.TABLE_NAME} " +
                    "ADD ${TourEntry.STARTING_POINT_LAT} REAL")
            db?.execSQL("ALTER TABLE ${TourEntry.TABLE_NAME} " +
                    "ADD ${TourEntry.STARTING_POINT_LON} REAL")
        }

        // Rename cache visit date column name
        if(oldVersion <= 18){
            // Apparently this is only possible for the most recent version of SQLite (and android is not up to date)
            // db?.execSQL("ALTER TABLE ${GeoCacheEntry.TABLE_NAME} " +
            //        "RENAME COLUMN ${GeoCacheEntry.FOUND_DATE_COL} TO ${GeoCacheEntry.VISIT_DATETIME_COL};")

            // Rename old table to a different name
            val tempTableName = "${GeoCacheEntry.TABLE_NAME}OLD"

            db?.execSQL("ALTER TABLE ${GeoCacheEntry.TABLE_NAME} " +
                    "RENAME TO $tempTableName")

            // Create new table with appropriate structure
            db?.execSQL(SQL_CREATE_CACHE_TABLE)

            // Transfer data from old one to the new one
            db?.execSQL( "INSERT INTO ${GeoCacheEntry.TABLE_NAME} (" +
                    "${GeoCacheEntry._ID}, " +
                    "${GeoCacheEntry.VISIT_DATETIME_COL}, " +
                    "${GeoCacheEntry.NEEDS_MAINTENANCE_COL}, " +
                    "${GeoCacheEntry.VISIT_COL}, " +
                    "${GeoCacheEntry.NOTES_COL}, " +
                    "${GeoCacheEntry.FOUND_TRACKABLE_COL}, " +
                    "${GeoCacheEntry.DROPPED_TRACKABLE_COL}, " +
                    "${GeoCacheEntry.FAV_POINT_COL}, " +
                    "${GeoCacheEntry.TOUR_ID_FK_COL}, " +
                    "${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL}, " +
                    "${GeoCacheEntry.ORDER_COL}, " +
                    GeoCacheEntry.IMAGE_COL +
                    ") " +
                    "SELECT " +
                    "${GeoCacheEntry._ID}, " +
                    "${GeoCacheEntry.FOUND_DATE_COL}, " +
                    "${GeoCacheEntry.NEEDS_MAINTENANCE_COL}, " +
                    "${GeoCacheEntry.VISIT_COL}, " +
                    "${GeoCacheEntry.NOTES_COL}, " +
                    "${GeoCacheEntry.FOUND_TRACKABLE_COL}, " +
                    "${GeoCacheEntry.DROPPED_TRACKABLE_COL}, " +
                    "${GeoCacheEntry.FAV_POINT_COL}, " +
                    "${GeoCacheEntry.TOUR_ID_FK_COL}, " +
                    "${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL}, " +
                    "${GeoCacheEntry.ORDER_COL}, " +
                    GeoCacheEntry.IMAGE_COL +
                    " FROM $tempTableName")

            // Delete original table
            db?.execSQL("DROP TABLE IF EXISTS $tempTableName")

            // Change the format of the date for the rows where it exists
            val selection = "${GeoCacheEntry.VISIT_DATETIME_COL} IS NOT NULL"
            val cursor = db?.doQuery(GeoCacheEntry.TABLE_NAME, arrayOf(GeoCacheEntry._ID,
                    GeoCacheEntry.VISIT_DATETIME_COL), selection)
            val datesNewFormat = mutableMapOf<Long, Instant>()
            if (cursor != null) {
                while(cursor.moveToNext()){
                    val visitDate = cursor.getString(GeoCacheEntry.VISIT_DATETIME_COL).toDate()
                    val visitInstant = visitDate.toInstant()
                    val id = cursor.getLong(GeoCacheEntry._ID)
                    datesNewFormat[id] = visitInstant
                }
            }

            for ((id, date) in datesNewFormat){
                val values = ContentValues()
                values.put(GeoCacheEntry.VISIT_DATETIME_COL,
                        date.toString())
                db?.update(GeoCacheEntry.TABLE_NAME,
                        values,
                        "${GeoCacheEntry._ID} = ?",
                        arrayOf("$id"))
            }
        }

        // Add new column for controlling whether draft has been uploaded
        if(oldVersion <= 19){
            db?.execSQL("ALTER TABLE ${GeoCacheEntry.TABLE_NAME} " +
                    "ADD ${GeoCacheEntry.DRAFT_UPLOADED_COL} INTEGER NOT NULL DEFAULT 0")
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