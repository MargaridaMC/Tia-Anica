package app;

import java.util.Date;

/**
 * This class represents, in a very simplified way, a cache log. A string saying if it was a "Found it", "write note", "did not find", and the date.
 * It can be extended with more data as relevant for the use.
 */
public class GeocacheLog {
    public String logType;
    public Date logDate;
}