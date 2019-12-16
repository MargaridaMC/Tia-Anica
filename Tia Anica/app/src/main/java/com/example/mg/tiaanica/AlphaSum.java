package com.example.mg.tiaanica;

class AlphaSum {

    private int sum = 0;

    AlphaSum(String text){
        
        text = text.toUpperCase();

        for (int i = 0; i < text.length(); i++){
            char letter = text.charAt(i);

            if(letter >= 'A' && letter <= 'Z')
                sum += letter - 'A' + 1; // 'A' = 1
            else if(letter >= '0' && letter <= '9')
                sum += letter - '0';  // '0' = 0
        }

    }

    int getSum(){
        return sum;
    }

}
