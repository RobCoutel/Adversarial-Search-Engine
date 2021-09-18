// java --class-path bin  com.chess.BenchmarkCorrectness 5
package com.chess;

import java.util.Vector;
import java.lang.Math;

import com.agents.AgentCount;
import com.chess.*;

public class BenchmarkCorrectness {
    public static void main(String[] args) {
        long MEGABYTE = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        double freeMemory = (double) (runtime.totalMemory()/MEGABYTE)
        - (double) (runtime.freeMemory()/MEGABYTE);

        int depth = Integer.parseInt(args[0]);
        long[] shannonNumbers = new long[6];
        String positionID = "";
        if(args.length == 1) {
            positionID = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            shannonNumbers[0] = 20;
            shannonNumbers[1] = 400;
            shannonNumbers[2] = 8902;
            shannonNumbers[3] = 197281;
            shannonNumbers[4] = 4865609;
            shannonNumbers[5] = 119060324;
        }
        else if(args.length == 2) {
            if(args[1].equals("1")) {
                positionID = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
                shannonNumbers[0] = 20;
                shannonNumbers[1] = 400;
                shannonNumbers[2] = 8902;
                shannonNumbers[3] = 197281;
                shannonNumbers[4] = 4865609;
                shannonNumbers[5] = 119060324;
            }
            if(args[1].equals("2")) {
                positionID = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ";
                shannonNumbers[0] = 48;
                shannonNumbers[1] = 2039;
                shannonNumbers[2] = 97862;
                shannonNumbers[3] = 4085603	;
                shannonNumbers[4] = 193690690;
                //shannonNumbers[5] = 8031647685;
            }
            else if(args[1].equals("3")) {
                positionID = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - ";
                shannonNumbers[0] = 14;
                shannonNumbers[1] = 191;
                shannonNumbers[2] = 2812;
                shannonNumbers[3] = 43238;
                shannonNumbers[4] = 674624;
                shannonNumbers[5] = 11030083;
            }
            else if(args[1].equals("4")) {
                positionID = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
                shannonNumbers[0] = 6;
                shannonNumbers[1] = 264;
                shannonNumbers[2] = 9467;
                shannonNumbers[3] = 422333;
                shannonNumbers[4] = 15833292;
                shannonNumbers[5] = 706045033;
            }
            else if(args[1].equals("5")) {
                positionID = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
                shannonNumbers[0] = 44;
                shannonNumbers[1] = 1486;
                shannonNumbers[2] = 62379;
                shannonNumbers[3] = 2103487;
                shannonNumbers[4] = 89941194;
            }
            else if(args[1].equals("6")) {
                positionID = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
                shannonNumbers[0] = 45;
                shannonNumbers[1] = 2079;
                shannonNumbers[2] = 89890;
                shannonNumbers[3] = 3894594;
                shannonNumbers[4] = 16407551;
                //shannonNumbers[5] = 6923051137;
            }
            else {
                System.out.println("Invalid position");
                return;
            }
        }
        else {
            System.out.println("Wrong number of arguments");
            return;
        }



        AgentCount ac = new AgentCount("Count", 0);
        ChessBoard board = new ChessBoard(positionID, ac, ac);
        System.out.println(board);
        System.out.println(board.getLegalMoves());
        boolean correct = true;
        for(int i=0; i<depth; i++) {
            long startTime = System.currentTimeMillis();
            ac = new AgentCount("Count", i);

            board = new ChessBoard(positionID, ac, ac);
            ac.play(board);
            long computed = ac.getNbNodesExplored();
            System.out.println("The shannon number " + i + " is : " + shannonNumbers[i]);
            System.out.println("My calculation is : " + computed);
            double newFreeMemory = (double) (runtime.totalMemory()/MEGABYTE)
            - (double) (runtime.freeMemory()/MEGABYTE);
            System.out.println("Memory used : " + (newFreeMemory - freeMemory) + "MB");

            System.out.println("Time for this iteration : "
            + ((double)(System.currentTimeMillis() - startTime))/1000 + " sec");
            System.out.println("Speed : "
            + (Math.ceil(1000*((double) ac.getNbNodesExplored()/(System.currentTimeMillis() - startTime))))
            + " nodes / sec\n");
            board = null;
            correct = correct && shannonNumbers[i] == computed;
            System.gc();
        }
        if(correct) {
            System.out.println("\nThe computation is correct!!");
        }
        else {
            System.out.println("\n/!\\ There is a mistake!!");
        }
    }
}
