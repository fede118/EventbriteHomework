package com.example.guessfour;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

class NumberGuesser {
    private static final String TAG = NumberGuesser.class.getSimpleName();

    private int amountOfNumbersToGues;
    HashMap<String, ArrayList<Integer>> columnsValues;

    private ArrayList<Integer> lastGuess;

    public NumberGuesser(int amount) {
        this.amountOfNumbersToGues = amount;

        columnsValues = new HashMap<>();
        for (int i = 0; i < amountOfNumbersToGues; i++){
            ArrayList<Integer> numbers = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));
            columnsValues.put("column" + i, numbers);
            Log.d(TAG, "column" + i +": " + numbers.toString());
        }
    }

    ArrayList<Integer> guess (@Nullable Integer exactNum, @Nullable Integer wrongPosNum) {
        ArrayList<Integer> guessedNumber = new ArrayList<>();
        if (exactNum == null && wrongPosNum == null) {
            lastGuess = createRandomNumberFromPool();
            return lastGuess;
        } else {
            if (exactNum == 0) {
                removeNumbersFromPools(lastGuess);
            }

            return guessedNumber;
        }
    }

    private ArrayList<Integer> createRandomNumberFromPool () {
        ArrayList<Integer> randomNumber = new ArrayList<>();
        for (int i = 0; i < columnsValues.size(); i++) {
            Random random = new Random();
            ArrayList<Integer> numPool = columnsValues.get("column" + i);
            int number = numPool.get(random.nextInt(numPool.size()));
            randomNumber.add(number);
        }

        Log.d(TAG, "GENERATED RANDOM NUMBER: " + randomNumber.toString());
        return randomNumber;
    }

    private void removeNumbersFromPools(ArrayList<Integer> numsToErrase) {
        if (numsToErrase == null) return;
        for (int i = 0; i < numsToErrase.size(); i++) {
            ArrayList<Integer> columnPool = columnsValues.get("column" + i);
            if (columnPool.contains(numsToErrase.get(i))) {
                columnPool.remove(numsToErrase.get(i));
            }
        }
    }
}
