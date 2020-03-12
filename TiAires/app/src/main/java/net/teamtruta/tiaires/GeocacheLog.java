package net.teamtruta.tiaires;

import java.util.Date;

/**
 * This class represents, in a very simplified way, a cache log. A string saying if it was a "Found it", "write note", "did not find", and the date.
 * It can be extended with more data as relevant for the use.
 */
class GeocacheLog {

    FoundEnumType logType;
    Date logDate;

    public FoundEnumType getLogType(){ return logType; }
    public void setLogType(FoundEnumType type){ logType = type; }

    public Date getLogDate(){ return logDate; }
    public void setLogDate(Date date){ logDate = date; }
}