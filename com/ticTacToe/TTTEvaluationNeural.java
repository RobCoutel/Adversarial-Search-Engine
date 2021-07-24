package com.ticTacToe;

import com.gameEngine.*;
import com.matrix.*;
import com.neuralNetworks.NeuralNetwork;

public class TTTEvaluationNeural implements NeuralEvaluation {
    private static final int nbInputs = 10;
    private NeuralNetwork nn;

    public TTTEvaluationNeural(NeuralNetwork nn) {
        if(nn.getDimensions()[0] != nbInputs) {
            throw new IllegalArgumentException
            ("Error in EvaluationNeuralNet : The number of inputs of the Neural Network is "
            + nn.getDimensions()[0] + " (expected " + Integer.toString(nbInputs) + ")");
        }
        this.nn = nn;
    }

    public double evaluate(Board board) {
        double[] input = new double[nbInputs];

        int[] squares = board.getSquares();

        for(int i=0; i<squares.length; i++) {
            input[i] = squares[i];
        }
        input[squares.length] = ((double) board.getTurn())*2 - 1;
        //System.out.println("Output : " + Double.toString(nn.propagate(input)[0]));
        return nn.propagate(new Matrix(input)).getTable()[0][0];
    }

    public static int getNbInputs() { return nbInputs; }

    public NeuralNetwork getNeuralNet() { return nn; }
}
