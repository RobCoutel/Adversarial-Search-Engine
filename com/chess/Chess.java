/*
command to run this main() :
    java --class-path bin  com.chess.Chess
    java --class-path bin  com.chess.Chess <white agent> <black agent>
*/

package com.chess;

import com.gameEngine.Board;
import com.gameEngine.Player;
import com.neuralNetworks.NeuralNetwork;
import com.agents.*;

public class Chess {
    private static final int depth = 3;
    private static final int budget = 50000;

    private static Player getAgent(String agentName, String color) {
        if(agentName.equals("Human")) {
            return new Human(color + " Human");
        }
        if(agentName.equals("AgentBudget")) {
            return new AgentBudget(color + " Agent Budget", 50000, new ChessEvaluation());
        }
        else if(agentName.equals("AgentBudgetGain")) {
            return new AgentBudgetGain(color + " Agent Budget Gain", budget, new ChessEvaluation());
        }
        else if(agentName.equals("AgentMinimax")) {
            return new AgentMinimax(color + " AgentMinimax", depth, new ChessEvaluation());
        }
        else if(agentName.equals("AgentMinimaxSorted")) {
            return new AgentMinimaxSorted(color + " Agent Minimax Sorted", depth, new ChessEvaluation());
        }
        else if(agentName.equals("AgentNeuralBudget")) {
            NeuralNetwork nn = NeuralNetLoader.loadBestNN("", "Chess");
            return new AgentBudget(color + " Agent Neural Budget", budget, new ChessEvaluationNeural(nn));
        }
        else if(agentName.equals("AgentNeuralMinimax")) {
            NeuralNetwork nn = NeuralNetLoader.loadBestNN("", "Chess");
            return new AgentMinimax(color + " Agent Neural Minimax", depth, new ChessEvaluationNeural(nn));
        }
        else if(agentName.equals("AgentRandom")) {
            return new AgentRandom(color + "Agent Random");
        }
        System.out.println("Unknown Agent \"" + agentName + "\"");
        return null;
    }

    public static void main(String[] args) {
        Player white, black;
        if(args.length == 0) {
            white = new AgentMinimaxSorted("White", 3, new ChessEvaluation());
            black = new AgentMinimaxSorted("Black", 3, new ChessEvaluation());
        }
        else {
            white = getAgent(args[0], "White");
            black = getAgent(args[1], "Black");
        }
        if(white == null || black == null) {
            System.out.println("Invalid agent name");
            return;
        }
        ChessBoard board = new ChessBoard(white, black);
        board.activatePrint();
        board.play();
        if(board.gameOver() != 2) {
            System.out.println("GAME OVER");
        }
    }
}
