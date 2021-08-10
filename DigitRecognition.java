//javac -d bin DigitRecognition.java
//java --class-path bin DigitRecognition 1

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

import com.neuralNetworks.*;
import com.matrix.*;


public class DigitRecognition {
    public static void saveNN(NeuralNetwork nn, String path) {
        try {
            Date date = new Date();
            String fullPath = path + "Generation-" + Long.toString((date.getTime()/1000)%31536000) + "/";
            File f1 = new File(fullPath);
            if(!f1.mkdir()) {
                System.out.println(fullPath);
                System.out.println("!!!!!!!!!!!!!!!!!!\nFILE FAILED TO BE CREATED\n!!!!!!!!!!!!!!!!!!!");
            }
            FileWriter myWriter = new FileWriter(path + "lastGeneration.txt");
            myWriter.write(fullPath);
            myWriter.close();

            fullPath += "NeuralNet";
            FileOutputStream fileOut = new FileOutputStream(fullPath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(nn);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    public static void main(String[] args) {
        NeuralNetwork nn;
        LearningData data;
        int nbBatches, nbIteration, batchSize;
        if(args.length == 0 || Integer.parseInt(args[0]) == 0) {
            int[] dimensions = {784, 10, 10, 10};

            String path = "mnist_train_small.csv";
            nn = new NeuralNetwork(dimensions);
            data = new LearningData(path);

            nbBatches = 2000;
            nbIteration = 50;
            batchSize = 200;
        }
        else {
            int[] dimensions = {1, 4, 4};
            String path = "test_data.csv";

            /*double[][] weightsArray = {{-2}, {2}};
            double[][] biasesArray = {{1}, {-1}};
            Matrix[] weights = new Matrix[1];
            Matrix[] biases = new Matrix[1];
            weights[0] = new Matrix(weightsArray);
            biases[0] = new Matrix(biasesArray);
            nn = new NeuralNetwork(weights, biases);*/
            nn = new NeuralNetwork(dimensions);

            /*double[] input0 = {0};
            System.out.println("Output 0 : " + nn.propagate(new Matrix(input0)));
            double[] input1 = {1};
            System.out.println("Output 1 : " + nn.propagate(new Matrix(input1)));*/

            data = new LearningData(path);

            nbBatches = 10000;
            nbIteration = 10;
            batchSize = 20;
        }

        System.out.println("Accuracy before training : " + nn.testAccuracy(data.generateBatch(batchSize)));

        for(int i=0; i<nbIteration; i++) {
            nn.learnStocha(data, batchSize, nbBatches);
            System.out.println("Accuracy after training  " + i + " : "
             + nn.testAccuracy(data.generateBatch(batchSize)));
        }

        saveNN(nn, "");
    }
}
