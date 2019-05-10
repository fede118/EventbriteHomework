/*
    Helper class.
    para manejar la logica de crear los numberPickers y la de adivinar
*/

package com.example.guessfour;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

class CreateNumberPickers {
    private static final String TAG = CreateNumberPickers.class.getSimpleName();

    private LinearLayout linearLayout;
    private ArrayList<Integer> numberSequenceToGuess;
    private static int amountOfNumbersInPlay;

    /*
    constructor. amount: la cantidad de numeros a adivinar/randomizar, por si despues se quiere
        aumentar la cantidad (crea la canitdad de numberPickers que se declare en amount)
    */
    CreateNumberPickers (Context context, int amount, LinearLayout container) {
        amountOfNumbersInPlay = amount;

        this.linearLayout = container;
        linearLayout.removeAllViews();
        for (int i = 0; i < amount; i++) {
            NumberPicker numberPicker = new NumberPicker(context);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(9);
            linearLayout.addView(numberPicker);
        }

        this.numberSequenceToGuess = createRandomNumber();
        Log.d(TAG, String.format("Number to be guessed: %s", numberSequenceToGuess.toString()));
    }

    ArrayList<Integer> getNumberSequenceInput () {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < linearLayout.getChildCount(); i++){
            NumberPicker numPicker = (NumberPicker) linearLayout.getChildAt(i);
            res.add(numPicker.getValue());
        }

        return res;
    }

    int[] checkSuccess () {
        int[] res = new int[2];
        ArrayList<Integer> userInput = getNumberSequenceInput();

        Log.d(TAG, numberSequenceToGuess.toString());
        if (userInput.equals(numberSequenceToGuess)) {
            res[0] = amountOfNumbersInPlay;
            return res;
        } else {
            for (int i = 0; i < numberSequenceToGuess.size(); i++) {
                if (userInput.get(i) == numberSequenceToGuess.get(i)) {
                    res[0]++;
                } else if (numberSequenceToGuess.contains(userInput.get(i))) {
                    res[1]++;
                }
            }
        }
        return res;
    }

    private ArrayList<Integer> createRandomNumber() {
        Random random = new Random();
        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < amountOfNumbersInPlay;i++) {
            result.add(random.nextInt(9));
        }

        return result;
    }

    void lockNumberPickers () {
        Log.d(TAG, "locking number pickers");
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            NumberPicker numberPicker = (NumberPicker) linearLayout.getChildAt(i);
            numberPicker.setEnabled(false);
        }
    }
}
