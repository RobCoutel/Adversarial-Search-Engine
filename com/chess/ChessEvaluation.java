package com.chess;

import com.gameEngine.*;

public class ChessEvaluation implements Evaluation {

    public ChessEvaluation() {

    }

	public double evaluate(Board brd) {
        ChessBoard board = (ChessBoard) brd;
        int result = board.gameOver();
        if(result != 2) {
            return 1000 * result;
        }
        double toReturn = 0;
        toReturn += material(board);
        toReturn += 0.1 * controledSquares(board);
		return toReturn;
	}

    public double material(ChessBoard board) {
        double toReturn = 0;
        for(byte i=0; i<64; i++) {
            int piece = board.getPiece(i);
            if(piece != ChessBoard.UNDEFINED) {
                toReturn += ChessBoard.pieceWorth(piece) * ((piece/ChessBoard.BLACK)==0 ? 1 : -1);
            }
        }
        return toReturn;
    }

    public double controledSquares(ChessBoard board) {
        int[] whiteControl = board.getControl(0);
        int[] blackControl = board.getControl(1);

        double toReturn = 0;
        for(byte i=0; i<64; i++) {
            double factor = 0.1 * (8 - abs(board.getFile(i)-3.5) + abs(board.getRank(i)-3.5));
            toReturn += whiteControl[i] * (1+factor);
            toReturn -= blackControl[i] * (1+factor);
        }
        return toReturn;
    }

    private double abs(double a) { return a<0? -a : a; }

}
