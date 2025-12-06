package com.harutyun.game.enums;

import lombok.Getter;

@Getter
public enum Winner {

    X,
    DRAW,
    O,
    NOONE;

    public static Winner getWinnerFromCellState(CellState cellState){
        return switch (cellState) {
            case X -> Winner.X;
            case O -> Winner.O;
        };
    }
}
