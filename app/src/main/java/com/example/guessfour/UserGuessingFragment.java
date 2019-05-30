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
import java.util.Random;

public class UserGuessingFragment extends Fragment {
    private static final String TAG = UserGuessingFragment.class.getSimpleName();

    /*
    constante pensada para a futuro poder cambiar si se desea el modo en que se determina la
    cantidad de digitos en juego
    */
    private static final int AMOUNT_OF_NUMBERS_IN_PLAY = 4;

    private ArrayList<Integer> numberSequenceToGuess;

    private LinearLayout numPicksLinearLayout;
    private LinearLayout userGuessingResultLinearLayout;
    private TextView exactMatchTextView;    // exactMatch = Numero Bien
    private TextView wrongPositionTextView;  // wrongPosition = Numero Regular
    private Button guessButton;

    private boolean gameFinished = false;

    OnBtnClickListerner onBtnClickListerner;

    public interface OnBtnClickListerner {
        void userGuessingGameFinished();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        final Context context = getContext();

        View rootView = inflater.inflate(R.layout.fragment_user_guessing, container,false);

        numPicksLinearLayout = (LinearLayout) rootView.findViewById(R.id.userGuessingNumPicksLinearLayout);
        userGuessingResultLinearLayout = (LinearLayout) rootView.findViewById(R.id.userGuessingResultLinearLayout);
        exactMatchTextView = new TextView(context);
        exactMatchTextView.setTextSize(25);
        wrongPositionTextView = new TextView(context);
        wrongPositionTextView.setTextSize(25);

        guessButton = (Button) rootView.findViewById(R.id.guessButton);

        setNumberPickers(context, numPicksLinearLayout, AMOUNT_OF_NUMBERS_IN_PLAY);

        numberSequenceToGuess = createRandomNumber();
        Log.d(TAG, String.format("NUMBER TO GUESS: %s", numberSequenceToGuess.toString()));

        /*
        cuando tocamos el boton de adivinar si el juego no ha terminado checkeamos cuantos numeros
        bien y cuantos regulares hay en el numero del usuario y lo mostramos en los TextViews
         */
        guessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameFinished) {
                    userGuessingResultLinearLayout.removeAllViews();

                    int exactMatch = checkSuccess()[0];
                    int wrongPosition = checkSuccess()[1];

                    exactMatchTextView.setText(getString(R.string.exact_match_textview, String.valueOf(exactMatch)));
                    wrongPositionTextView.setText(getString(R.string.wrong_position_textview, String.valueOf(wrongPosition)));

                    userGuessingResultLinearLayout.addView(wrongPositionTextView);
                    userGuessingResultLinearLayout.addView(exactMatchTextView);

                    if (exactMatch == AMOUNT_OF_NUMBERS_IN_PLAY) {
                        Toast.makeText(context, "Ganaste!!", Toast.LENGTH_SHORT).show();

                        userGuessingResultLinearLayout.removeAllViews();

                        guessButton.setText(R.string.restart_button);
                        gameFinished = true;
                    }
                } else {
//                        game finished need to restart
                    onBtnClickListerner.userGuessingGameFinished();
                }
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

    /*
    funcion que devuelve el numero ingresado en los NumberPickers por el usuario
     */
    ArrayList<Integer> getNumberSequenceInput () {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < numPicksLinearLayout.getChildCount(); i++){
            NumberPicker numPicker = (NumberPicker) numPicksLinearLayout.getChildAt(i);
            res.add(numPicker.getValue());
        }

        Log.d(TAG, res.toString());
        return res;
    }

    /*
    funcion que devuelve un int Array con la cantidad de numeros bien (ExactMatch) en index 0
    y la cantidad de numeros regulares en index [1]

     */
    int[] checkSuccess () {
        int[] res = new int[2];
        ArrayList<Integer> userInput = getNumberSequenceInput();

        Log.d(TAG, numberSequenceToGuess.toString());
        if (userInput.equals(numberSequenceToGuess)) {
            res[0] = AMOUNT_OF_NUMBERS_IN_PLAY;
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

    /*
    funcion que devuelve un ArrayList de integers al azar del 0 a 9 y crea la cantidad de digitos
    segun la constante AMOUNT_OF_NUMBERS_IN_PLAY
     */
    private ArrayList<Integer> createRandomNumber() {
        Random random = new Random();
        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < AMOUNT_OF_NUMBERS_IN_PLAY;i++) {
            result.add(random.nextInt(9));
        }

        return result;
    }

    /*
    funcion que crea NumberPickers en un linearLayout determinado y la cantidad que se necesiten
    en este caso se usa la constante AMOUNT_OF_NUMBERS_IN_PLAY pero permite flexibilidad en caso
    de que mas adelante se quiera agregar funcionalidad que aumente la cantidad de digitos a adivinar
     */
    private void setNumberPickers(Context context, LinearLayout linearLayout,  int amount) {
        for (int i = 0; i < amount; i++) {
            NumberPicker numberPicker = new NumberPicker(context);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(9);
            linearLayout.addView(numberPicker);
        }
    }
}
