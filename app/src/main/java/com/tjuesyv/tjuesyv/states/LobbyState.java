package com.tjuesyv.tjuesyv.states;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tjuesyv.tjuesyv.gameHandlers.GameObserver;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;
import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.firebaseObjects.Game;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by RayTM on 08.04.2016.
 */
public class LobbyState extends GameState {

    @Bind(R.id.gameCodeTextView) TextView gameCodeTextView;
    @Bind(R.id.startedTextView) TextView startedTextView;
    @Bind(R.id.activeTextView) TextView activeTextView;
    @Bind(R.id.startGameButton) Button startGameButton;
    @Bind(R.id.playerListView) ListView playersListView;

    private static final int MAIN_VIEW = 0;

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    public LobbyState(GameObserver observer) {
        super(observer);

        // Setup ButterKnife
        ButterKnife.bind(this, this.observer.getActivityReference());

        // Displays game info
        setGameInfo();

        // Displays players in a listView
        setPlayerList();

        // Set startbutton if host
        setStartButton();
    }

    @OnClick(R.id.startGameButton)
    protected void startGameButton() {
        nextState();
    }

    /**
     * Populates game info textViews.
     */
    private void setGameInfo() {
        observer.getFirebaseGameReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);
                gameCodeTextView.setText("Game code: " + game.getGameCode());
                startedTextView.setText("Started: " + game.getStarted());
                activeTextView.setText("Active: " + game.getActive());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void setStartButton() {
        // If we are not the game host, disable start button and change text
        if (!observer.isHost()) {
            startGameButton.setText("Waiting on host to start game...");
            startGameButton.setEnabled(false);
        }
    }

    /**
     * Updates player list when players are added or removed.
     */
    private void setPlayerList() {
        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>
                (observer.getActivityReference(), android.R.layout.simple_list_item_1, android.R.id.text1);
        // Assign adapter to ListView
        playersListView.setAdapter(adapter);

        // Set child listener for the current games players
        observer.getFirebaseGameReference().child("players").addChildEventListener(new ChildEventListener() {
            // If player is added
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                observer.getFirebaseUsersReference().child(dataSnapshot.getKey()).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapter.add(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            // If player is removed
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                observer.getFirebaseUsersReference().child(dataSnapshot.getKey()).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapter.remove(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
}
