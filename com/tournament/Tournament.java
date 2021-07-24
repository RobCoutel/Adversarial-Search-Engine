package com.tournament;

import com.gameEngine.Board;
import com.gameEngine.Player;
import com.agents.Agent;
import com.ticTacToe.*;
import com.chess.*;

import java.util.concurrent.Semaphore;
import java.lang.Math;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

public class Tournament {
    private int nbPlayers;
    private Player[] players;
    private double[] results;
    private String game;
    private Semaphore lock = null;

    public static void main(String[] args) {
        int nbPlayers = 16;
        String game = "TicTacToe";
        Player[] players = new Player[nbPlayers];
        for(int i=0; i<nbPlayers; i++) {
            players[i] = new Agent("Random " + Integer.toString(i), new TTTEvaluation());
        }

        Tournament tournament = new Tournament(players, game);
        Player[] standings = tournament.play();
    }

    public Tournament(Player[] players, String game) {
        if(!(game.equals("Chess") || game.equals("TicTacToe"))) {
            throw new IllegalArgumentException("Error in Match.<init> : The game is unknown");
        }
        this.game = game;
        this.players = players;
        shuffle();
        nbPlayers = players.length;
        results = new double[nbPlayers];
        for(int i=0; i<nbPlayers; i++) {
            results[i] = 0;
        }
    }

    public Tournament(Player[] players, int coreLimit, String game){
        this(players, game);

        lock = new Semaphore(coreLimit, false);
    }

    public double[] getResults() { return results; }

    public Player[] play() {
        int nbMatches = nbPlayers/2;
        int nbRounds = (int) (Math.log(nbMatches) / Math.log(2)) + 1;
        int bracketSize = nbPlayers/2;
        int roundNumber = 0;
        while(bracketSize != 0) {
            System.out.println("ROUND " + Integer.toString(++roundNumber) +
                               " out of " + nbRounds);
            for(int i=0; i<nbPlayers && i<8; i++) {
                System.out.print(players[i].getName() + "\t\t");
                System.out.println(results[i]);

            }
            System.out.println("\n");
            Match[] matches = generateMatches(bracketSize);
            for(int i=0; i<nbMatches; i++) {
                if(lock != null){
                    try{
                        lock.acquire();
                    } catch(InterruptedException e){
                        System.out.println("One match has been interrupted unexpectedly");
                    }
                }
                matches[i].start();
                System.out.print("Matches : " + (i+1) + " out of " + nbMatches + "\t\t\t\r");
            }
            System.out.println("\n");
            int j = 0;
            for(int i=0; i<nbMatches; i++) {
                results[j] += matches[i].getP1Result();
                results[j+bracketSize] += matches[i].getP2Result();
                j++;
                if(j%bracketSize == 0) {
                    j += bracketSize;
                }
            }
            this.sortPlayers();
            bracketSize /= 2;
        }
        System.out.println("FINALS RESULTS ");
        for(int i=0; i<nbPlayers; i++) {
            System.out.print(players[i].getName() + "\t\t");
            System.out.println(results[i]);
        }
        return players;
    }

    private Match[] generateMatches(int bracketSize) {
        int nbMatches = nbPlayers/2;
        Match[] matches = new Match[nbMatches];
        int j = 0;
        for(int i=0; i<nbMatches; i++) {
            matches[i] = new Match(players[j], players[j+bracketSize], game, lock);
            j++;
            if(j%bracketSize == 0) {
                j += bracketSize;
            }
        }
        return matches;
    }

    private void sortPlayers() {
        for(int i=0; i<nbPlayers-1; i++) {
            for(int j=i+1; j<nbPlayers; j++) {
                if (results[i] < results[j]) {
                    double aux1 = results[i];
                    Player aux2 = players[i];
                    results[i] = results[j];
                    players[i] = players[j];
                    results[j] = aux1;
                    players[j] = aux2;
                }
            }
        }
    }

    private void shuffle(){
        List<Player> shfl = Arrays.asList(players);
        Collections.shuffle(shfl);
        players = shfl.stream().toArray(Player[] ::new);
        return;
    }
}
