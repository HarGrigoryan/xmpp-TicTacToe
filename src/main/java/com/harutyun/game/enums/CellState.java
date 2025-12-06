package com.harutyun.game.enums;

public enum CellState {

    X(1), O(0);

    private final int binary;

    CellState(int binary)
    {
        this.binary = binary;
    }

    int getBinary(){
        return binary;
    }


}

