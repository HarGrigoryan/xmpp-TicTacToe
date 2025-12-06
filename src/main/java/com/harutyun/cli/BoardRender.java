package com.harutyun.cli;

import com.harutyun.game.enums.Winner;
import com.harutyun.game.core.Player;
import com.harutyun.game.core.TicTacToeBoard;
import com.harutyun.game.enums.CellState;

public class BoardRender {

    public static String renderBoard(TicTacToeBoard board, Player player) {
        CellState cellState = player.getCellState();
        String boardString = "\nYOU: %s\nOPPONENT: %s".formatted(cellState, cellState == CellState.X ? CellState.O : CellState.X) + renderBoard(board);
        if(board.getWinner() == Winner.NOONE)
            boardString = boardString + board.currentTurn() + "'s turn!\n";
        return  boardString;
    }

    public static String renderBoard(TicTacToeBoard board) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        // append top border
        sb.append("   ┌───┬───┬───┐\n");

        for (int row = 0; row < 3; row++) {
            // append row number and left border piece
            sb.append(" ").append(row).append(" │ ");

            for (int col = 0; col < 3; col++) {
                String cell = board.getCellState(row, col);
                String display = " ".equals(cell) ? " " : cell;
                sb.append(display);

                if (col < 2) {
                    sb.append(" │ ");
                }
            }
            sb.append(" │\n");

            // append middle border
            if (row < 2) {
                sb.append("   ├───┼───┼───┤\n");
            }
        }

        // append bottom border
        sb.append("   └───┴───┴───┘\n");
        sb.append("     0   1   2  \n");  // append column numbers

        return sb.toString();
    }


}
