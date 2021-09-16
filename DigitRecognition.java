//java --class-path bin DigitRecognition load 200 50 200
//java --class-path bin DigitRecognition <load> <nbBatches> <nbIteration> <batchSize>

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
    public static final String nnPath = "bin/DigitNeuralNet/";

    public static void saveNN(LearningNeuralNetwork nn, String path) {
        try {
            Date date = new Date();
            String fullPath = path + nnPath;
            fullPath += "NeuralNet-" + Long.toString((date.getTime()/1000)%31536000) + ".ser";
            FileWriter myWriter = new FileWriter(path + nnPath + "lastNeuralNet.txt");
            myWriter.write(fullPath);
            myWriter.close();

            nn.saveNeuralNetwork(fullPath);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static LearningNeuralNetwork loadNN() {
        try {
            File myObj = new File("bin/DigitNeuralNet/" + "lastNeuralNet.txt");
            Scanner myReader = new Scanner(myObj);
            String lastNNpath = "";
            while (myReader.hasNextLine()) {
                 lastNNpath += myReader.nextLine();
            }
            myReader.close();

            return LearningNeuralNetwork.loadNeuralNetwork(lastNNpath);

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        LearningNeuralNetwork nn;
        LearningData data;
        int nbBatches, nbIteration, batchSize;

        int[] dimensions = {784, 10, 10, 10};

        String path = "mnist_train_small.csv";
        nn = new LearningNeuralNetwork(dimensions);
        data = new LearningData(path);

        if(args.length > 0 && args[0].equals("load")) {
            System.out.println("Loading NeuralNetwork");
            nn = loadNN();
        }
        nbBatches = 200;
        if(args.length > 1) {
            nbBatches = Integer.parseInt(args[1]);
        }
        nbIteration = 50;
        if(args.length > 2) {
            nbIteration = Integer.parseInt(args[2]);
        }
        batchSize = 200;
        if(args.length > 3) {
            batchSize = Integer.parseInt(args[3]);
        }

        System.out.println("Accuracy before training : "
            + nn.testAccuracy(data.generateBatch(batchSize))*100 + "%");

        for(int i=0; i<nbIteration; i++) {
            nn.learnStocha(data, batchSize, nbBatches);
            System.out.println("Accuracy after training  " + (i+1)*nbBatches + " : "
             + nn.testAccuracy(data.generateBatch(batchSize))*100 + "%");
        }

        saveNN(nn, "");
    }
}
