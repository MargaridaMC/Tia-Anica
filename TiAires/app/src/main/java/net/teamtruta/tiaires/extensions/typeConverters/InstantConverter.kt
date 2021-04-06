package net.teamtruta.tiaires.extensions.typeConverters

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverter {

    @TypeConverter
    fun toString(datetime: Instant?): String?{
        return datetime?.toString()
    }

    @TypeConverter
    fun toDateTime(datetimeString: String?): Instant?{
        return if(datetimeString==null) null else Instant.parse(datetimeString)
    }
}