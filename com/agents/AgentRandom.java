package com.agents;

import com.gameEngine.*;
import java.util.Vector;
import java.util.Random;

public class AgentRandom implements Player {
    private String name;
    private Random rand;
    public AgentRandom(String name) {
        this.name = name;
        rand = new Random();
    }
    public Move play(Board board) {
        Vector<Move> legalMoves = board.getLegalMoves();
        return legalMoves.get(rand.nextInt(legalMoves.size()));
    }

    public String getName() { return name; }

    public Evaluation getEvaluation() { return null; }
}
