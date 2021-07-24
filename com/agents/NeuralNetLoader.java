package com.agents;

import com.gameEngine.*;
import com.neuralNetworks.*;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class NeuralNetLoader {
    public static NeuralNetwork loadBestNN(String dataPath, String game) {
        NeuralNetwork neuralNet = null;
        try {
            File myObj = new File(dataPath + "bin/com/agents/NeuralNetParam-" + game + "/lastGeneration.txt");
            Scanner myReader = new Scanner(myObj);
            String lastGenPath = "";
            while (myReader.hasNextLine()) {
                 lastGenPath += myReader.nextLine();
            }
            myReader.close();

            File file = new File(dataPath + lastGenPath);
            System.out.println("\n" + lastGenPath);
            String bestNNPath = dataPath + lastGenPath + file.list()[0];

            FileInputStream fileIn = new FileInputStream(bestNNPath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            neuralNet = (NeuralNetwork) in.readObject();
            in.close();
            fileIn.close();

        } catch (IOException i) {
           i.printStackTrace();
           return null;
        } catch (ClassNotFoundException c) {
           System.out.println("NeuralNetwork class not found");
           c.printStackTrace();
           return null;
        }
        return neuralNet;
    }
}
