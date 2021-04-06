package net.teamtruta.tiaires.extensions.typeConverters

import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {

    private val DATE_STRING_FORMAT = "dd.MMM.yyyy HH:mm"

    @TypeConverter
    fun toString(date: Date): String{
        val dateFormat: DateFormat = SimpleDateFormat(DATE_STRING_FORMAT, Locale.getDefault())
        return dateFormat.format(date)
    }

    @TypeConverter
    fun toDate(dateString: String) : Date{
        val dateFormat: DateFormat = SimpleDateFormat(DATE_STRING_FORMAT, Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }
}