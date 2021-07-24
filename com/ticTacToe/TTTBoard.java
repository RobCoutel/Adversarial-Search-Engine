package com.ticTacToe;

import com.gameEngine.*;
import com.agents.*;

import java.util.Vector;

public class TTTBoard implements Board {
    private boolean printGame = false;
    protected static final int X = 1;
    protected static final int O = -1;
    protected static final int EMPTY = 0;

    private int[] board;
    private int[] moveStack;
    private int nbMoves;
    private Player oplayer, xplayer;
    private int turn;
    private int winner;

    public TTTBoard(Player xplayer, Player oplayer) {
        board = new int[9];
        moveStack = new int[9];
        nbMoves = 0;
        this.xplayer = xplayer;
        this.oplayer = oplayer;
        turn = 0;
    }

    public TTTBoard clone() {
        TTTBoard toReturn = new TTTBoard(this.xplayer, this.oplayer);
        toReturn.turn = this.turn;

        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                toReturn.board[i*3 + j] = this.board[i*3 + j];
            }
        }
        toReturn.nbMoves = this.nbMoves;
        for(int i=0; i<this.nbMoves; i++) {
            toReturn.moveStack[i] = this.moveStack[i];
        }
        return toReturn;
    }

    public void play() {
        while(gameOver() == 2) {
            if(printGame) {
                System.out.println(this);
                Player player = turn==0? xplayer : oplayer;
                System.out.println(player.getName() + " to play!");
            }
            Move move = turn==0? xplayer.play(this.clone()):
                                 oplayer.play(this.clone());
            if(move == null) {
                continue;
            }
            move(move);
        }
        if(printGame) {
            System.out.println(this);
            int result = gameOver();
            if(result == 1) {
                System.out.println(xplayer.getName() + " wins the game");
            }
            else if(result == -1) {
                System.out.println(oplayer.getName() + " wins the game");
            }
            else {
                System.out.println("------------------\nThe game is a draw\n------------------");
            }
        }
    }

    public void move(Move move) {
        TTTMove tttMove = (TTTMove) move;
        int i = move.getDestination();
        if(board[i] != EMPTY) {
            System.out.println("Illegal Move");
        }
        board[i] = turn==0? X : O;
        moveStack[nbMoves++] = i;
        turn = 1 - turn;
    }

    public void undo() {
        int lastMove = moveStack[--nbMoves];
        board[lastMove] = EMPTY;
        turn = 1 - turn;
    }

    public int gameOver() {
        // check rows
        for(int i=0; i<3; i++) {
            int symbolRow = board[i*3];
            int symbolCol = board[i];
            boolean wonRow = true;
            boolean wonCol = true;
            for(int j=1; j<3; j++) {
                wonRow = wonRow && board[i*3 + j] == symbolRow && symbolRow != EMPTY;
                wonCol = wonCol && board[j*3 + i] == symbolCol && symbolCol != EMPTY;
            }
            if(wonRow) {
                return symbolRow;
            }
            if(wonCol) {
                return symbolCol;
            }
        }

        // check diag
        int symbol1 = board[0];
        boolean won1 = true;
        int symbol2 = board[2];
        boolean won2 = true;
        for(int i=1; i<3; i++) {
            won1 = won1 && board[i*4] == symbol1 && symbol1 != EMPTY;
            won2 = won2 && board[i*2 + 2] == symbol2 && symbol2 != EMPTY;
        }
        if(won1) {
            return symbol1;
        }
        if(won2) {
            return symbol2;
        }

        if(nbMoves == 9) {
            return 0;
        }

        return 2;
    }

    public Vector<Move> getLegalMoves() {
        Vector<Move> legalMoves = new Vector<Move>(9-nbMoves);
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                if(board[i*3 + j] == EMPTY) {
                    TTTMove newMove = new TTTMove(i*3 + j);
                    legalMoves.add(/*(Move)*/ newMove);
                }
            }
        }
        return legalMoves;
    }

    public int[] getSquares() { return board; }

    public int getTurn() { return turn; }

    public String toString() {
        String s = "";
        s += "-------------\n";
        for(int i=0; i<3; i++) {
            s += "| ";
            for(int j=0; j<3; j++) {
                if(board[i*3 + j] == EMPTY) {
                    s += " ";
                }
                else {
                    s += board[i*3 + j]==X ? "X" : "O";
                }
                s += " | ";
            }
            s += "\n-------------\n";
        }
        return s;
    }

    public String positionID() {
        String s = "";
        for(int i=0; i<9; i++) {
            if(board[i] == EMPTY) {
                s += "_";
                continue;
            }
            s += board[i]==X ? "X" : "O";
        }
        return s;
    }

    public void activatePrint() { printGame = true; }

    public void silentMode() { printGame = false; }
}
