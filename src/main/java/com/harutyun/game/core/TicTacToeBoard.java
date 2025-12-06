package com.harutyun.game.core;

import com.harutyun.game.enums.CellState;
import com.harutyun.game.enums.Winner;
import com.harutyun.game.exception.IllegalMoveException;


public class TicTacToeBoard {

    private final CellState[][] board = new CellState[rowCount][colCount];
    private CellState lastMove = CellState.O;
    private Winner winner = Winner.NOONE;

    private final static int rowCount = 3;
    private final static int colCount = 3;

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return colCount;
    }

    public CellState currentTurn()
    {
        return lastMove == CellState.O ? CellState.X : CellState.O;
    }

    public void performStep(CellState cellState, int row, int col) throws IllegalMoveException {
        if(cellState == lastMove) {
            throw new IllegalMoveException("Not your turn!");
        }
        if (row < 0 || row >= getRowCount())
            throw new IllegalMoveException("Illegal row number of [%s]".formatted(row));
        if(col < 0 || col >= getColumnCount())
            throw new IllegalMoveException("Illegal column number of [%s]".formatted(col));
        if(board[row][col] != null)
            throw new IllegalMoveException("The specified cell is already full.");
        board[row][col] = cellState;
        lastMove = cellState;
        winner = whoWon();
    }

    public synchronized void performStep(Move move) throws IllegalMoveException {
        performStep(move.getCellState(), move.getRow(), move.getCol());
    }

    public String getCellState(int row, int col){
        CellState cellState = board[row][col];
        return cellState == null ? " " : cellState.toString();
    }

    private Winner whoWon()
    {
        for(int col = 0; col < colCount; col++){
            Winner winner = checkColumn(col);
            if(winner != Winner.NOONE)
                return winner;
        }
        for(int row = 0; row < rowCount; row++){
            Winner winner = checkRow(row);
            if(winner != Winner.NOONE)
                return winner;
        }
        Winner winner = checkDiagonal(true);
        if (winner != Winner.NOONE)
            return winner;
        return checkDiagonal(false);
    }

    private Winner checkDiagonal(boolean fromLeft){
        if(fromLeft)
        {
            CellState prev = board[0][0];
            if(prev == null)
                return Winner.NOONE;
            for(int row = 1; row < rowCount; row++){
                CellState cellState = board[row][row];
                if(cellState == null || prev != cellState)
                    return Winner.NOONE;
            }
            return Winner.getWinnerFromCellState(prev);
        }
        int initialRow = 0;
        int initialCol = colCount - 1;
        CellState prev = board[initialRow][initialCol];
        if(prev == null)
            return Winner.NOONE;
        for(int row = initialRow + 1; row < rowCount; row++){
            CellState cellState = board[row][--initialCol];
            if(cellState == null || prev != cellState)
                return Winner.NOONE;
        }
        return Winner.getWinnerFromCellState(prev);
    }

    private Winner checkColumn(int col){
        CellState prev = board[0][col];
        if(prev == null)
            return Winner.NOONE;
        for(int row = 1; row < rowCount; row++){
            CellState cellState = board[row][col];
            if(cellState == null || prev != cellState)
                return Winner.NOONE;
        }
        return Winner.getWinnerFromCellState(prev);
    }

    private Winner checkRow(int row){
        CellState prev = board[row][0];
        if(prev == null)
            return Winner.NOONE;
        for(int col = 1; col < colCount; col++){
            CellState cellState = board[row][col];
            if (cellState == null || prev != cellState)
                return Winner.NOONE;
        }
        return Winner.getWinnerFromCellState(prev);
    }

    public synchronized Winner getWinner() {
        return winner;
    }

}

