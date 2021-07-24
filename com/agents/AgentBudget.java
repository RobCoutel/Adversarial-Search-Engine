package com.agents;

import com.gameEngine.*;

import java.util.Vector;
import java.util.Map;
import java.util.Collections;

public class AgentBudget extends Agent {
    protected int totalBudget;

    public AgentBudget(String name, int totalBudget, Evaluation evaluation) {
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
        for(Move legalMove : legalMoves) {
            budget += (totalBudget - nbNodesExplored) / (nbMovesLeft--);

            board.move(legalMove);
            double eval = minimax(board, alpha, beta, budget);
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

        //System.out.println(name + " : The move was evaluated "
        //+ nbNodesExplored + " nodes to " + bestEval);
        return toPlay;
    }

    protected double minimax(Board board, double alpha, double beta,
                             int maxBudget) {
        int turn = board.getTurn();

        if(maxBudget <= nbNodesExplored - 1 || board.gameOver() != 2) {
            nbNodesExplored++;
            return evaluation.evaluate(board);
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
            double eval = minimax(board, alpha, beta, budget);
            board.undo();

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
