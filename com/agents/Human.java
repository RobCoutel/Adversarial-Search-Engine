package com.agents;

import com.gameEngine.*;

import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Human implements Player {
    private String name;
    public Human(String name) {
        this.name = name;
    }

    public Move play(Board board) {
        Vector<Move> vect = board.getLegalMoves();
        while(true) {
            String s = "";
            System.out.println(name + " to move");
            System.out.println("Enter your move here : ");
            try {
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                s = bufferRead.readLine();
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            for(byte i=0; i<vect.size(); i++) {
                if(vect.get(i).toString().equals(s)) {
                    return vect.get(i);
                }
            }
            System.out.println("Illegal move, try again");
            System.out.println("The legal moves are :");
            System.out.println(vect);
        }
    }

    public String getName() {
        return name;
    }

    public Evaluation getEvaluation() {
        return null;
    }
}
