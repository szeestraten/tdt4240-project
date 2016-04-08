package com.tjuesyv.tjuesyv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.tjuesyv.tjuesyv.gameHandlers.GameHandler;
import com.tjuesyv.tjuesyv.gameModes.DefaultMode;
import com.tjuesyv.tjuesyv.ui.Prompter;

import butterknife.ButterKnife;

public class GameActivity extends AppCompatActivity {

    private String gameUID;

    private Firebase rootRef;
    private Firebase gamesRef;
    private Firebase usersRef;
    private Firebase currentGameRef;
    private Firebase currentUserRef;
    private AuthData authData;

    private GameHandler gameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup activity
        init();

        // Start game
        gameHandler.startGame();
    }

    private void init() {
        setContentView(R.layout.activity_game);

        // Setup ButterKnife
        ButterKnife.bind(this);

        // Get intent
        Intent intent = getIntent();
        gameUID = intent.getStringExtra("GAME_UID");

        // Create main Firebase ref
        rootRef = new Firebase(getResources().getString(R.string.firebase_url));

        // Get Firebase authentication
        authData = rootRef.getAuth();

        // Setup other Firebase references
        gamesRef = rootRef.child("games");
        usersRef = rootRef.child("users");
        currentGameRef = gamesRef.child(gameUID);
        currentUserRef = usersRef.child(authData.getUid());

        // Setup game
        gameHandler = new GameHandler(new DefaultMode(), this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            reallyExit();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void reallyExit() {
        new Prompter(getText(R.string.prompt_exit), this) {
            @Override
            public void callBack(boolean answer) {
                if (answer) finish();
            }
        }.ask();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        // Exit server if host
        // Logout if user
    }
}