import java.util.Arrays;

public class VigenereCipher {
	
	char[] allowedCharacters = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	public char[] encode(String msg, String key) {

		msg = msg.toUpperCase();
		key = key.toUpperCase();
		
		char[] msgArray = msg.toCharArray();
		char[] keyArray = key.toCharArray();

		int msgLen = msgArray.length;
		char[] newKey = new char[msgLen];
		
		//generate new key in cyclic manner equal to the length of original message
		for(int i = 0, j = 0; i < msgLen; ++i, ++j){
			if(j == keyArray.length)
				j = 0;
			
			if (msgArray[i] == ' ' || Arrays.binarySearch(allowedCharacters,msgArray[i])<0) {
				newKey[i] = ' ';
				j--;
				continue;
			}
			
			
			newKey[i] = keyArray[j];
		}
		
		
		char[] encryptedMsg = new char[msgLen];
		
		for(int i = 0; i < msgLen; ++i) {
			
			if (msgArray[i]==' ') {
				encryptedMsg[i] = ' ';
				continue;
				}
			
			if (Arrays.binarySearch(allowedCharacters,msgArray[i])<0) {
				encryptedMsg[i] = msgArray[i];
				continue;	
			}

			
			encryptedMsg[i] = (char)(((msgArray[i] + newKey[i]) % 26) + 'A');
			
		}
		
		return encryptedMsg;
	}
	

	public char[] decode(String msg, String key) {
		
		msg = msg.toUpperCase();
		key = key.toUpperCase();
		
		char[] msgArray = msg.toCharArray();
		char[] keyArray = key.toCharArray();

		int msgLen = msgArray.length;
		char[] newKey = new char[msgLen];
		
		//generate new key in cyclic manner equal to the length of original message
		for(int i = 0, j = 0; i < msgLen; ++i, ++j){
			if(j == keyArray.length)
				j = 0;
			
			if (msgArray[i] == ' ' || Arrays.binarySearch(allowedCharacters,msgArray[i])<0) {
				newKey[i] = ' ';
				j--;
				continue;
			}
			
			newKey[i] = keyArray[j];
		}
		
		char[] decryptedMsg = new char[msgLen];
		
		for(int i = 0; i < msgLen; ++i) {
			
			if (msgArray[i]==' ') {
				decryptedMsg[i] = ' ';
				continue;
				}
			
			if ( Arrays.binarySearch(allowedCharacters,msgArray[i])<0) {
				decryptedMsg[i] = msgArray[i];
				continue;
			}
			
			decryptedMsg[i] = (char)(((msgArray[i] - newKey[i] + 26) % 26) + 'A');
			}
		
		return decryptedMsg;
	}
	
	public static void main(String...s){

		String msg = "The jäva pr0grammer";
		String key = "neeraj";
	
		VigenereCipher cifra = new VigenereCipher();
		
		System.out.println("Original Message: " + msg);		//String.valueOf() converts character array to String
		//System.out.println("Key: " + String.valueOf(key));
		char[] encoded = cifra.encode(msg,key);
	
		System.out.println("\nEncrypted Message: " + String.valueOf(encoded));
		
		System.out.println("\nDecrypted Message: " + String.valueOf(cifra.decode(String.valueOf(encoded),key)));
		

	}
	
}
