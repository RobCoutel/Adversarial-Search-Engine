package com.gameEngine;

import java.util.Vector;

public interface Board {
    /*
    Note that the constructor must contain the 2 players
    */

    /*
    This function creates and return a deep copy of the object
    */
    public Board clone();

    /*
    This function calls the two players in their turn to play a move until the
    game is over.
    */
    public void play();

    /*
    This function will play the move provided as input
    */
    public void move(Move move);

    /*
    This function comes back one move back
    */
    public void undo();

    /*
    This function returns :
        - 2 if the game is still running
        - 1 if the first player won
        - 0 if it is a draw
        - -1 if the second player won
    */
    public int gameOver();

    /*
    This function returns the vector of all the legal moves in the current
    position
    */
    public Vector<Move> getLegalMoves();

    /*
    This function returns the state of the board. The representation is free as
    long as it fits in a int[]
    */
    public int[] getSquares();

    /*
    This function returns whose turn it is to play
    */
    public int getTurn();

    /*
    This function creates a string representation of the position
    */
    public String toString();

    /*
    This function returns an ID unique to the position
    */
    public String positionID();

    /*
    When this function is called, the board will be printed after each move
    */
    public void activatePrint();

    /*
    When this function is called, the board will no longer be printed after each
    move
    */
    public void silentMode();

}
