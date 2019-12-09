package com.example.mg.tiaanica;
import java.util.Arrays;

class VigenereCipher {

    private char[] allowedCharacters = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

    private char[] msg;
    private char[] key;
    private int msgLen;

    VigenereCipher(String inputMessage, String inputKey) {

        char[] msgArray = inputMessage.toUpperCase().toCharArray();
        char[] keyArray = inputKey.toUpperCase().toCharArray();

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

        this.msg = msgArray;
        this.key = newKey;
        this.msgLen = inputMessage.length();
    }

    String encode() {

        char[] encryptedMsg = new char[msgLen];

        for(int i = 0; i < msgLen; ++i) {

            if (this.msg[i]==' ') {
                encryptedMsg[i] = ' ';
                continue;
            }

            if (Arrays.binarySearch(allowedCharacters, this.msg[i])<0) {
                encryptedMsg[i] = this.msg[i];
                continue;
            }


            encryptedMsg[i] = (char)(((this.msg[i] + this.key[i]) % 26) + 'A');

        }

        return String.valueOf(encryptedMsg);
    }

    String decode() {

        char[] decryptedMsg = new char[msgLen];

        for(int i = 0; i < msgLen; ++i) {

            if (this.msg[i]==' ') {
                decryptedMsg[i] = ' ';
                continue;
            }

            if ( Arrays.binarySearch(allowedCharacters,this.msg[i])<0) {
                decryptedMsg[i] = this.msg[i];
                continue;
            }

            decryptedMsg[i] = (char)(((this.msg[i] - this.key[i] + 26) % 26) + 'A');
        }

        return String.valueOf(decryptedMsg);
    }
}
