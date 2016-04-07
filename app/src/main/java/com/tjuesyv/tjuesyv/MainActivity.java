package com.tjuesyv.tjuesyv;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;

import org.hashids.Hashids;


import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.createGameButton) Button createGameButton;
    @Bind(R.id.joinGameButton) Button joinGameButton;
    @Bind(R.id.logoutButton) Button logoutButton;
    @Bind(R.id.gameCodeText) EditText gameCodeText;
    @Bind(R.id.nicknameText) EditText nicknameText;
    @Bind(R.id.nicknameTextInputLayout) TextInputLayout nicknameTextInputLayout;
    @Bind(R.id.gameCodeTextInputLayout) TextInputLayout gameCodeTextInputLayout;

    private Firebase rootRef;
    private Firebase gamesRef;
    private Firebase usersRef;
    private AuthData authData;
    private Firebase.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup ButterKnife
        ButterKnife.bind(this);

        // Create main Firebase ref
        rootRef = new Firebase(getResources().getString(R.string.firebase_url));
        gamesRef = rootRef.child("games");
        usersRef = rootRef.child("users");

        // Get Firebase authentication
        authData = rootRef.getAuth();
        authStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    // User is logged in
                    Snackbar.make(findViewById(R.id.rootView),
                            "Logged in as: " + authData.getUid(),
                            Snackbar.LENGTH_LONG).show();

                } else {
                    // User is not logged in
                    Snackbar.make(findViewById(R.id.rootView),
                            "No auth session found. Logging in...",
                            Snackbar.LENGTH_LONG).show();
                    authAnonymously();
                }
            }
        };
        rootRef.addAuthStateListener(authStateListener);
    }

    /**
     * Removes authentication state listener when destroying.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        rootRef.removeAuthStateListener(authStateListener);
    }

    /**
     * Validates the game code from the editText on text changed
     * @param input The CharSequence from the game code editText.
     */
    @OnTextChanged(R.id.gameCodeText)
    protected void validGameCode(CharSequence input) {
        isValidGameCode(input.toString());
    }

    /**
     * Validates the nickname from the editText on text changed
     * @param input The CharSequence from the nickname editText.
     */
    @OnTextChanged(R.id.nicknameText)
    protected void validNickname(CharSequence input) {
        isValidNickname(input.toString());
    }

    @OnClick(R.id.createGameButton)
    protected void createGameButton() {
        String nickname = nicknameText.getText().toString();
        // Authenticate player
        authenticatePlayer(nickname);
        // Create a new game
        String gameCode = createGame();
        // Join game
        joinGame(gameCode);
    }

    @OnClick(R.id.joinGameButton)
    protected void joinGameButton() {
        String gameCode = gameCodeText.getText().toString();
        String nickname = nicknameText.getText().toString();
        // Make sure game code is valid
        if (!isValidGameCode(gameCode))
            return;
        // Authenticate player
        authenticatePlayer(nickname);
        // Join game
        joinGame(gameCode);
    }

    /**
     * Clicking the logout button unauthenticates the player,
     */
    @OnClick(R.id.logoutButton)
    protected void logoutButton() {
        logoutAuthenticatedUser();
    }

    /**
     * Creates a new game entry on Firebase.
     * The game code is generated by using the UID that is generated when it's first pushed to Firebase.
     * @return  The game code of the newly created game.
     */
    private String createGame() {
        // Create reference to new game entry
        Firebase newGameRef = gamesRef.push();
        // Use the Firebase generated UID as salt to generate 4 letter/digit code
        String gameCode = createGameCode(newGameRef.getKey());
        // Create new game object
        Game newGame = new Game(gameCode);
        // Populate the game entry in Firebase with the game object
        newGameRef.setValue(newGame);
        return gameCode;
    }

    /**
     * Joins an active game based on the game code as the currently authenticated player.
     * Goes to the GameLobby activity if all goes well.
     * @param gameCode  The game code of the game to join.
     */
    private void joinGame(final String gameCode) {
        // Find games with the game code
        Query queryRef = rootRef.child("games").orderByChild("gameCode").equalTo(gameCode).limitToFirst(1);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get first snapshot from the query
                    final DataSnapshot gameSnapshot = snapshot.getChildren().iterator().next();

                    // Get the game object from Firebase
                    Game game = gameSnapshot.getValue(Game.class);

                    // Make sure game is active
                    if (!game.getActive()) {
                        gameCodeTextInputLayout.setError(getString(R.string.error_game_not_active));
                        return;
                    }
                    // Make sure the game is not full
                    if (game.getPlayers().entrySet().size() >= game.getMaxPlayers()) {
                        gameCodeTextInputLayout.setError(getString(R.string.error_game_is_full));
                        return;
                    }
                    Boolean containsNick=false;
                    for (String key:game.getPlayers().keySet()){
                        if (!containsNick){
                            containsNick=
                                    (String.valueOf(snapshot.child("users").child(key).child("nickname").getValue())
                                    .equalsIgnoreCase(String.valueOf(snapshot.child("users").child(authData.getUid()).child("nickname").getValue()))
                                    &&(!authData.getUid().equalsIgnoreCase(key)));
                        }
                    }
                    if(containsNick){
                        gameCodeTextInputLayout.setError(getString(R.string.error_taken_nick));
                        return;
                    }

                    Firebase user= new Firebase(String.valueOf(usersRef));
                    user.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });


                    // Add player UID to players list in game object and update Firebase
                    game.addPlayer(authData.getUid());
                    gamesRef.child(gameSnapshot.getKey()).setValue(game);

                    // Add game UID to players games list and update Firebase
                    Map<String, Object> games = new HashMap<String, Object>();
                    games.put(gameSnapshot.getKey(), true);
                    usersRef.child(authData.getUid()).child("games").updateChildren(games);

                    // Join game lobby as player
                    goToGameLobby(gameSnapshot.getKey());

                    // Clear any error messages
                    gameCodeTextInputLayout.setErrorEnabled(false);
                    gameCodeTextInputLayout.setError(null);
                } else {
                    // Display message if the query results with nothing
                    gameCodeTextInputLayout.setError(getString(R.string.error_game_not_found));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Snackbar.make(findViewById(R.id.rootView),
                        "Error joining game: " + gameCode + ". Error: " + firebaseError.toString(),
                        Snackbar.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Authenticates a new player if not already authenticated.
     * If already authenticated, update the nickname.
     * @param nickname  The nickname to be set for the player entry
     */
    private void authenticatePlayer(String nickname) {
        if (isValidNickname(nickname)) {
            if (authData == null) {
                authAnonymously();
            } else {
                // Add player info to the authenticated player
                rootRef.child("users").child(authData.getUid()).child("nickname").setValue(nickname);
            }
        }
    }

    /**
     * Authenticates anonymously to Firebase and sets the {@link #authData} field.
     */
    private void authAnonymously() {
        rootRef.authAnonymously(new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                setAuthenticatedUser(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Snackbar.make(findViewById(R.id.rootView),
                        "Error signing in player: " + firebaseError.toString(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Helper function for starting the GameLobby activity.
     * Passes over the game UID.
     * @param gameUID   The UID of the game.
     */
    private void goToGameLobby(String gameUID) {
        Intent intent = new Intent(this, GameLobby.class);
        intent.putExtra("GAME_UID", gameUID);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Checks if the game code is valid.
     * Sets an error message on the editText if not valid.
     * @param gameCode  The game code to be validated.
     * @return  True if game code is valid, false otherwise.
     */
    private boolean isValidGameCode(String gameCode) {
        boolean valid = true;
        if (gameCode.isEmpty()) {
            joinGameButton.setEnabled(false);
            gameCodeTextInputLayout.setError(getString(R.string.error_empty_game_code));
            valid = false;
        } else if (gameCode.length() > 0 && gameCode.length() < 4) {
            joinGameButton.setEnabled(false);
            gameCodeTextInputLayout.setError(getString(R.string.error_short_game_code));
            valid = false;
        } else {
            joinGameButton.setEnabled(true);
            gameCodeTextInputLayout.setError(null);
        }
        return valid;
    }

    /**
     * Checks if the nickname is valid.
     * Sets an error message on the editText if not valid.
     * @param nickname  The nickname to be validated.
     * @return  True if game code is valid, false otherwise.
     */
    private boolean isValidNickname(String nickname) {
        boolean valid = true;
        if (nickname.isEmpty()) {
            nicknameTextInputLayout.setError(getString(R.string.error_empty_nickname));
            valid = false;
        } else {
            nicknameTextInputLayout.setError(null);
        }
        return valid;
    }

    /**
     * Generates a hash using the UID of the game entry as salt.
     * @param salt  the salt to be used for hashing.
     * @return  A hash as a String
     */
    private String createGameCode(String salt) {
        Hashids hashids = new Hashids(salt, 4, getString(R.string.edit_valid_game_codes));
        return hashids.encode(1);
    }

    /**
     * Setter for authentication data.
     * @param authData  the authentication data to be set.
     */
    private void setAuthenticatedUser(AuthData authData) {
        this.authData = authData;
    }

    /**
     * Logs out the current authenticated user.
     */
    private void logoutAuthenticatedUser() {
        if (authData != null) {
            rootRef.unauth();
        }
    }
}
