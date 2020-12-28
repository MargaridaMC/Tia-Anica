package net.teamtruta.tiaires;

public enum VisitOutcomeEnum
{
    NotAttempted(""),
    DNF("Didn't find it"),
    Found("Found it"),
    Note("Write note"),
    Disabled("Temporarily Disable Listing"),
    NeedsMaintenance("Needs Maintenance");

    public String visitOutcomeString;

    public String getVisitOutcomeString(){
        return visitOutcomeString;
    }

    VisitOutcomeEnum(String visitOutcomeString){
        this.visitOutcomeString = visitOutcomeString;
    }

    public static VisitOutcomeEnum valueOfString(String foundTypeString){

        for(VisitOutcomeEnum e : values()){
            if(e.getVisitOutcomeString().equals(foundTypeString)) return e;
        }

        return NotAttempted;
    }
}