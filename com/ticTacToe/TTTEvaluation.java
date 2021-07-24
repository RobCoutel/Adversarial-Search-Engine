package com.ticTacToe;

import com.gameEngine.Evaluation;
import com.gameEngine.Board;

public class TTTEvaluation implements Evaluation {
    public TTTEvaluation() {

    }

	public double evaluate(Board board) {
        int result = board.gameOver();
        if(result == 2) {
            return 0;
        }
        return result * 1000;
	}
}
