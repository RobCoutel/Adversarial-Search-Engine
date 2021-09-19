/*
command to run this main() :
    java --class-path bin  com.ticTacToe.TicTacToe
    java --class-path bin  com.ticTacToe.TicTacToe <x agent> <o agent>
*/

package com.ticTacToe;

import com.gameEngine.Board;
import com.gameEngine.Player;
import com.neuralNetworks.NeuralNetwork;
import com.agents.*;

public class TicTacToe {
    private static final int depth = 10;
    private static final int budget = 500;
    private static Player getAgent(String agentName, String color) {
        if(agentName.equals("Human")) {
            return new Human(color + " Human");
        }
        if(agentName.equals("AgentBudget")) {
            return new AgentBudget(color + " Agent Budget", budget, new TTTEvaluation());
        }
        else if(agentName.equals("AgentBudgetGain")) {
            return new AgentBudgetGain(color + " Agent Budget Gain", budget, new TTTEvaluation());
        }
        else if(agentName.equals("AgentMinimax")) {
            return new AgentMinimax(color + " AgentMinimax", depth, new TTTEvaluation());
        }
        else if(agentName.equals("AgentMinimaxSorted")) {
            return new AgentMinimaxSorted(color + " Agent Minimax Sorted", depth, new TTTEvaluation());
        }
        else if(agentName.equals("AgentNeuralBudget")) {
            NeuralNetwork nn = NeuralNetLoader.loadBestNN("", "TicTacToe");
            return new AgentBudget(color + " Agent Neural Budget", budget, new TTTEvaluationNeural(nn));
        }
        else if(agentName.equals("AgentNeuralMinimax")) {
            NeuralNetwork nn = NeuralNetLoader.loadBestNN("", "TicTacToe");
            return new AgentMinimax(color + " Agent Neural Minimax", depth, new TTTEvaluationNeural(nn));
        }
        else if(agentName.equals("AgentRandom")) {
            return new AgentRandom(color + "Agent Random");
        }
        else if(agentName.equals("Search")) {
            return new AgentSearch(color + " Agent Seach", depth, new TTTEvaluation());
        }
        System.out.println("Unknown Agent \"" + agentName + "\"");
        return null;
    }

    public static void main(String[] args) {
        Player xplayer, oplayer;
        if(args.length == 0) {
            xplayer = new AgentMinimax("X", depth, new TTTEvaluation());
            oplayer = new AgentMinimax("O", depth, new TTTEvaluation());
        }
        else {
            xplayer = getAgent(args[0], "X ");
            oplayer = getAgent(args[1], "O ");
        }
        if(xplayer == null || oplayer == null) {
            return;
        }

        TTTBoard board = new TTTBoard(xplayer, oplayer);
        board.activatePrint();
        board.play();
    }
}
