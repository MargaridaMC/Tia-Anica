package net.teamtruta.tiaires

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun SQLiteDatabase.doQuery(tableName: String, columns : Array<String>, selection : String? = null,
                                   selectionArgs : Array<String>? = null, groupBy : String? = null,
                                   having : String? = null, orderBy : String? = null) : Cursor {
    return query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy)
}

fun Cursor.getString(columnName : String) = getString(getColumnIndex(columnName))
fun Cursor.getInt(columnName : String) = getInt(getColumnIndex(columnName))
fun Cursor.getDouble(columnName : String) = getDouble(getColumnIndex(columnName))
fun Cursor.getLong(columnName : String) = getLong(getColumnIndex(columnName))
fun Cursor.getBoolean(columnName : String) = getInt(this.getColumnIndex(columnName)) == 1

inline fun <T> SQLiteDatabase.transaction(function: SQLiteDatabase.() -> T) : T{

    /*
    When to use transactions:
    When you are not using explicit transactions, SQLite will automatically wrap a transaction around every statement.
    When you write to the database, every insert/update/delete call is a single statement. If you are doing multiple such operations, you use transactions to avoid paying the transaction overhead for each of them.
    A query (query or rawQuery) is a single statement, even if it returns multiple rows. Therefore, using a transaction around a single query does not make any difference.
     */

    // All the following methods are applied on a SQLite object
    // including the inputted function because it is in itself an extension on the SQLite database
    beginTransaction()
    val result = try{
        // Adding an input to the function means there wil never be the problem of applying this transaction to the wrong database
        val returnValue = function()
        setTransactionSuccessful()
        returnValue
    } finally {
        endTransaction()
    }
    close()
    return result
}

//fun Boolean.toInt() = if (this) 1 else 0

val DATE_STRING_FORMAT = "dd.MMM.yyyy HH:mm"

fun Date.toFormattedString() : String{
    val dateFormat: DateFormat = SimpleDateFormat(DATE_STRING_FORMAT)
    return dateFormat.format(this)
}

fun String.toDate() : Date{
    //val format = "E MMM dd hh:mm:ss z yyyy"
    val dateFormat: DateFormat = SimpleDateFormat(DATE_STRING_FORMAT)
    return dateFormat.parse(this)
}