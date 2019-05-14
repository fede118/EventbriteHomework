package com.example.guessfour;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class NumberGuesserFragment extends Fragment {
    private static final String TAG = NumberGuesserFragment.class.getSimpleName();

    private Context context;
    private LinearLayout numberGuessedLayout;
    private LinearLayout digitsInputLinearLayout;
    private NumberPicker exactMatchNP ;
    private NumberPicker wrongPosNP;
    private Button userFeedbackBtn;

    private boolean gameStarted = false;

    private int numberDigits;
    HashMap<String, ArrayList<Integer>> columnsValues;

    private ArrayList<Integer> lastGuess;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_number_guesser, container, false);

        context = getContext();

        digitsInputLinearLayout = (LinearLayout) rootView.findViewById(R.id.digitsNumberLinearLayout);
        exactMatchNP = new NumberPicker(context);
        wrongPosNP= new NumberPicker(context);

        TextView numPickerInfo = new TextView(context);
        numPickerInfo.setText(R.string.number_picker_info_textview);
        digitsInputLinearLayout.addView(numPickerInfo);
        final NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(4);
        digitsInputLinearLayout.addView(numberPicker);

        userFeedbackBtn = (Button) rootView.findViewById(R.id.userFeedBackBtn);

        final Button startBtn = (Button) rootView.findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameStarted) {
//                    todo: change button to restart
//                    display some message, delete everything else?
                } else {
                    initializeColumnPools(numberPicker.getValue());
                    initializeMatchInputs(context, digitsInputLinearLayout);

                    ArrayList<Integer> guessedNumber = guess(null, null);
                    numberGuessedLayout = rootView.findViewById(R.id.generatedNumberLinearLayout);
                    displayGuessedNumber(guessedNumber);


                    startBtn.setText(R.string.correct_number_button);
                    userFeedbackBtn.setVisibility(View.VISIBLE);
                    gameStarted = !gameStarted;
                }

            }
        });

        userFeedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int exactMatch = exactMatchNP.getValue();
                int wrongPosition = wrongPosNP.getValue();

                displayGuessedNumber(guess(exactMatch, wrongPosition));
                Log.d(TAG, "NEW GUESS AFTER FEEDBACK => " + lastGuess);
            }
        });

        return rootView;
    }


    private ArrayList<Integer> guess (@Nullable Integer exactNum, @Nullable Integer wrongPosNum) {
        if (exactNum == null && wrongPosNum == null) {
            return createRandomNumberFromPool();
        } else {
            if (exactNum == 0) {
                removeNumbersFromPools(lastGuess);
            }

            // todo: el resto de los casos
            // user lastGuess para los casos con different position

            return createRandomNumberFromPool();
        }
    }

    private void displayGuessedNumber (ArrayList<Integer> guessedNumber) {
        Log.d(TAG, guessedNumber.toString());
        numberGuessedLayout.removeAllViews();
        for (int i = 0; i < guessedNumber.size(); i++) {
            TextView textView = new TextView(context);
            textView.setText(String.valueOf(guessedNumber.get(i)));
            textView.setTextSize(50);
            numberGuessedLayout.addView(textView);

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
        lastGuess = randomNumber;
        return lastGuess;
    }

    private void removeNumbersFromPools(ArrayList<Integer> numsToErrase) {
        if (numsToErrase == null) return;
        for (int i = 0; i < numsToErrase.size(); i++) {
            ArrayList<Integer> columnPool = columnsValues.get("column" + i);
            if (columnPool.contains(numsToErrase.get(i))) {
                columnPool.remove(numsToErrase.get(i));
            }
        }

        Log.d(TAG, "columns updated: " + columnsValues.toString());
    }

    private void initializeColumnPools (int numberLength) {
        numberDigits = numberLength;

        columnsValues = new HashMap<>();
        for (int i = 0; i < numberDigits; i++) {
            ArrayList<Integer> numbers = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            columnsValues.put("column" + i, numbers);
            Log.d(TAG, "column" + i + ": " + numbers.toString());
        }
    }

    private void initializeMatchInputs (Context context, LinearLayout linearLayout) {
        linearLayout.removeAllViews();
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        //        todo: esto deberia ser una helper class y crear 2 instancias una para exactmatch y otra para wrong position

        LinearLayout exactMatchLinearLayout = new LinearLayout(context);
        exactMatchLinearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView exactMatchTextView = new TextView(context);
        exactMatchTextView.setText(R.string.exact_match_textview);
        exactMatchLinearLayout.removeAllViews();
        exactMatchLinearLayout.addView(exactMatchTextView);


        exactMatchNP.setMaxValue(numberDigits);
        exactMatchLinearLayout.addView(exactMatchNP);

        LinearLayout wrongPosLinearLayout = new LinearLayout(context);
        wrongPosLinearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView wrongPosTextView = new TextView(context);
        wrongPosTextView.setText(R.string.wrong_position_textview);
        wrongPosLinearLayout.removeAllViews();
        wrongPosLinearLayout.addView(wrongPosTextView);
        wrongPosNP.setMaxValue(numberDigits);
        wrongPosLinearLayout.addView(wrongPosNP);

        linearLayout.addView(exactMatchLinearLayout);
        linearLayout.addView(wrongPosLinearLayout);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    }
}
