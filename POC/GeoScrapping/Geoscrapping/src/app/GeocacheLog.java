package app;

import java.util.Date;

public class GeocacheLog {
    public FoundEnumType logType;
    public Date logDate;

    public FoundEnumType getLogType(){ return logType; }
    public void setLogType(FoundEnumType type){ logType = type; }

    public Date getLogDate(){ return logDate; }
    public void setLogDate(Date date){ logDate = date; }
}