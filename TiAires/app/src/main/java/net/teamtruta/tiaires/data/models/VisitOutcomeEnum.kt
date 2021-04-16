package net.teamtruta.tiaires.data.models

enum class VisitOutcomeEnum(var visitOutcomeString: String) {
    NotAttempted(""),
    DNF("Didn't find it"),
    Found("Found it"),
    //Note("Write note"),
    Disabled("Temporarily Disable Listing");
    //NeedsMaintenance("Needs Maintenance");

    companion object {
        fun valueOfString(foundTypeString: String): VisitOutcomeEnum {
            for (e in values()) {
                if (e.visitOutcomeString == foundTypeString) return e
            }
            return NotAttempted
        }
    }

}