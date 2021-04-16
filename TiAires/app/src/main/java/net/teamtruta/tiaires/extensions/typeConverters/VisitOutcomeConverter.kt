package net.teamtruta.tiaires.extensions.typeConverters

import androidx.room.TypeConverter
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum

class VisitOutcomeConverter{

    @TypeConverter
    fun toString(visitOutcome: VisitOutcomeEnum) : String{
        return visitOutcome.visitOutcomeString
    }

    @TypeConverter
    fun toGeoCacheTypeEnum(visitOutcomeString: String): VisitOutcomeEnum {
        return VisitOutcomeEnum.valueOfString(visitOutcomeString)
    }
}