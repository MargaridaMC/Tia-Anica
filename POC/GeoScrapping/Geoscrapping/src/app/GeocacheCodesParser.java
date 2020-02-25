package app;

import java.util.List;

/**
 * GeocachingTourParser
 */
public class GeocacheCodesParser {

    private GeocachingScrapper _gs;

    public GeocacheCodesParser(GeocachingScrapper gs) throws Exception {
        if(_gs == null)
        {
            throw new Exception("GeocachingScrapper must be non-null");
        }
        
        _gs = gs;

        _gs.login(); // TODO: se tentar fazer login mais de uma vez... dizer qq coisa? ou permitir? ou ter outro método de login com force
    }

    public List<Geocache> getCaches(String textWithCodes)
    {
        if(textWithCodes == null)
        return null;

        textWithCodes = textWithCodes.toUpperCase();

        // para fazer parse posso usar uma regex
        // TODO - code the parser here

        return null;
    }


}