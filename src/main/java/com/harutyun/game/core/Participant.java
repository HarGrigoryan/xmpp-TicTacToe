package com.harutyun.game.core;

import com.harutyun.game.enums.CellState;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Participant {

    private final String userName;
    @Setter
    private CellState cellState;

    public Participant(String userName) {
        validateUserName(userName);
        this.userName = userName;
    }

    private void validateUserName(String userName) {
        if(userName == null || !userName.endsWith("@xmpp.jp"))
        {
            throw new IllegalArgumentException("Invalid user name format: " + userName + ". Must end with @xmpp.jp");
        }
    }

    public String getName()
    {
        return userName.substring(0, userName.indexOf("@"));
    }

}
