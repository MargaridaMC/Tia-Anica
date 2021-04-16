package net.teamtruta.tiaires.extensions.typeConverters

import androidx.room.TypeConverter
import net.teamtruta.tiaires.data.models.GeoCacheAttributeEnum

class AttributeTypeConverter {

    @TypeConverter
    fun toString(attribute: GeoCacheAttributeEnum) : String{
        return attribute.attributeString
    }

    @TypeConverter
    fun toAttributeTypeEnum(attributeString: String): GeoCacheAttributeEnum {
        return GeoCacheAttributeEnum.valueOfString(attributeString)
    }
}