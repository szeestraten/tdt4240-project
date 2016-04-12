package com.tjuesyv.tjuesyv.states;

import android.widget.Button;

import com.tjuesyv.tjuesyv.R;
import com.tjuesyv.tjuesyv.gameHandlers.GameState;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by RayTM on 08.04.2016.
 * Here all players are faced with a fancy and exciting representation of the score so far into the game
 * Should also show a slightly different view if it's the final game
 */
public class ScoreState extends GameState {

    @Bind(R.id.scoreContinueButton) Button scoreContinueButton;

    private static final int MAIN_VIEW = 5;

    @Override
    public int getViewId() {
        return MAIN_VIEW;
    }

    @Override
    public void onEnter() {
        // Setup ButterKnife
        ButterKnife.bind(this, handler.getActivityReference());
    }

    @OnClick(R.id.scoreContinueButton)
    protected void goToNextRound() {
        nextState();
    }
}
