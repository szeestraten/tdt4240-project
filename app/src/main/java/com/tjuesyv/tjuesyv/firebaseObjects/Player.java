package com.tjuesyv.tjuesyv.firebaseObjects;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private String nickname;
    private Map<String, Object> games = new HashMap<>();

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public Player() {}

    public Player(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Map<String, Object> getGames() {
        return games;
    }

    public void addGame(String gameId) {
        games.put(gameId, true);
    }
}
