package net.teamtruta.tiaires.extensions;

/**
 * Rot13 encode/decode
 */
public class Rot13 {
    public static String Decode(String encodedString) {

        if(encodedString == null)
        return null;
        
        char[] converted = encodedString.toCharArray();

        for (int i = 0; i < encodedString.length(); i++) {
            char c = encodedString.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'N' && c <= 'Z') c -= 13;
            
            converted[i] = c;
         }

         return new String(converted);
    }
    
}