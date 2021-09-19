package com.ticTacToe;

import com.gameEngine.Move;

public class TTTMove implements Move {
    int move;
    boolean resign = false;

    public TTTMove(int i) {
        if(i < 0) {
            resign = true;
        }
        move = i;
    }

    public TTTMove(String name) {
        resign = true;
        move = -1;
    }

    public int getDestination() { return move; }

    public TTTMove clone() {
        return new TTTMove(move);
    }

    public boolean isResignation() { return resign; }

    public int compareTo(Move move) {
        return 0;
    }

    public boolean equals(Move move2) {
        return this.toString().equals(move2.toString());
    }

    public String toString() {
        if(resign) {
            return "resign";
        }
        return Integer.toString(move);
    }

    public boolean isThreat() {
        return false;
    }
}
