package com.agents;

import com.gameEngine.*;

import java.util.Vector;
import java.util.Random;

public class Agent implements Player {
    protected Evaluation evaluation;
    protected int nbNodesExplored;
    protected String name;

    public Agent(String name, Evaluation evaluation) {
        this.name = name;
        this.evaluation = evaluation;
        nbNodesExplored = 0;

    }

    public Move play(Board board) {
        Random rand = new Random();
        Vector<Move> legalMoves = board.getLegalMoves();
        return legalMoves.get(rand.nextInt(legalMoves.size()));
    }

    public String getName() { return name; }

    public Evaluation getEvaluation() { return evaluation; }

}
