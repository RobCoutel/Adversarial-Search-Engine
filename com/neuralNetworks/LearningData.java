package com.neuralNetworks;

import com.matrix.*;

import java.lang.Math;
import java.lang.IllegalArgumentException;
import java.util.Random;
import java.io.Serializable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LearningData {
    private int nbData, dataSize, nbPossibleLabels;
    private Matrix[] data;
    private Matrix[] labels;
    private int currIndex;

    public LearningData(String dataPath) {
        this.dataSize = dataSize;
        try {
            // read from scv file
            File myObj = new File(dataPath);
            Scanner myReader = new Scanner(myObj);
            // set up variables
            String currLine = myReader.nextLine();
            String subString = "";
            char currChar = currLine.charAt(currIndex++);
            currIndex = 0;

            // number of data in one element
            nbData = Integer.parseInt(readNumber(currLine));;

            // number of parameters of each data
            dataSize = Integer.parseInt(readNumber(currLine));

            // number of possible labels
            nbPossibleLabels = Integer.parseInt(readNumber(currLine));

            // creating the arrays
            labels = new Matrix[nbData];
            data = new Matrix[nbData];

            for(int i=0; i<nbData && myReader.hasNextLine(); i++) {
                currIndex = 0;
                currLine = myReader.nextLine();

                // extrating the label
                int currLabel = Integer.parseInt(readNumber(currLine));

                double[] auxLabel = new double[nbPossibleLabels];
                auxLabel[currLabel] = 1.0;
                labels[i] = new Matrix(auxLabel);

                // extracting the data
                int j=0;
                double[] auxData = new double[dataSize];
                while(currIndex < currLine.length()) {
                    subString = readNumber(currLine);
                    if(subString == "") { break; }
                    auxData[j++] = Double.parseDouble(subString) / 256;
                }
                data[i] = new Matrix(auxData);
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public Matrix[] getData() { return data; }
    public Matrix[] getLabels() { return labels; }

    private String readNumber(String line) {
        if(currIndex >= line.length()) {
            return "out of bounds : " + Integer.toString(currIndex) + " of " + Integer.toString(line.length());
        }
        String subString = "";
        char currChar = line.charAt(currIndex++);
        // skipping until the next number
        while(currChar > '9' || currChar < '0' && currIndex < line.length()) {
            currChar = line.charAt(currIndex++);
        }

        // collecting the digits
        while(((currChar <= '9' && currChar >= '0') || currChar == '.')
             && currIndex < line.length()) {
            subString += currChar;
            currChar = line.charAt(currIndex++);
        }
        if(currIndex >= line.length()) {
            subString += currChar;
        }
        return subString;
    }

    public LearningData(Matrix[] data, Matrix[] labels) {
        nbData = data.length;
        if(nbData != labels.length) {
            throw new IllegalArgumentException("Error in LearningData constructor : mismatching between labels and data length");
        }
        dataSize = data[0].getHeight();
        nbPossibleLabels = labels[0].getHeight();
        this.data = data;
        this.labels = labels;
    }

    public LearningData generateBatch(int batchSize) {
        Matrix[] batchData = new Matrix[batchSize];
        Matrix[] batchLabels = new Matrix[batchSize];

        Random rand = new Random();

        for(int i=0; i<batchSize; i++) {
            int r = rand.nextInt(nbData);
            batchData[i] = data[r].clone();
            batchLabels[i] = labels[r].clone();
        }
        return new LearningData(batchData, batchLabels);
    }

    public static void main(String[] args) {
        /*int[] dimensions = {1,2,2};
        NeuralNetwork nn = new NeuralNetwork(dimensions);

        double[] inputs = {0.5};
        System.out.println(nn.propagate(inputs)[0]);
        System.out.println(nn.propagate(inputs)[1]);*/

        String dataPath = "mnist_train_small.csv";
        //String dataPath = "test.csv";
        LearningData data = new LearningData(dataPath);

        data = data.generateBatch(5);
        data.data[0].transpose();
        data.labels[0].transpose();

        System.out.println("Number of labels : " + data.nbPossibleLabels);
        System.out.println("Data size : " + data.dataSize);
        System.out.println("Number of Data : " + data.nbData);
        System.out.print(data.data[0]);
        System.out.println("\n");
        System.out.print(data.labels[0]);
        System.out.println("\n");
    }
}
