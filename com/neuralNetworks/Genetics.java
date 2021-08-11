/*
command to run this main() :
    java --class-path bin com/neuralNetworks/Genetics <generation size> <nb generations> <depth> <nb cores> <nb saved> <load> <game>
    java --class-path bin com/neuralNetworks/Genetics 128 10 1 4 8 0 TicTacToe
*/

package com.neuralNetworks;

import com.gameEngine.*;
import com.matrix.*;
import com.agents.*;
import com.ticTacToe.*;
import com.chess.*;
import com.tournament.Tournament;

import java.lang.Math;
import java.util.Date;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.Random;
import java.lang.IllegalArgumentException;

public class Genetics {
    private int generationSize;
    private GeneticNeuralNetwork[] neuralNets;
    private Player[] players;
    private int coreLimit = 8;
    private int saveNb;
    private int[] dimensions;
    private String game;
    private String nnpath;

    public Genetics(int generationSize, int[] dimensions, String game) {
        this(generationSize, dimensions, false, game);
    }

    public Genetics(int generationSize, int[] dimensions, boolean loadLastGen, String game) {
        if(!(game.equals("Chess") || game.equals("TicTacToe"))) {
            throw new IllegalArgumentException("Error in Genetics.<init> : The game is unknown");
        }
        this.generationSize = generationSize;
        this.dimensions = dimensions;
        this.game = game;
        nnpath = "bin/com/agents/NeuralNetParam-" + game + "/";
        neuralNets = new GeneticNeuralNetwork[generationSize];

        int i=0;

        if(loadLastGen) {
            try {
                File myObj = new File(nnpath + "lastGeneration.txt");
                Scanner myReader = new Scanner(myObj);
                String lastGenPath = "";
                while (myReader.hasNextLine()) {
                     lastGenPath += myReader.nextLine();
                }
                myReader.close();

                File file = new File(lastGenPath);
                String[] paths = file.list();
                for(int j=0; j<paths.length; j++) {
                    paths[j] = lastGenPath + "/" + paths[j];
                }
                i = paths.length;

                loadGeneration(paths);

            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

        }

        for(; i<generationSize; i++){
            neuralNets[i] = new GeneticNeuralNetwork(dimensions);
        }
    }

    public Genetics(int generationSize, int[] dimensions, boolean loadLastGen, int coreLimit, int saveNb, String game){
        this(generationSize, dimensions, loadLastGen, game);
        this.coreLimit = coreLimit;
        this.saveNb = saveNb;
    }

    private int nextPower2(int n) {
        return (int) Math.pow(2, (int) Math.ceil(Math.log(n) / Math.log(2)));
    }

    public void loadGeneration(String[] filePath) {
        try {
            int nbNeuralNets = filePath.length;
            for(int i=0; i<nbNeuralNets; i++) {
                FileInputStream fileIn = new FileInputStream(filePath[i]);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                neuralNets[i] = (GeneticNeuralNetwork) in.readObject();
                in.close();
                fileIn.close();
            }
        } catch (IOException i) {
           i.printStackTrace();
           return;
        } catch (ClassNotFoundException c) {
           System.out.println("GeneticNeuralNetwork class not found");
           c.printStackTrace();
           return;
        }
    }

    public void saveGeneration(String path) {
        try {
            Date date = new Date();
            String fullPath = path + nnpath + "Generation-" + Long.toString((date.getTime()/1000)%31536000) + "/";
            File f1 = new File(fullPath);
            if(!f1.mkdir()) {
                System.out.println(fullPath);
                System.out.println("!!!!!!!!!!!!!!!!!!\nFILE FAILED TO BE CREATED\n!!!!!!!!!!!!!!!!!!!");
            }
            FileWriter myWriter = new FileWriter(path + nnpath + "lastGeneration.txt");
            myWriter.write(fullPath);
            myWriter.close();

            fullPath += "NeuralNet-";
            for(int i=0; i<saveNb && i<generationSize; i++) {
                String filePath = fullPath + Integer.toString(i) + ".ser";
                FileOutputStream fileOut = new FileOutputStream(filePath);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(neuralNets[i]);
                out.close();
                fileOut.close();
            }
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public void nextGen(int budget, boolean save){
        // create the players with the neural nets
        System.out.println("Number of players : " + generationSize);
        Player[] players = new Player[nextPower2(generationSize)];

        for(int i=0; i<generationSize; i++){
            String id = Integer.toString(neuralNets[i].getSerialNumber());
            // the first quart will have (i / (generationSize/4)) = 0
            // since it is an integer division
            switch (i / (generationSize/4)) {
                case 0:
                    id += " *Surv";
                    break;
                default:
                    id += " *breed";
            }
            if(game.equals("TicTacToe")) {
                players[i] = new AgentMinimax("Budget neural - " + id,
                                            budget,
                                            new TTTEvaluationNeural(neuralNets[i]));
            }
            if(game.equals("Chess")) {
                players[i] = new AgentMinimax("Minimax neural - " + id,
                                            budget,
                                            new ChessEvaluationNeural(neuralNets[i]));
            }
        }
        // make them play in the tournament
        Tournament tour = new Tournament(players, coreLimit, game);
        players = tour.play();
        // recover the neural networks
        for(int i=0; i<generationSize; i++) {
            neuralNets[i] = (GeneticNeuralNetwork)
                            ((NeuralEvaluation)
                            players[i].getEvaluation()).getNeuralNet();
        }
        double[] results = tour.getResults();

        if(save) {
            saveGeneration("");
        }
        // breed the last generation
        breedGen(results);
    }

    private void breedGen(double[] results) {
        GeneticNeuralNetwork[] newGen = new GeneticNeuralNetwork[generationSize];
        int nbSurvivors = generationSize/4;
        // compute the total of the results for the breeding proba;
        double resTot = 0;
        for(int i=0; i<results.length; i++) {
            resTot += results[i];
        }
        Random rand = new Random();
        for(int i=0; i<nbSurvivors; i++) {
            newGen[i] = neuralNets[i];
        }
        for(int i=nbSurvivors; i<generationSize; i++) {
            newGen[i] = breedOne(results, resTot, rand);
        }
        neuralNets = newGen;
    }

    private GeneticNeuralNetwork breedOne(double[] results, double resTot, Random rand) {
        double r1 = rand.nextDouble() * resTot;
        double r2 = rand.nextDouble() * resTot;
        GeneticNeuralNetwork nn1 = null;
        GeneticNeuralNetwork nn2 = null;
        double tmp = 0;
        for(int i=0; i<neuralNets.length; i++) {
            tmp += results[i];
            if(nn1 == null && r1 < tmp) {
                nn1 = neuralNets[i];
            }
            if(nn2 == null && r2 < tmp) {
                nn2 = neuralNets[i];
            }
            if(nn1!= null && nn2 !=null) {
                break;
            }
        }
        return nn1.clone().breed(nn2);
    }

    public static void main(String[] args){
        int generationSize = 128;

        int numberOfGens = 1;
        int depth = 1;
        int load = 1;
        int coreLimit = 8;
        int saveNb = 1;
        String game = "TicTacToe";
        // No breaks on purpose
        switch (args.length) {
            case 7 :
            game = args[6];
            case 6 :
            load = Integer.parseInt(args[5]);
            case 5 :
            saveNb = Integer.parseInt(args[4]);
            case 4 :
            coreLimit = Integer.parseInt(args[3]);
            coreLimit = coreLimit > 0 ? coreLimit : 1;
            case 3 :
            depth = Integer.parseInt(args[2]);
            depth = depth > 0 ? depth : 1;
            case 2 :
            numberOfGens = Integer.parseInt(args[1]);
            numberOfGens = numberOfGens >= 0 ? numberOfGens : 1;
            case 1 :
            generationSize = Integer.parseInt(args[0]);
        }

        System.out.println("You chose the following parameters : " + "\n" +
                          "Number of players : " + generationSize + "\n" +
                          "Number of generations : " + numberOfGens + "\n" +
                          "Depth of search : " + depth + "\n" +
                          "Corelimit : " + coreLimit + "\n" +
                          "Number of NN saved per 10 rounds : " + saveNb + "\n" +
                          "Load existing NN : " + load + "\n" +
                          "\n If you're sure press '1' to confirm\n");

        //Waiting for comfirmation
        if(args.length == 0 && new Scanner(System.in).nextInt() != 1){
            System.exit(-1);
        }

        int[] dimensions;
        if(game.equals("TicTacToe")) {
            dimensions = new int[3];
            dimensions[0] = TTTEvaluationNeural.getNbInputs();
            dimensions[1] = 10;
            dimensions[2] = 1;
        }
        else if(game.equals("Chess")) {
            dimensions = new int[4];
            dimensions[0] = ChessEvaluationNeural.getNbInputs();
            dimensions[1] = 50;
            dimensions[2] = 50;
            dimensions[3] = 1;
        }
        else {
            System.out.println("Invalid game");
            return;
        }

        Genetics genetics = new Genetics(generationSize, dimensions, load==1?true:false, coreLimit, saveNb, game);

        for(int i = 1; i <= numberOfGens; i++){
            System.out.println("--------------------------\n  GENERATION " + Integer.toString(i+1) +
                               " out of " + Integer.toString(numberOfGens) + "\n--------------------------");
            if(i%100 == 0 || i == numberOfGens) {
                genetics.nextGen(depth, true);
            }
            else {
                genetics.nextGen(depth, false);
            }
        }
    }
}
