package com.agents;

import com.gameEngine.*;

import java.util.Vector;
import java.util.Map;
import java.util.Collections;

public class AgentBudgetGain extends Agent {
    protected int totalBudget;

    public AgentBudgetGain(String name, int totalBudget, Evaluation evaluation) {
        super(name, evaluation);
        this.totalBudget = totalBudget;
    }

    public Move play(Board board) {
        int turn = board.getTurn();
        nbNodesExplored = 0;

        Move toPlay = null;

        double bestEval = turn==0?
                          Double.NEGATIVE_INFINITY :
                          Double.POSITIVE_INFINITY;

        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        int budget = 0;
        Vector<Move> legalMoves = board.getLegalMoves();
        int nbMovesLeft = legalMoves.size();

        double currentEval = evaluation.evaluate(board);
        for(Move legalMove : legalMoves) {
            budget += (totalBudget - nbNodesExplored) / (nbMovesLeft--);

            board.move(legalMove);
            double eval = minimax(board, alpha, beta, budget, currentEval);
            board.undo();

            // update the best evaluation and prunning values
            if(turn == 0) {
                // update the best move
                if (bestEval < eval){
                    bestEval = eval;
                    toPlay = legalMove;
                }
                alpha = Double.max(alpha, eval);
            }
            else {
                // update the best move
                if (bestEval > eval){
                    bestEval = eval;
                    toPlay = legalMove;
                }
                beta = Double.min(beta, eval);
            }
        }

        System.out.println(name + " : The move was evaluated "
        + nbNodesExplored + " nodes to " + bestEval);
        return toPlay;
    }

    protected double minimax(Board board, double alpha, double beta,
                             int maxBudget, double lastEval) {
        nbNodesExplored++;
        int turn = board.getTurn();
        double currentEval = evaluation.evaluate(board);
        if(maxBudget <= nbNodesExplored || board.gameOver() != 2) {
            return currentEval;
        }

        double bestEval = turn==0?
                          Double.NEGATIVE_INFINITY :
                          Double.POSITIVE_INFINITY;

        int budget = nbNodesExplored;
        Vector<Move> legalMoves = board.getLegalMoves();
        int nbMovesLeft = legalMoves.size();
        for(Move legalMove : legalMoves) {
            budget += (maxBudget - nbNodesExplored) / (nbMovesLeft--);

            board.move(legalMove);
            double eval = minimax(board, alpha, beta,
                      budget, currentEval);
            board.undo();
            eval = 0.99 * eval + 0.01 * (lastEval - eval);

            // update the best evaluation and prunning values
            if(turn == 0) {
                bestEval = Double.max(bestEval, eval);
                alpha = Double.max(alpha, eval);
            }
            else {
                bestEval = Double.min(bestEval, eval);
                beta = Double.min(beta, eval);
            }
            // prunne if necessary
            if(beta <= alpha) {
                break;
            }
        }
        return bestEval;
    }
}
