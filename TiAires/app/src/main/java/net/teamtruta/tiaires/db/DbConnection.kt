package net.teamtruta.tiaires.db

import android.content.Context

class DbConnection(context: Context) {
    val tourTable = TourDbTable(context)
    val geoCacheTable = GeoCacheDbTable(context)
    val geoCacheDetailTable = GeoCacheDetailDbTable(context)
    val logTable = LogDbTable(context)
}