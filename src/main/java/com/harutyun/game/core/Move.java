package com.harutyun.game.core;

import com.harutyun.game.enums.CellState;
import com.harutyun.game.exception.IllegalMoveException;
import lombok.Getter;

@Getter
public class Move {

    CellState cellState;
    Integer row;
    Integer col;


    public Move(CellState cellState, int row, int col) throws IllegalMoveException {
        this.cellState = cellState;
        setRow(row);
        setCol(col);
    }

    public void setRow(int row) throws IllegalMoveException {
        checkBounds(row);
        this.row = row;
    }

    private static void checkBounds(int n) throws IllegalMoveException {
        if(n < 0 || n >= 3)
            throw new IllegalMoveException("Row out of bounds");
    }

    public void setCol(int col) throws IllegalMoveException {
        checkBounds(col);
        this.col = col;
    }

    @Override
    public String toString() {
        return "Move[\n cellState=" + cellState + "\n row=" + row + "\n col=" + col + "\n]";
    }
}
