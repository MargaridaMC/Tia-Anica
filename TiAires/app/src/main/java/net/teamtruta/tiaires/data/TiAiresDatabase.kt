package net.teamtruta.tiaires.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.teamtruta.tiaires.data.daos.*
import net.teamtruta.tiaires.data.models.*
import net.teamtruta.tiaires.extensions.typeConverters.*


@Database(entities = [GeoCache::class,
                    GeoCacheInTour::class,
                    GeocachingTour::class,
                    GeoCacheLog::class,
                    GeoCacheAttribute::class,
                     Waypoint::class], version = 24, exportSchema = true)
@TypeConverters(CoordinateConverter::class, GeoCacheTypeConverter::class,
        AttributeTypeConverter::class, DateConverter::class, InstantConverter::class,
        VisitOutcomeConverter::class)
abstract class TiAiresDatabase: RoomDatabase() {

    abstract fun geoCacheDao(): GeoCacheDao
    abstract fun geoCacheInTourDao(): GeoCacheInTourDao
    abstract fun geocachingTourDao(): GeocachingTourDao
    abstract fun geoCacheLogDao(): GeoCacheLogDao
    abstract fun geoCacheAttributeDao(): GeoCacheAttributeDao
    abstract fun waypointDao(): WaypointDao

    companion object{

        private val MIGRATION_20_21: Migration = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Reformat tables such that they match the new verion

                // 1. GeoCacheDetail - rename columns to match new names, make columns not null
                // 1.1 Rename existing table to a new name
                var tempTableName = "cacheDetailOLD"
                database.execSQL("ALTER TABLE cacheDetail " +
                        "RENAME TO $tempTableName")

                // 1.2 Create new table with the right name and schema
                var createNewTableCmd = "" +
                        "CREATE TABLE cacheDetail(" +
                        "id INTEGER PRIMARY KEY NOT NULL," +
                        "name TEXT NOT NULL," +
                        "code TEXT UNIQUE NOT NULL," +
                        "type TEXT NOT NULL," +
                        "size TEXT NOT NULL," +
                        "terrain REAL NOT NULL," +
                        "difficulty REAL NOT NULL," +
                        "previousVisitOutcome TEXT NOT NULL," +
                        "hint TEXT NOT NULL," +
                        "latitude REAL NOT NULL," +
                        "longitude REAL NOT NULL," +
                        "favourites INTEGER NOT NULL" +
                        ")"
                database.execSQL(createNewTableCmd)

                // 1.3 Copy all data from the old table to the new one
                var transferDataCmd = "INSERT INTO cacheDetail(id, name, code, type, size, " +
                        "terrain, difficulty, previousVisitOutcome, hint, " +
                        "latitude, longitude, favourites) " +
                        "SELECT id, name, code, type, size, terrain, difficulty, " +
                        "foundIt, hint, latitude, longitude, favourites " +
                        "FROM cacheDetailOLD"
                database.execSQL(transferDataCmd)

                // 1.4 Delete old version of table -- might not work due to the existing foreign keys
                database.execSQL("DROP TABLE IF EXISTS $tempTableName")

                // 2. Tour - rename columns to match new names, make columns not null
                // 2.1 Rename existing table to a new name
                tempTableName = "tourOLD"
                database.execSQL("ALTER TABLE tour RENAME TO $tempTableName")

                // 2.2 Create new table with the right name and schema
                createNewTableCmd = "CREATE TABLE tour (" +
                        "id INTEGER PRIMARY KEY NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "isCurrentTour INTEGER NOT NULL, " +
                        "startingPointLatitude REAL, " +
                        "startingPointLongitude REAL " +
                        ")"
                database.execSQL(createNewTableCmd)

                // 2.3 Transfer data from old table to new
                transferDataCmd = "INSERT INTO tour(id, name, isCurrentTour, " +
                        "startingPointLatitude, startingPointLongitude) " +
                        "SELECT id, name, isCurrentTour, startingPointLatitude, " +
                        "startingPointLongitude FROM $tempTableName"
                database.execSQL(transferDataCmd)

                // 2.4 Delete old version of table -- might not work due to the existing foreign keys
                database.execSQL("DROP TABLE IF EXISTS $tempTableName")

                // 3. GeoCache - rename columns to match new names, make columns not null,
                // create indices for the foreign key columns and point foreign keys to the actual columns
                // 3.1 Rename existing table to a new name
                tempTableName = "cacheOLD"
                database.execSQL("ALTER TABLE cache RENAME TO $tempTableName")

                // 3.2 Create new table with the right name and schema
                createNewTableCmd = "CREATE TABLE cache ( " +
                        "id INTEGER PRIMARY KEY NOT NULL, " +
                        "currentVisitDatetime TEXT, " +
                        "needsMaintenance INTEGER NOT NULL, " +
                        "currentVisitOutcome TEXT NOT NULL, "+
                        "notes TEXT NOT NULL, " +
                        "foundTrackable TEXT, " +
                        "droppedTrackable TEXT, " +
                        "favouritePoint INTEGER NOT NULL, " +
                        "orderIdx INTEGER NOT NULL, " +
                        "pathToImage TEXT, " +
                        "draftUploaded INTEGER NOT NULL DEFAULT 0, " +
                        "tourIDFK INTEGER NOT NULL, " +
                        "geoCacheDetailIDFK INTEGER NOT NULL, " +
                        "FOREIGN KEY(tourIDFK) REFERENCES tour(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(geoCacheDetailIDFK) REFERENCES cacheDetail(id) ON DELETE CASCADE, " +
                        "UNIQUE (geoCacheDetailIDFK, tourIDFK) " +
                        ")"
                database.execSQL(createNewTableCmd)

                 // 3.3 Create indices on the two foreign keys
                database.execSQL("CREATE INDEX index_cache_tourIDFK ON cache(tourIDFK);")
                database.execSQL("CREATE INDEX index_cache_geoCacheDetailIDFK ON cache(geoCacheDetailIDFK);")

                // 3.4 Insert data from old to new table
                transferDataCmd = "INSERT INTO cache(id, currentVisitDatetime, needsMaintenance, " +
                        "currentVisitOutcome, notes, foundTrackable, droppedTrackable, " +
                        "favouritePoint, orderIdx, pathToImage, draftUploaded, tourIDFK, geoCacheDetailIDFK) " +
                        "SELECT id, visitDatetime, needsMaintenance, visit, notes, foundTrackable, " +
                        "droppedTrackable, favouritePoint, orderBy, image, " +
                        "draftUploaded, tourID_FK, cacheDetailID_FK FROM $tempTableName"
                database.execSQL(transferDataCmd)

                // 3.5 Delete old table
                database.execSQL("DROP TABLE IF EXISTS $tempTableName")

                // 4. Logs Table
                // 4.1 Rename existing table to a new name
                tempTableName = "logOLD"
                database.execSQL("ALTER TABLE log RENAME TO $tempTableName")

                // 4.2 Create new table with the right name and schema
                createNewTableCmd = "CREATE TABLE log( " +
                        "id INTEGER PRIMARY KEY NOT NULL, " +
                        "logDate TEXT NOT NULL, " +
                        "logType TEXT NOT NULL, " +
                        "cacheDetailIDFK INTEGER NOT NULL REFERENCES cacheDetail (id) ON DELETE CASCADE )"
                database.execSQL(createNewTableCmd)

                // 4.3 Create index for foreign key
                database.execSQL("CREATE INDEX index_log_cacheDetailIDFK ON log(cacheDetailIDFK)")

                // 4.4 Transfer data from old table to new
                transferDataCmd = "INSERT INTO log(id, logDate, logType, cacheDetailIDFK) " +
                        "SELECT id, date, type, cacheDetailID_FK FROM $tempTableName"
                database.execSQL(transferDataCmd)

                // 4.5 Delete old version of table -- might not work due to the existing foreign keys
                database.execSQL("DROP TABLE IF EXISTS $tempTableName")

                // 5. Attributes Table
                // 5.1 Rename existing table to a new name
                tempTableName = "attributeOLD"
                database.execSQL("ALTER TABLE attribute RENAME TO $tempTableName")

                // 5.2 Create new table with the right name and schema
                createNewTableCmd = "CREATE TABLE attribute( " +
                        "id INTEGER PRIMARY KEY NOT NULL, " +
                        "attributeType TEXT NOT NULL, " +
                        "cacheDetailIDFK INTEGER NOT NULL REFERENCES cacheDetail(id) ON DELETE CASCADE)"
                database.execSQL(createNewTableCmd)

                // 5.3 Create index for foreign key
                database.execSQL("CREATE INDEX index_attribute_cacheDetailIDFK" +
                        " ON attribute(cacheDetailIDFK)")

                // 5.4 Transfer data from old table to new
                transferDataCmd = "INSERT INTO attribute(id, attributeType, " +
                        "cacheDetailIDFK) SELECT id, type, cacheDetailID_FK FROM $tempTableName"
                database.execSQL(transferDataCmd)

                // 5.5 Delete old version of table -- might not work due to the existing foreign keys
                database.execSQL("DROP TABLE IF EXISTS $tempTableName")

            }
        }

        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE Waypoint (" +
                        "id INTEGER PRIMARY KEY NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "latitude REAL NOT NULL, " +
                        "longitude REAL NOT NULL, " +
                        "cacheDetailIDFK INTEGER NOT NULL REFERENCES cacheDetail(id) ON DELETE CASCADE)")

                database.execSQL("CREATE INDEX index_waypoint_cacheDetailIDFK ON Waypoint(cacheDetailIDFK);")
            }
        }

        private val MIGRATION_22_23 = object : Migration(22, 23){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Waypoint ADD COLUMN isDone INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE Waypoint ADD COLUMN isParking INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_23_24 = object : Migration(23, 24){
            // Add notes column to waypoint table and make latitude and longitude nullable
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Waypoint ADD COLUMN notes TEXT NOT NULL DEFAULT \"\"")
                database.execSQL("CREATE TABLE WaypointNew (" +
                        "id INTEGER PRIMARY KEY NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "latitude REAL NULL, " +
                        "longitude REAL NULL, " +
                        "isDone INTEGER NOT NULL DEFAULT 0, " +
                        "isParking INTEGER NOT NULL DEFAULT 0, " +
                        "notes TEXT NOT NULL DEFAULT \"\", " +
                        "cacheDetailIDFK INTEGER NOT NULL REFERENCES cacheDetail(id) ON DELETE CASCADE)")
                database.execSQL("INSERT INTO WaypointNew SELECT * FROM Waypoint")
                database.execSQL("DROP TABLE Waypoint")
                database.execSQL("ALTER TABLE WaypointNew RENAME TO Waypoint")
                database.execSQL("CREATE INDEX index_waypoint_cacheDetailIDFK ON Waypoint(cacheDetailIDFK);")
            }
        }

        @Volatile
        private var INSTANCE: TiAiresDatabase? = null

        fun getDatabase(context: Context): TiAiresDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        TiAiresDatabase::class.java,
                        "tiaires.db"
                ).addMigrations(
                        MIGRATION_20_21,
                        MIGRATION_21_22,
                        MIGRATION_22_23,
                        MIGRATION_23_24
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }



}

