package com.gameEngine;

public interface Move extends Comparable<Move> {
    public int getDestination();

    public boolean isResignation();

    public int compareTo(Move move);

    public String toString();

    public boolean equals(Move move2);

    public boolean isThreat();
}
