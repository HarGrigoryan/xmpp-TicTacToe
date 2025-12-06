package com.harutyun.game.core;

import lombok.Getter;

@Getter
public class GameOffer {

    private final String gameId;
    private final Participant opponent;


    public GameOffer(String gameOffer, Participant opponent) {
        gameId = gameOffer;
        this.opponent = opponent;
    }
}
