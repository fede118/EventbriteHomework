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
import java.util.Locale;
import java.util.Random;

public class UserGuessingFragment extends Fragment {
    private static final String TAG = UserGuessingFragment.class.getSimpleName();

    private ArrayList<Integer> numberSequenceToGuess;
    private static int amountOfNumbersInPlay;

    private LinearLayout numPicksLinearLayout;
    private LinearLayout userGuessingResultLinearLayout;
    private TextView gameModeTextView;
    private Button guessOrSetNumberBtn;

    private boolean gameFinished = false;

    OnBtnClickListerner onBtnClickListerner;

    public interface OnBtnClickListerner {
        void UserGuessingCallback(boolean gameFinished);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        final Context context = getContext();
        Bundle bundle = getArguments();

        View rootView = inflater.inflate(R.layout.fragment_user_guessing, container,false);

        numPicksLinearLayout = (LinearLayout) rootView.findViewById(R.id.userGuessingNumPicksLinearLayout);
        userGuessingResultLinearLayout = (LinearLayout) rootView.findViewById(R.id.userGuessingResultLinearLayout);
//        userGuessingResultLinearLayout.removeAllViews();

        amountOfNumbersInPlay = bundle.getInt("amount");

        for (int i = 0; i < amountOfNumbersInPlay; i++) {
            NumberPicker numberPicker = new NumberPicker(context);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(9);
            numPicksLinearLayout.addView(numberPicker);
        }

        gameModeTextView = (TextView) rootView.findViewById(R.id.gameModeTextView);
        guessOrSetNumberBtn = (Button) rootView.findViewById(R.id.guessOrSetNumberBtn);

        numberSequenceToGuess = createRandomNumber();
        Log.d(TAG, String.format("NUMBER TO GUESS: %s", numberSequenceToGuess.toString()));

        guessOrSetNumberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                int[] success = checkSuccess();

//                if (gameModeGuessing) {
                    if (!gameFinished) {
                        userGuessingResultLinearLayout.removeAllViews();

                        TextView exactMatchTextView = new TextView(context);
                        TextView notExactMatchTextView = new TextView(context);

                        int exactMatch = checkSuccess()[0];
                        int notExactMatch = checkSuccess()[1];

                        notExactMatchTextView.setText(String.format(Locale.getDefault(), "Right number wrong position: %d", notExactMatch));
                        exactMatchTextView.setText(String.format(Locale.getDefault(), "Exact Matchs: %d", exactMatch));
                        userGuessingResultLinearLayout.addView(notExactMatchTextView);
                        userGuessingResultLinearLayout.addView(exactMatchTextView);

                        if (exactMatch == amountOfNumbersInPlay) {
                            Toast.makeText(context, "You WON!!", Toast.LENGTH_SHORT).show();

                            userGuessingResultLinearLayout.removeAllViews();

                            btn.setText(R.string.restart_button);
                            gameFinished = true;
                        }
                    } else {
//                        game finished need to restart
                        onBtnClickListerner.UserGuessingCallback(true);
//                        mCreateNumberPickers = new CreateNumberPickers(this, AMOUNT_OF_NUMBERS_IN_PLAY, numberPicksLinearLayout);
//                        btn.setText(R.string.guess_button);
//                        gameFinished = false;
                    }
//                } else {
//                    mCreateNumberPickers.lockNumberPickers();
//
//
//                    Log.d(TAG, "initializing NUMBER GUESSER");
//                    NumberGuesser numberGuesser = new NumberGuesser(AMOUNT_OF_NUMBERS_IN_PLAY);
//                    numberGuesser.guess(null, null);
//                }


                }
            });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onBtnClickListerner = (OnBtnClickListerner) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ArrayList<Integer> getNumberSequenceInput () {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < numPicksLinearLayout.getChildCount(); i++){
            NumberPicker numPicker = (NumberPicker) numPicksLinearLayout.getChildAt(i);
            res.add(numPicker.getValue());
        }

        Log.d(TAG, res.toString());
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
        for (int i = 0; i < numPicksLinearLayout.getChildCount(); i++) {
            NumberPicker numberPicker = (NumberPicker) numPicksLinearLayout.getChildAt(i);
            numberPicker.setEnabled(false);
        }
    }
}
