package com.chess;

import com.gameEngine.*;
import com.matrix.*;
import com.neuralNetworks.*;

public class ChessEvaluationNeural implements NeuralEvaluation {
    private static final int nbInputs = 64 + 5;
    private NeuralNetwork nn;

    public ChessEvaluationNeural(NeuralNetwork nn) {
        if(nn.getDimensions()[0] != nbInputs) {
            throw new IllegalArgumentException
            ("Error in EvaluationNeuralNet : The number of inputs of the Neural Network is "
            + nn.getDimensions()[0] + " (expected " + Integer.toString(nbInputs) + ")");
        }
        this.nn = nn;
    }

    public double evaluate(Board board_) {
        ChessBoard board = (ChessBoard) board_;
        double[] input = new double[nbInputs];

        int[] squares = board.getSquares();

        for(int i=0; i<64; i++) {
            input[i] = squares[i]/ChessBoard.BLACK == 0 ?
                        ((double) (squares[i])/ ChessBoard.BLACK) :
                      - ((double) (squares[i] % ChessBoard.BLACK) / ChessBoard.BLACK);
        }
        input[64] = ((double) board.getTurn()) - 0.5;

        double[] castlingRights = board.getCastlingRights();
        for(int i=0; i<4; i++) {
            input[65 + i] = castlingRights[i];
        }
        //System.out.println("Output : " + Double.toString(nn.propagate(input)[0]));

		return nn.propagate(new Matrix(input)).getTable()[0][0];
	}

    public static int getNbInputs() { return nbInputs; }

    public NeuralNetwork getNeuralNet() { return nn; }
}
