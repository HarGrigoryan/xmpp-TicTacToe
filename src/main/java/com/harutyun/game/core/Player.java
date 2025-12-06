package com.harutyun.game.core;

import lombok.Getter;

@Getter
public class Player extends Participant{

    private final String password;

    public Player(String userName, String password) {
        super(userName);
        this.password = password;
    }
}
