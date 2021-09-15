package com.neuralNetworks;

import com.matrix.*;

import java.lang.Math;
import java.lang.IllegalArgumentException;
import java.util.Random;
import java.io.Serializable;

public class GeneticNeuralNetwork extends NeuralNetwork {
/*******************************************************************************
                                 LAMBDA FUNCTIONS
*******************************************************************************/
    private static transient ApplicationFunction breedFunc = new ApplicationFunction() {
        private double crossOverRate = 0.05;
        private double mutationRate = 0.01;
        private double mutationStdev = 0.05;
        Random rand = new Random();
        boolean pickFirst = true;
        public double applyFunc(double x, double y) {
            double r = rand.nextDouble();
            if(r < crossOverRate) {
                pickFirst = !pickFirst;
            }
            double z;
            z = pickFirst ? x : y;
            r = rand.nextDouble();
            if(r < mutationRate) {
                z += rand.nextGaussian() * mutationStdev;
            }
            return z;
        }
    };

  private static transient ApplicationFunction breedHardFunc = new ApplicationFunction() {
        private double crossOverRate = 0.05;
        private double mutationRate = 0.20;
        private double mutationStdev = 0.1;
        Random rand = new Random();
        boolean pickFirst = true;
        public double applyFunc(double x, double y) {
            double r = rand.nextDouble();
            if(r < crossOverRate) {
                pickFirst = !pickFirst;
            }
            double z;
            z = pickFirst ? x : y;
            r = rand.nextDouble();
            if(r < mutationRate) {
                z += rand.nextGaussian() * mutationStdev;
            }
            return z;
        }
    };

  private static transient ApplicationFunction breedExtFunc = new ApplicationFunction() {
        private double crossOverRate = 0.05;
        private double mutationRate = 0.05;
        Random rand = new Random();
        boolean pickFirst = true;
        public double applyFunc(double x, double y) {
            double r = rand.nextDouble();
            if(r < crossOverRate) {
                pickFirst = !pickFirst;
            }
            double z;
            z = pickFirst ? x : y;
            r = rand.nextDouble();
            if(r < mutationRate) {
                z = rand.nextGaussian();
            }
            return z;
        }
    };


/*******************************************************************************
                                GENTETIC LEARNING
*******************************************************************************/
    public GeneticNeuralNetwork(int[] dimensions) {
        super(dimensions);
    }

    public GeneticNeuralNetwork clone() {
        GeneticNeuralNetwork clone = new GeneticNeuralNetwork(dimensions);
        for(int i=0; i<nbLayers; i++) {
            clone.layerWeights[i] = layerWeights[i].clone();
            clone.layerBiases[i] = layerBiases[i].clone();
        }
        return clone;
    }

    public GeneticNeuralNetwork breed(GeneticNeuralNetwork nn, int mode) {
        if(nbLayers != nn.nbLayers) {
            throw new IllegalArgumentException
            ("Error in NeuralNetwork.breed"
            + " : The number of layers of the NeuralNetworks do not match");
        }
        for(int i=0; i<nbLayers+1; i++) {
            if(dimensions[i] != nn.dimensions[i]) {
                throw new IllegalArgumentException
                ("Error in NeuralNetwork.breed"
                + " : The dimensions the NeuralNetworks do not match");
            }
        }

        GeneticNeuralNetwork newNN = this.clone();
        for(int i=0; i<nn.nbLayers; i++) {
            switch(mode){
                case 2 :
                    newNN.layerWeights[i].apply(nn.layerWeights[i], breedExtFunc);
                    newNN.layerBiases[i].apply(nn.layerBiases[i], breedExtFunc);
                case 1 :
                    newNN.layerWeights[i].apply(nn.layerWeights[i], breedHardFunc);
                    newNN.layerBiases[i].apply(nn.layerBiases[i], breedHardFunc);
                default :
                    newNN.layerWeights[i].apply(nn.layerWeights[i], breedFunc);
                    newNN.layerBiases[i].apply(nn.layerBiases[i], breedFunc);
            }
        }

        return newNN;
    }
}
