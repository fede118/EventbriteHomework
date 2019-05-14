package com.example.guessfour;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements UserGuessingFragment.OnBtnClickListerner {
    private static final String TAG = MainActivity.class.getSimpleName();

//    cantidad de numeros en juego (esto lo puedo hacer que lo cambie el usuario despues)
    private static final int AMOUNT_OF_NUMBERS_IN_PLAY = 4;

    private boolean gameModeGuessing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUserGuessFragment();
        changeMode();
    }

    @Override
    public void UserGuessingCallback(boolean gameFinished) {
        initUserGuessFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switchMode:
//                if mode is guessing change to thinking
                if (gameModeGuessing) {
                    gameModeGuessing = false;
                } else {
                    gameModeGuessing = true;
                }
                changeMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeMode () {
        TextView gameModeTextView = findViewById(R.id.gameModeTextView);
        if (gameModeGuessing) {
            gameModeTextView.setText(R.string.game_mode_thinking_textview);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayoutMainAct, new NumberGuesserFragment()).commit();

        } else {
            gameModeTextView.setText(R.string.game_mode_guessing_textview);
            initUserGuessFragment();
        }
    }

    private void initUserGuessFragment() {
        UserGuessingFragment userGuessingFragment = new UserGuessingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("amount",AMOUNT_OF_NUMBERS_IN_PLAY);
        userGuessingFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutMainAct, userGuessingFragment).commit();
    }
}
