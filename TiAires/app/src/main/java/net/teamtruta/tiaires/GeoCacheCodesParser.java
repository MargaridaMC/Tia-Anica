package net.teamtruta.tiaires;

import java.util.List;

//import org.omg.CORBA.portable.ApplicationException;

/**
 * GeocachingTourParser
 */
public class GeoCacheCodesParser
{

    private GeocachingScrapper _gs;

    public GeoCacheCodesParser(GeocachingScrapper gs) throws Exception {
        if(_gs == null)
        {
            throw new Exception("GeocachingScrapper must be non-null");
        }
        
        _gs = gs;

        _gs.login(); // TODO: se tentar fazer login mais de uma vez... dizer qq coisa? ou permitir? ou ter outro método de login com force
    }

    public List<GeoCache> getCaches(String textWithCodes)
    {
        if(textWithCodes == null)
        return null;

        textWithCodes = textWithCodes.toUpperCase();

        // para fazer parse posso usar uma regex
        // TODO - code the parser here

        return null;
    }


}