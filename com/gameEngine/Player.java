package com.gameEngine;

public interface Player {
    /*
    This function takes a position as input and returns a legal move that will
    be played;
    */
    public Move play(Board board);

    /*
    Returns the name of the player
    */
    public String getName();

    /*
    Returns the evaluation function used by the player. If none are used, returns
    null
    */
    public Evaluation getEvaluation();
}
