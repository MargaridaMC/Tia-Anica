package net.teamtruta.tiaires.data.models

import androidx.room.*
import net.teamtruta.tiaires.extensions.typeConverters.AttributeTypeConverter

@Entity(tableName = "attribute",
        foreignKeys = [ForeignKey(entity = GeoCache::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("cacheDetailIDFK"),
                    onDelete = ForeignKey.CASCADE)])

data class GeoCacheAttribute(

        @PrimaryKey(autoGenerate = true)
        val id: Long,

        @TypeConverters(AttributeTypeConverter::class)
        val attributeType: GeoCacheAttributeEnum,

        @ColumnInfo(index = true)
        var cacheDetailIDFK: Long

) {
    constructor(attribute: GeoCacheAttributeEnum) : this(0, attribute, 0)
}
