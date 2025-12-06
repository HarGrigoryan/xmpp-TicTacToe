package com.harutyun.game.enums;

import lombok.Getter;

@Getter
public enum Winner {

    X(CellState.X),
    DRAW(null),
    O(CellState.X),
    NOONE(null);

    private CellState cellState;

    Winner(CellState state){
    }

    public static Winner getWinnerFromCellState(CellState cellState){
        return switch (cellState) {
            case X -> Winner.X;
            case O -> Winner.O;
            default -> Winner.NOONE;
        };
    }
}
