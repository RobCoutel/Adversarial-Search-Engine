package com.tournament;

import com.gameEngine.Board;
import com.gameEngine.Player;
import com.ticTacToe.*;
import com.chess.*;

import java.lang.IllegalArgumentException;
import java.util.concurrent.Semaphore;

public class Match extends Thread {
    private Player p1, p2;
    private Board board;
    private double resultP1, resultP2;
    private Semaphore lock;
    private Semaphore externalLock = null;
    private long endTime = 0;
    private String game;

    // TODO open window to watch the match

    public Match(Player p1, Player p2, String game) {
        if(!(game.equals("Chess") || game.equals("TicTacToe"))) {
            throw new IllegalArgumentException("Error in Match.<init> : The game is unknown");
        }
        this.p1 = p1;
        this.p2 = p2;
        this.game = game;
        resultP1 = 0;
        resultP2 = 0;

        this.lock = new Semaphore(1, false);
        try {
            lock.acquire();
        } catch(InterruptedException e) {}
    }

    public Match(Player p1, Player p2, String game, Semaphore externalLock) {
        this(p1, p2, game);
        this.externalLock = externalLock;
    }

    public Player getPlayer1() { return p1; }
    public Player getPlayer2() { return p2; }
    public double getTimeScore() { return endTime / 3600000.0; }

    public void run() {
        long startTime = System.currentTimeMillis();
        if(game.equals("Chess")) {
            board = new ChessBoard(p1, p2);
        }
        if(game.equals("TicTacToe")) {
            board = new TTTBoard(p1, p2);
        }
        board.play();
        int matchRes = board.gameOver();
        resultP1 += 1 + matchRes;
        resultP2 += 1 - matchRes;

        if(game.equals("Chess")) {
            board = new ChessBoard(p2, p1);
        }
        if(game.equals("TicTacToe")) {
            board = new TTTBoard(p2, p1);
        }
        board.play();
        matchRes = board.gameOver();
        resultP1 += 1 - matchRes;
        resultP2 += 1 + matchRes;

        endTime = (System.currentTimeMillis() - startTime);

        /*
        long hours = endTime / 3600000;
        long minutes = (endTime % 3600000) / 60000;
        long seconds = (endTime % 60000) / 1000;
        long miliSec = endTime % 1000;


        System.out.println("Time for the Match : "
        + hours + "h " + minutes + "min " + seconds + "sec " + miliSec + "ms\n"
        + p1.getName() + "    " + Integer.toString(resultP1) + " - "
        + Integer.toString(resultP2) + "    " + p2.getName() + " \n");
        */

        lock.release();

        if(externalLock != null){
           externalLock.release();
        }
    }

    public double getP1Result() {
        double toReturn = 0;
        try {
            lock.acquire();
            toReturn = resultP1;
            lock.release();
        }
        catch(InterruptedException e) {
        }
        return resultP1;
    }
    public double getP2Result() {
        double toReturn = 0;
        try {
            lock.acquire();
            toReturn = resultP2;
            lock.release();
        }
        catch(InterruptedException e) {}
        return toReturn;
    }
}
