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

    public int getDestination() { return move; }

    public TTTMove clone() {
        return new TTTMove(move);
    }

    public boolean isResignation() { return resign; }

    public int compareTo(Move move) {
        return 0;
    }

    public String toString() {
        return Integer.toString(move);
    }
}
