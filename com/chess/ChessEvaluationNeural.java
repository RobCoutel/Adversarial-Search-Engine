package com.chess;

import com.gameEngine.*;
import com.matrix.*;
import com.neuralNetworks.*;

public class ChessEvaluationNeural implements NeuralEvaluation {
    private static final int nbInputs = 64*12 + 5;
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

        //Each class variable, instance variable, or array component is initialized with
        //a default value when it is created (§15.9, §15.10) [...] For type int, the default value is zero, that is, 0.

        for(int i=0; i<64; i++) {
            if(squares[i] != 0){
                if(squares[i] / ChessBoard.BLACK == 0){
                    //white
                    input[i+64*(squares[i] - 1)] = 1;
                } else {
                    //black
                    input[i+64*((squares[i] % ChessBoard.BLACK) + 5)] = 1;
                }
            }
        }

        input[64*12] = ((double) board.getTurn());

        double[] castlingRights = board.getCastlingRights();
        for(int i=0; i<4; i++) {
            input[64*12 + 1 + i] = castlingRights[i];
        }

        /*
        for(int i=0; i<64*12; i++) {
            if(i%8 == 0){
                System.out.print("\n");
            }
            if(i%64 == 0){
                System.out.print(ChessBoard.pieceName(1 + i/12) + "\n");
            }
            System.out.print(Double.toString(input[i]) + " ");
        }*/

		return nn.propagate(new Matrix(input)).getContent()[0][0];
	}

    public static int getNbInputs() { return nbInputs; }

    public NeuralNetwork getNeuralNet() { return nn; }
}
