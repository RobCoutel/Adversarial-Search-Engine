package com.agents;

import com.gameEngine.*;
import java.util.Vector;

public class AgentMinimax extends Agent {
    private int maxDepth;

    public AgentMinimax(String name, int maxDepth, Evaluation evaluation) {
        super(name, evaluation);
        this.maxDepth = maxDepth;
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

		for(Move legalMove : board.getLegalMoves()) {
            board.move(legalMove);
            double eval = minimax(board, alpha, beta, maxDepth);
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

    private double minimax(Board board, double alpha, double beta, int depth) {
        int turn = board.getTurn();
        int result = board.gameOver();
        if(depth == 0 || board.gameOver() != 2) {
            nbNodesExplored++;
            return evaluation.evaluate(board);
        }

        double bestEval = turn==0?
                          Double.NEGATIVE_INFINITY :
                          Double.POSITIVE_INFINITY;
        for(Move legalMove : board.getLegalMoves()) {
            board.move(legalMove);
            double eval = minimax(board, alpha, beta, depth-1);
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
