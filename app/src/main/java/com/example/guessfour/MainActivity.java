/*
    Juego de de adivinar un numero de 4 digitos o que la aplicación adivine un numero de hasta 4
    digitos.
    Creado por Federico Cano
 */

package com.example.guessfour;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements UserGuessingFragment.OnBtnClickListerner, NumberGuesserFragment.OnGameFinishedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

//    constantes para manejar los Fragments
    private static final int USER_GUESSING_FRAGMENT = 0;
    private static final int APP_GUESSING_FRAGMENT = 1;

    private TextView gameModeTextView;

    private boolean gameModeUserGuessing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameModeTextView = findViewById(R.id.gameModeTextView);

        initFragment(USER_GUESSING_FRAGMENT);
    }

    /*
    Callback del Fragment UserGuessing cuando usuario adivino el numero
     */
    @Override
    public void userGuessingGameFinished() {
        gameModeTextView.setText(R.string.game_mode_thinking_textview_try_again);
        initFragment(USER_GUESSING_FRAGMENT);
    }

    /*
    Callback del Frament NumberGuesser cuando adivinamos el numero
     */
    @Override
    public void numberGuesserGameEnded() {
        gameModeTextView.setText(R.string.game_mode_thinking_textview_try_again);
        initFragment(APP_GUESSING_FRAGMENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
        En el menu podemos cambiar el modo de juego (osea el fragment)
        y podemos Reiniciar tocando el boton con el Icono de Undo.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_switchMode:
//                if mode is guessing change to thinking
                gameModeUserGuessing = !gameModeUserGuessing;
                changeMode();
                return true;
            case R.id.menu_undo:
                if (gameModeUserGuessing) {
                    initFragment(USER_GUESSING_FRAGMENT);
                } else {
                    initFragment(APP_GUESSING_FRAGMENT);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    funcion que simplemente inicia el fragmento de acuerdo a gameModeUserGuessing,
    es decir si este boolean es verdad inicia el fragmento UserGuessing
    si es falso quiere decir que la aplicacion debe adivinar el numero del usuario
     */
    private void changeMode () {
        if (gameModeUserGuessing) {
            gameModeTextView.setText(R.string.game_mode_guessing_textview);
            initFragment(USER_GUESSING_FRAGMENT);
        } else {
            gameModeTextView.setText(R.string.game_mode_thinking_textview);
            initFragment(APP_GUESSING_FRAGMENT);
        }
    }

    /*
    helper function
    dado el FragId definido como una constante se inicia el fragmento correspondiente
    usando fragment manager y reemplazando el fragmento que estaba en uso en el placeholder
     */
    private void initFragment(int fragId) {
        Fragment frag;
        switch (fragId) {
            case USER_GUESSING_FRAGMENT:
                frag = new UserGuessingFragment();
                break;
            case APP_GUESSING_FRAGMENT:
                frag = new NumberGuesserFragment();
                break;
            default:
                frag = new UserGuessingFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutMainAct, frag).commit();
    }
}
