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

    private int NUMBER_TO_GUESS_LENGTH;

    private Context context;
    private LinearLayout numberGuessedLayout;
    private LinearLayout digitsInputLinearLayout;
    private NumberPicker exactMatchNumPicker;
    private NumberPicker wrongPositionNumPicker;
    private Button startBtn;
    private Button userFeedbackBtn;
    private NumberPicker numberLengthNumPicker;
    private TextView numberGuessTextView;

    private boolean gameStarted = false;

    private ArrayList<Integer> lastGuess;
    private ArrayList<ArrayList<Integer>> wrongGuesses;
    private ArrayList<int[]> wrongGuessesFeedback;
    private HashMap<String, ArrayList<Integer>> columnsValues;


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
        numberGuessedLayout = (LinearLayout) rootView.findViewById(R.id.numberGuessedLinearLayout);
        startBtn = (Button) rootView.findViewById(R.id.startBtn);
        userFeedbackBtn = (Button) rootView.findViewById(R.id.userFeedBackBtn);

        numberGuessTextView = new TextView(context);
        numberGuessTextView.setTextSize(50);

        numberLengthNumPicker = (NumberPicker) rootView.findViewById(R.id.numberLengthNumPicker);
        numberLengthNumPicker.setMaxValue(4);
        numberLengthNumPicker.setMinValue(2);

        wrongGuesses = new ArrayList<>();
        wrongGuessesFeedback = new ArrayList<>();

        startBtn.setText(R.string.start_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameStarted) {
                    Toast.makeText(context, "Woohoo!", Toast.LENGTH_SHORT).show();

                    onGameFinishedListener.numberGuesserGameEnded();
                } else {
                    NUMBER_TO_GUESS_LENGTH = numberLengthNumPicker.getValue();

                    numberGuessedLayout.removeAllViews();
                    digitsInputLinearLayout.removeAllViews();
                    exactMatchNumPicker = new NumberPicker(context);
                    wrongPositionNumPicker = new NumberPicker(context);
                    digitsInputLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                    digitsInputLinearLayout.addView(createNumberPickerWithTitle(context,
                            R.string.exact_match_textview, exactMatchNumPicker, NUMBER_TO_GUESS_LENGTH));
                    digitsInputLinearLayout.addView(createNumberPickerWithTitle(context,
                            R.string.wrong_position_textview, wrongPositionNumPicker, NUMBER_TO_GUESS_LENGTH));

                    digitsInputLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

                    initializeColumnPools();

                    ArrayList<Integer> guessAttemptNumber = guess(null, null);
                    numberGuessTextView.setText(arrayToStringFormatted(guessAttemptNumber));
                    numberGuessedLayout.addView(numberGuessTextView);

                    userFeedbackBtn.setVisibility(View.VISIBLE);
                    startBtn.setText(R.string.correct_number_button);
                    gameStarted = true;
                }

            }
        });

        userFeedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int exactMatch = exactMatchNumPicker.getValue();
                int wrongPosition = wrongPositionNumPicker.getValue();

                if (exactMatch + wrongPosition > NUMBER_TO_GUESS_LENGTH) {
                    Toast.makeText(context, "bien + regular no puede ser mas que la cantidad de digitos", Toast.LENGTH_SHORT).show();
                    return;
                }

                numberGuessTextView.setText(arrayToStringFormatted(guess(exactMatch, wrongPosition)));
            }
        });

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

    /*
    crea un Hashmap donde cada digito (o columna) tiene un pozo (o pool) con los numeros del 0 al 9
     */
    private void initializeColumnPools () {
        columnsValues = new HashMap<>();
        for (int i = 0; i < NUMBER_TO_GUESS_LENGTH; i++) {
            ArrayList<Integer> numbers = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            columnsValues.put("column" + i, numbers);
        }
    }

    /*
    GUESS: funcion core del fragment

    si se ejecuta con ambos parametros nulos, es decir sin feedback
    ejecuta createRandomNumberFromPool (ver mas abajo)

    si hay feedback es decir sabemos cuantos numeros en posicion correcta y en posicion incorrecta
    paso 1:
        Finaliza el juego si numeros "bien" == digitos que tiene numero
        y quita los numeros de los pozos de cada "columna"  (si no hay numeros "bien")
        o de todos los pzoos (si tampoco hay numeros "regulares")

    paso 2
        Luego agrega el ultimo numero generado a la lista de numeros incorrectos y el feedback de ese numero
        a la vez en la lista de feedbacks (ambos tiene el mismo index para accederlos facilmente)

        se genera nextNumer ( 0000 -> 0001 ...) del ultimo numero intentado
        y se compara con todos los numeros intentados anteriores y sus feedbacks hasta el proximo que
        coincida con todos. Ademas el numero tiene que ser valido segun los pozos de numeros para cada
        columna del numero (que se han actualizado en el paso 1)

    failsafe:
        se registra donde empezo el loop, si se vuelve a ese punto se hace break,
        si el break es el en el ultimo numero de la lista de numeros intentados es que no se pudo
        coincidir los feedbacks recibidos con el numero actual
     */
    private ArrayList<Integer> guess (@Nullable Integer exactNum, @Nullable Integer wrongPosNum) {
        if (exactNum == null && wrongPosNum == null) {
            return createRandomNumberFromPool();
        } else if (exactNum != null && wrongPosNum != null){
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

            wrongGuesses.add(lastGuess);
            wrongGuessesFeedback.add(userFeedBack);

            ArrayList<Integer> nextNumber = nextPossibleNumber(lastGuess);

            for (int i = 0; i < wrongGuesses.size(); i++) {
                ArrayList<Integer> wrongGuess = wrongGuesses.get(i);
                int[] currentNumberFeedback = wrongGuessesFeedback.get(i);
                int [] numbersComparison = compareTwoNumbers(wrongGuess, nextNumber);

                ArrayList<Integer> loopStart = nextNumber;

                while (!Arrays.equals(currentNumberFeedback, numbersComparison) || !checkIfValidNumbersFromPools(nextNumber) ) {
                    nextNumber = nextPossibleNumber(nextNumber);

                    numbersComparison = compareTwoNumbers(wrongGuess, nextNumber);

                    Log.d(TAG, nextNumber.toString());

                    if (nextNumber.equals(loopStart)) {
                        if (i == wrongGuesses.size() - 1) {
                            Toast.makeText(context, "por favor empeza de nuevo, no se pudo determinar el numero", Toast.LENGTH_SHORT).show();
                            onGameFinishedListener.numberGuesserGameEnded();
                        }
                        break;
                    }
                }
            }

            lastGuess = nextNumber;
            return lastGuess;
        }
        return lastGuess;
    }

    /*
    crea un numero al azar teniendo en cuenta los numeros disponibles en cada pozo
     */
    private ArrayList<Integer> createRandomNumberFromPool () {
        ArrayList<Integer> randomNumber = new ArrayList<>();
        for (int i = 0; i < columnsValues.size(); i++) {
            Random random = new Random();
            ArrayList<Integer> numPool = columnsValues.get("column" + i);
            int number = numPool.get(random.nextInt(numPool.size()));
            randomNumber.add(number);
        }

        lastGuess = randomNumber;
        return lastGuess;
    }

    /*
    quita los numeros solo de cada columna. Ej.:
    si le ingresamos el numero 1234 va sacar el 1 del pozo de la primer columna o digito
    el 2 de la segunda columna, el 3 de la tercera y el 4 de la cuarta.
    (no necesariamente tienen que ser 4 digitos)
     */
    private void removeNumbersFromEachPools (ArrayList<Integer> numsToErrase) {
        if (numsToErrase == null) return;
        for (int i = 0; i < numsToErrase.size(); i++) {
            ArrayList<Integer> columnPool = columnsValues.get("column" + i);
            if (columnPool.contains(numsToErrase.get(i))) {
                columnPool.remove(numsToErrase.get(i));
            }
        }
    }

    /*
    saca todos los numeros de todos los pozos
    Ej.: si le ingresamos el numero 1234 va a sacar los numeros 1, 2, 3 y 4 de todos los pozos
    (no necesariamente tienen que ser 4 digitos)
     */
    private void removeNumbersFromAllPools(ArrayList<Integer> numsToErase) {
        if (numsToErase == null) return;

        for (int i = 0; i < numsToErase.size(); i++) {
            ArrayList<Integer> columnPool = columnsValues.get("column" + i);
            for (int digit : numsToErase) {
                if (columnPool.contains(digit)) columnPool.remove(columnPool.indexOf(digit));
            }
        }
    }

    /*
    compara 2 numeros y dice cuantos numeros estan "bien" (exact match) y cuantos estan "regular"
    (wrong Position)

     */
    private int[] compareTwoNumbers (ArrayList<Integer> prevNum, ArrayList<Integer> newNum) {
        int exactMatches = 0;
        int wrongPosition = 0;

        for (int i = 0; i < prevNum.size(); i++) {
            if (prevNum.get(i).equals(newNum.get(i))) exactMatches++;
            else if (prevNum.contains(newNum.get(i))) wrongPosition++;
        }
        return new int[] {exactMatches, wrongPosition};
    }

    /*
    funcion que devuelve el numero ingresado mas 1, sin aumentar la cantidad de digitos
    es decir si se ingresa el numero 99 no va a devolver 100, sino que va a devolver 00 (vuelve a
    empezar)
    NOTA: se realiza una copia de la lista ya que las listas son mutables y no queremos modificar
    la lista ingresada
     */
    private ArrayList<Integer> nextPossibleNumber (ArrayList<Integer> number) {
        ArrayList<Integer> listCopy = new ArrayList<>(number);

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

    /*
    funcion que devuelve un booleano como resultado de checkear si cada digito se encuentra disponible
    en su respectivo pozo
     */
    private boolean checkIfValidNumbersFromPools (ArrayList<Integer> number) {
        int validNumbers = 0;

        for (int i = 0; i < number.size(); i++) {
            if (columnsValues.get("column" + i).contains(number.get(i))) validNumbers++;
        }

        return validNumbers == number.size();
    }

    /*
    funcion que crea los NumberPickers con los titulos "bien" y "regular"
     */
    private LinearLayout createNumberPickerWithTitle(Context context, int titleStringId, NumberPicker numberPicker, int numberPickerMaxVal ) {
        String idToString = getResources().getString(titleStringId, "");

        LinearLayout verticalLinearLayout = new LinearLayout(context);
        verticalLinearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView titleTextView = new TextView(context);
        titleTextView.setText(idToString);
        titleTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        verticalLinearLayout.addView(titleTextView);

        numberPicker.setMaxValue(numberPickerMaxVal);
        verticalLinearLayout.addView(numberPicker);

        return verticalLinearLayout;
    }

    /*
    funcion que formatea una lista a una string

    NOTA: Ej.: <arrayList>.toString() = "[1,2,3,4]" en vez de "1234"
     */
    private String arrayToStringFormatted(ArrayList number) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < number.size(); i++) {
            res.append(number.get(i));
        }

        return res.toString();
    }
}
