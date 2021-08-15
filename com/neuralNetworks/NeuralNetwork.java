package com.neuralNetworks;

import com.matrix.*;

import java.lang.Math;
import java.lang.IllegalArgumentException;
import java.io.Serializable;

public class NeuralNetwork implements Serializable {

/*******************************************************************************
                                 LAMBDA FUNCTIONS
*******************************************************************************/
    // Activation function
    protected static transient MapFunction sigmoid = new MapFunction() {
        public double mapFunc(double x) {
            return 1 / (1 + Math.exp(-x));
        }
    };

/*******************************************************************************
                                 CLASS DECLARATION
*******************************************************************************/
    protected Matrix[] layerWeights;
    protected Matrix[] layerBiases;
    protected int[] dimensions;
    protected int nbLayers;
    private static int serialNumberTot = 0;
    private int serialNumber;

    public NeuralNetwork(int[] dimensions) {
        this.dimensions = dimensions;
        nbLayers = dimensions.length - 1;

        layerWeights = new Matrix[nbLayers];
        layerBiases = new Matrix[nbLayers];
        double mul = 2.0 / Math.sqrt((double) dimensions[0]);
        for(int i=0; i<nbLayers; i++) {
            // generate the layers
            layerWeights[i] = new Matrix(dimensions[i+1], dimensions[i]);
            layerBiases[i] = new Matrix(dimensions[i+1], 1);
            // fills them with random values
            //double mul = 1/Math.sqrt(dimensions[0]);
            layerWeights[i].randomFillGauss(0, mul);
            layerBiases[i].randomFillGauss(0, mul);
        }
        serialNumber = serialNumberTot++;
    }

    public NeuralNetwork(Matrix[] weights, Matrix[] biases) {
        if(weights.length != biases.length) {
            System.out.println("Incompatible length (in NeuralNetwork(Matrix[] weights, Matrix[] biases))");
        }
        nbLayers = weights.length;
        layerWeights = weights.clone();
        layerBiases = biases.clone();
        serialNumber = serialNumberTot++;

        dimensions = new int[nbLayers + 1];
        dimensions[0] = weights[0].getHeight();
        for(int i=1; i<nbLayers; i++) {
            dimensions[i] = weights[i-1].getWidth();
        }
    }

    public NeuralNetwork clone() {
        NeuralNetwork clone = new NeuralNetwork(dimensions);
        for(int i=0; i<nbLayers; i++) {
            clone.layerWeights[i] = layerWeights[i].clone();
            clone.layerBiases[i] = layerBiases[i].clone();
        }
        return clone;
    }

    public int[] getDimensions() { return dimensions; }
    public int getSerialNumber() { return serialNumber; }

    public Matrix propagate(Matrix input) {
        Matrix output = input.clone();
        for(int i=0; i<nbLayers; i++) {
            output.multiplyLeft(layerWeights[i]).add(layerBiases[i]);
            output.map(sigmoid);
        }

        return output;
    }

    public String toString() {
        String s = "";
        for(int i=0; i<nbLayers; i++) {
            s += "Weights " + i + "\n" + layerWeights[i] + "\n";
            s += "Biases " + i + "\n" + layerBiases[i] + "\n\n";
        }
        return s;
    }
}
