package net.teamtruta.tiaires.extensions.typeConverters

import androidx.room.TypeConverter
import net.teamtruta.tiaires.data.models.GeoCacheTypeEnum

class GeoCacheTypeConverter{

    @TypeConverter
    fun toString(geoCacheType: GeoCacheTypeEnum) : String{
        return geoCacheType.typeString
    }

    @TypeConverter
    fun toGeoCacheTypeEnum(typeString: String): GeoCacheTypeEnum {
        return GeoCacheTypeEnum.valueOfString(typeString)
    }
}