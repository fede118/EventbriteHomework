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
import android.widget.Toast;

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
    private Button startBtn;
    private Button userFeedbackBtn;
    private NumberPicker numberPicker;

    private boolean gameStarted = false;

    private int NUMBER_TO_GUESS_LENGTH;
    private HashMap<String, ArrayList<Integer>> columnsValues;

    private ArrayList<Integer> lastGuess;
    private ArrayList<ArrayList<Integer>> wrongGuesses;
    private ArrayList<int[]> wrongGuessesFeedback;
    private ArrayList<ArrayList<Integer>> discardedNumbers;
//    todo que sume los feedbacks al index de los guesses para despues acceder al mismo index

    public interface OnGameFinishedListener {
        void numberGuesserGameEnded();
    }

    OnGameFinishedListener onGameFinishedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_number_guesser, container, false);

        context = getContext();


        digitsInputLinearLayout = (LinearLayout) rootView.findViewById(R.id.digitsNumberLinearLayout);
        numberGuessedLayout = rootView.findViewById(R.id.generatedNumberLinearLayout);
        startBtn = (Button) rootView.findViewById(R.id.startBtn);
        userFeedbackBtn = (Button) rootView.findViewById(R.id.userFeedBackBtn);

        initializePreStart();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameStarted) {
//                    todo: change button to restart
//                    display some message, delete everything else?
                    Toast.makeText(context, "Woohoo!", Toast.LENGTH_SHORT).show();

                    onGameFinishedListener.numberGuesserGameEnded();
                } else {
                    NUMBER_TO_GUESS_LENGTH = numberPicker.getValue();

                    initializeColumnPools();
                    initializeMatchInputs(context, digitsInputLinearLayout);

                    ArrayList<Integer> guessedNumber = guess(null, null);
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

        wrongGuesses = new ArrayList<>();
        wrongGuessesFeedback = new ArrayList<>();
        discardedNumbers = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onGameFinishedListener = (OnGameFinishedListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Integer> guess (@Nullable Integer exactNum, @Nullable Integer wrongPosNum) {
        if (exactNum == null && wrongPosNum == null) {
            return createRandomNumberFromPool();
        } else {
//            prov
            if (exactNum == null) exactNum = 0;
            if (wrongPosNum == null) wrongPosNum = 0;

            int[] userFeedBack = new int[]{exactNum, wrongPosNum};

            if (exactNum == NUMBER_TO_GUESS_LENGTH) {
                Toast.makeText(context, "Woohoo!", Toast.LENGTH_SHORT).show();

                onGameFinishedListener.numberGuesserGameEnded();
            } else if (exactNum == 0) {
                if (wrongPosNum == 0) {
                    removeNumbersFromAllPools(lastGuess);
                } else {
                    removeNumbersFromEachPools(lastGuess);
                }
            }
//            todo: si hay 3 matches habria que ir probando de a una columna y volviendo al numero anterior


            Log.d(TAG, "Storing wrong guess: " + lastGuess);
            wrongGuesses.add(lastGuess);
            wrongGuessesFeedback.add(userFeedBack);

            ArrayList nextNumber = nextPossibleNumber(lastGuess);
            Log.d(TAG, "last guess Number: " + lastGuess);
            Log.d(TAG, "next pos number: " + nextNumber.toString());
            int [] numbersComparison = compareNumbers(lastGuess, nextNumber);
            Log.d(TAG, "numbers compared: " + Arrays.toString(numbersComparison));

//            while next pos number tiene que tneer en cuenta los discarded numbers
//            y while tambien tiene que tener en cuenta los wrongGuesses y feedback
            while (!Arrays.equals(userFeedBack, numbersComparison) || !checkIfValidNumbersFromPools(nextNumber)) {
                discardedNumbers.add(nextNumber);

                nextNumber = nextPossibleNumber(nextNumber);
                Log.d(TAG, "inside loop next number: " + nextNumber);
                numbersComparison = compareNumbers(lastGuess, nextNumber);
                Log.d(TAG, "comparison: " + Arrays.toString(numbersComparison));

//                todo: no solo tiene que checkear que coincida con el numero anterior
//                              sino con todo el feedback de todos los numbero anteriores
            }
//
            if (nextNumber == lastGuess){
                Log.d(TAG, "no more numbers available");
                Toast.makeText(context, "All numbers discarded", Toast.LENGTH_SHORT).show();
                return null;
            }
            lastGuess = nextNumber;
            return lastGuess;
        }
    }

    private void displayGuessedNumber (ArrayList<Integer> guessedNumber) {
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

    private void removeNumbersFromEachPools (ArrayList<Integer> numsToErrase) {
        if (numsToErrase == null) return;
        for (int i = 0; i < numsToErrase.size(); i++) {
            ArrayList<Integer> columnPool = columnsValues.get("column" + i);
            if (columnPool.contains(numsToErrase.get(i))) {
                columnPool.remove(numsToErrase.get(i));
            }
        }

        Log.d(TAG, "removed " + numsToErrase.toString() + " from EACH pool: " + columnsValues.toString());
    }

    private void removeNumbersFromAllPools(ArrayList<Integer> numsToErase) {
        if (numsToErase == null) return;

        for (int i = 0; i < numsToErase.size(); i++) {
            ArrayList<Integer> columnPool = columnsValues.get("column" + i);
            for (int digit : numsToErase) {
                if (columnPool.contains(digit)) columnPool.remove(columnPool.indexOf(digit));
            }
        }

        Log.d(TAG, "removed " + numsToErase + " from ALL pools: " + columnsValues.toString());
    }

    private int[] compareNumbers (ArrayList<Integer> prevNum, ArrayList<Integer> newNum) {
        int exactMatches = 0;
        int wrongPosition = 0;
        for (int i = 0; i < prevNum.size(); i++) {
            if (prevNum.get(i) == newNum.get(i)) {
                exactMatches++;
            } else if (prevNum.contains(newNum.get(i))) wrongPosition++;
        }
        return new int[] {exactMatches, wrongPosition};
    }

    private ArrayList<Integer> nextPossibleNumber (ArrayList<Integer> number) {
//        copiando porque las listas son mutables
//        las recursive calls van a crear una nueva copia en cada "vuelta"? mmmmm
        ArrayList<Integer> listCopy = new ArrayList<>();
        listCopy.addAll(number);

        if (!listCopy.isEmpty()) {
            int lastIndex = listCopy.size() - 1;
            int lastDigit = listCopy.get(lastIndex);
            lastDigit += 1;

            listCopy.remove(lastIndex);

            if (lastDigit >= 10) {
                return nextPossibleNumber(listCopy);
            }
            listCopy.add(lastDigit);
        }

        while (listCopy.size() < NUMBER_TO_GUESS_LENGTH) {
            listCopy.add(0);
        }
        return listCopy;
    }

    private boolean checkIfValidNumbersFromPools (ArrayList<Integer> number) {
        int validNumbers = 0;

        for (int i = 0; i < number.size(); i++) {
            if (columnsValues.get("column" + i).contains(number.get(i))) validNumbers++;
        }

        return validNumbers == number.size();
    }

    private void initializePreStart () {
        numberGuessedLayout.removeAllViews();
        digitsInputLinearLayout.removeAllViews();

        exactMatchNP = new NumberPicker(context);
        wrongPosNP= new NumberPicker(context);

        TextView numPickerInfo = new TextView(context);
        numPickerInfo.setText(R.string.number_picker_info_textview);
        digitsInputLinearLayout.addView(numPickerInfo);
        numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(2);
        numberPicker.setMaxValue(4);
        digitsInputLinearLayout.addView(numberPicker);

        startBtn.setText(R.string.start_button);
        userFeedbackBtn.setVisibility(View.INVISIBLE);
    }

    private void initializeColumnPools () {
                columnsValues = new HashMap<>();
        for (int i = 0; i < NUMBER_TO_GUESS_LENGTH; i++) {
            ArrayList<Integer> numbers = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            columnsValues.put("column" + i, numbers);
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


        exactMatchNP.setMaxValue(NUMBER_TO_GUESS_LENGTH);
        exactMatchLinearLayout.addView(exactMatchNP);

        LinearLayout wrongPosLinearLayout = new LinearLayout(context);
        wrongPosLinearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView wrongPosTextView = new TextView(context);
        wrongPosTextView.setText(R.string.wrong_position_textview);
        wrongPosLinearLayout.removeAllViews();
        wrongPosLinearLayout.addView(wrongPosTextView);
        wrongPosNP.setMaxValue(NUMBER_TO_GUESS_LENGTH);
        wrongPosLinearLayout.addView(wrongPosNP);

        linearLayout.addView(exactMatchLinearLayout);
        linearLayout.addView(wrongPosLinearLayout);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    }
}
