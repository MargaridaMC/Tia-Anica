package net.teamtruta.tiaires.db

import android.content.Context

class DbConnection(context: Context) {
    val tourTable = TourDbTable(context)
    val cacheTable = CacheDbTable(context)
    val cacheDetailTable = CacheDetailDbTable(context)
    val logTable = LogDbTable(context)
}