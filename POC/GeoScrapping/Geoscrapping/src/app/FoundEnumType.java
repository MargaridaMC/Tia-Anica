package app;

enum FoundEnumType
{
        NotAttempted(""),
        DNF("Didn't find it"),
        Found("Found it"),
        Note("Write note"),
        Disabled("Temporarily Disable Listing"),
        NeedsMaintenance("Needs Maintenance");

        private String foundTypeString;

        public String getTypeString(){
            return foundTypeString;
        }

        FoundEnumType(String foundTypeString){
            this.foundTypeString = foundTypeString;
        }

        public static FoundEnumType valueOfString(String foundTypeString){

            for(FoundEnumType e : values()){
                if(e.getTypeString().equals(foundTypeString)) return e;
            } 

            return NotAttempted;
        }
}