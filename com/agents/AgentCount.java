package com.agents;

import com.agents.Agent;

import com.gameEngine.Board;
import com.gameEngine.Move;
import com.gameEngine.Player;

import java.util.Vector;

public class AgentCount extends Agent {
    private int maxDepth;

    public AgentCount(String name, int maxDepth) {
        super(name, null);
        this.maxDepth = maxDepth;
    }

    public Move play(Board board) {
        nbNodesExplored = 0;
        countRec(board, maxDepth+1);
        return null;
    }

    private void countRec(Board board, int depth) {
        if(depth == 0) {
            nbNodesExplored++;
            return;
        }
        if(board.gameOver() != 2) {
            return;
        }
        Vector<Move> legalMoves = board.getLegalMoves();
        checkDuplicates(legalMoves);
        for(Move legalMove : board.getLegalMoves()) {
            if(legalMoves == null) { System.out.println("null"); }
            board.move(legalMove);
            countRec(board, depth-1);
            board.undo();
        }
    }

    private void checkDuplicates(Vector<Move> legalMoves) {
        for(int i=0; i<legalMoves.size(); i++) {
            for(int j=i+1; j<legalMoves.size(); j++) {
                if(legalMoves.get(i).equals(legalMoves.get(j))) {
                    System.out.println("Duplicate move : " + legalMoves.get(i));
                }
            }
        }
    }
}
