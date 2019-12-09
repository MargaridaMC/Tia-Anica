package com.example.mg.tiaanica;

class AlphaSum {

    int sum = 0;

    AlphaSum(String text){
        
        text = text.toUpperCase();

        for (int i = 0; i < text.length(); i++){
            char letter = text.charAt(i);

            switch(letter){
                case 'A':
                    sum = sum + 1;
                    break;

                case 'B':
                    sum += 2;
                    break;

                case 'C':
                    sum += 3;
                    break;

                case 'D':
                    sum += 4;
                    break;

                case 'E':
                    sum += 5;
                    break;

                case 'F':
                    sum += 6;
                    break;

                case 'G':
                    sum += 7;
                    break;

                case 'H':
                    sum += 8;
                    break;

                case 'I':
                    sum += 9;
                    break;

                case 'J':
                    sum += 10;
                    break;

                case 'K':
                    sum += 11;
                    break;

                case 'L':
                    sum += 12;
                    break;

                case 'M':
                    sum += 13;
                    break;

                case 'N':
                    sum += 14;
                    break;

                case 'O':
                    sum += 15;
                    break;

                case 'P':
                    sum += 16;
                    break;

                case 'Q':
                    sum += 17;
                    break;

                case 'R':
                    sum += 18;
                    break;

                case 'S':
                    sum += 19;
                    break;

                case 'T':
                    sum += 20;
                    break;

                case 'U':
                    sum += 21;
                    break;

                case 'V':
                    sum += 22;
                    break;

                case 'W':
                    sum += 23;
                    break;

                case 'X':
                    sum += 24;
                    break;

                case 'Y':
                    sum += 25;
                    break;

                case 'Z':
                    sum += 26;
                    break;

                case '0':
                    sum += 0;
                    break;

                case '1':
                    sum += 1;
                    break;

                case '2':
                    sum += 2;
                    break;

                case '3':
                    sum += 3;
                    break;

                case '4':
                    sum += 4;
                    break;

                case '5':
                    sum += 5;
                    break;

                case '6':
                    sum += 6;
                    break;

                case '7':
                    sum += 7;
                    break;

                case '8':
                    sum += 8;
                    break;

                case '9':
                    sum += 9;
                    break;
            }

        }

    }

}
