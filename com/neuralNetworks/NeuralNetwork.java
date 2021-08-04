package com.neuralNetworks;

import com.matrix.*;

import java.lang.Math;
import java.lang.IllegalArgumentException;
import java.util.Random;
import java.io.Serializable;

public class NeuralNetwork implements Serializable {

/*******************************************************************************
                                 LAMBDA FUNCTIONS
*******************************************************************************/
    // derivative of the cost function :
    // dC / da_i_L = (a_i_L - label_i)
    private static transient ApplicationFunction costDerivative = new ApplicationFunction() {
        public double applyFunc(double x, double y) {
            return x - y;
        }
    };

    // Activation function
    private static transient MapFunction actFunc = new MapFunction() {
        public double mapFunc(double x) {
            return 1 / (1 + Math.exp(-x));
        }
    };

    // derivative of the activation function :
    // da_i_L / dz_i_L = exp(-z_i_L) / (1 + exp(-z_i_L))^2
    private static transient MapFunction sigmoidDerivative = new MapFunction() {
        public double mapFunc(double x) {
            double exp = Math.exp(-x);
            return exp / ((1 + exp) * (1 + exp));
        }
    };

    // make a step in the gradient direction
    private double step = 0.5;
    private transient ApplicationFunction makeStep = new ApplicationFunction() {
        public double applyFunc(double x, double y) {
            return x - step * y;
        }
    };

    private static transient ApplicationFunction breedFunc = new ApplicationFunction() {
        private double crossOverRate = 0.05;
        private double mutationRate = 0.01;
        private double mutationStdev = 0.5;
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
                z *= rand.nextGaussian() * mutationStdev;
            }
            return z;
        }
    };

    private void updateStep() {
        step *= 0.995;
    }

/*******************************************************************************
                                 CLASS DECLARATION
*******************************************************************************/
    private Matrix[] layerWeights;
    private Matrix[] layerBiases;
    private Matrix[] neuronActivity;
    private Matrix[] neuronLinearComb;
    private int[] dimensions;
    private int nbLayers;
    private static int serialNumberTot = 0;
    private int serialNumber;

    public NeuralNetwork(int[] dimensions) {
        this.dimensions = dimensions;
        nbLayers = dimensions.length - 1;

        neuronActivity = new Matrix[nbLayers + 1];
        neuronLinearComb = new Matrix[nbLayers + 1];

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
        neuronActivity[0] = input.clone();
        for(int i=0; i<nbLayers; i++) {
            neuronLinearComb[i+1] = layerWeights[i].clone();
            neuronLinearComb[i+1].multiply(neuronActivity[i]);
            neuronLinearComb[i+1].add(layerBiases[i]);
            neuronActivity[i+1] = neuronLinearComb[i+1].clone();
            neuronActivity[i+1].map(actFunc);
        }

        return neuronActivity[nbLayers].clone();
    }

    public String toString() {
        String s = "";
        for(int i=0; i<nbLayers; i++) {
            s += "Weights " + i + "\n" + layerWeights[i] + "\n";
            s += "Biases " + i + "\n" + layerBiases[i] + "\n\n";
        }
        return s;
    }

/*******************************************************************************
                            GRADIENT DESCENT LEARNING
*******************************************************************************/
    public void learn(LearningData learningData, int nbBatches) {
        for(int i=0; i<nbBatches; i++) {
            learnBatch(learningData);
        }
    }

    public void learn(String dataPath, int nbBatches) {
        LearningData learningData = new LearningData(dataPath);
        learn(learningData, nbBatches);
    }

    public void learnStocha(LearningData learningData, int batchSize, int nbBatches) {
        for(int i=0; i<nbBatches; i++) {
            learnBatch(learningData.generateBatch(batchSize));
        }
    }

    public void learnStocha(String dataPath, int batchSize, int nbBatches) {
        LearningData learningData = new LearningData(dataPath);
        learnStocha(learningData, batchSize, nbBatches);
    }

    /*
    This function takes as input a two 2D array, for which each line
    represents a traning data, and the output expecte on that training data
    */
    private void learnBatch(LearningData batch) {
        Matrix[] data = batch.getData();
        Matrix[] labels = batch.getLabels();
        int batchSize = data.length;
        Matrix[][] gradientsWeights = new Matrix[nbLayers][batchSize];
        Matrix[][] gradientsBiases = new Matrix[nbLayers][batchSize];

        // compute the gradient for each training data
        for(int i=0; i<batchSize; i++) {
            Matrix[][] gradient = learnOneData(data[i], labels[i]);
            for(int j=0; j<nbLayers; j++) {
                gradientsWeights[j][i] = gradient[0][j];
                gradientsBiases[j][i] = gradient[1][j];
            }
        }

        // compute the averageGradient on the batch
        Matrix[] averageGradWeights = new Matrix[nbLayers];
        Matrix[] averageGradBiases = new Matrix[nbLayers];
        for(int i=0; i<nbLayers; i++) {
            averageGradWeights[i] = Matrix.average(gradientsWeights[i]);
            averageGradBiases[i] = Matrix.average(gradientsBiases[i]);
        }

        for(int i=0; i<nbLayers; i++) {
            updateStep();
            this.layerWeights[i].apply(averageGradWeights[i], makeStep);
            this.layerBiases[i].apply(averageGradBiases[i], makeStep);
        }
    }

    /*
    This function returns the gradient of the data provided on the neural network
    This the first being the dradient on the weights and the second being the
    gradient on the Biases
    */
    private Matrix[][] learnOneData(Matrix inputs, Matrix label) {
        Matrix[] weightsGradient = new Matrix[nbLayers];
        Matrix[] biasesGradient = new Matrix[nbLayers];
        Matrix outputs = propagate(inputs);

        Matrix dCdA = outputs.clone();
        dCdA.apply(label, costDerivative);

        /* derivative with the parameters in back propagation:
        // dz_i_l / dw_ij_(l-1) = a_j_(l-1)
        // dz_i_l / db_i_(l-1) = 1

        // dC / dw_ij_l = dC / da_i_(l+1) * da_i_(l+1) / dz_i_(l+1) * a_j_l
        // dC / db_i_l  = dC / da_i_(l+1) * da_i_(l+1) / dz_i_(l+1) * 1
        */
        for(int i=nbLayers-1; i>=0; i--) {
            // bias gradient
            // dC / db_i_l = (dC / da_i_(l+1)) * (da_i_(l+1) / dz_i_(l+1)) * (dz_i_(l+1) / db_i_l)
            // dC / db_i_l = dCdA * sigmoidDerivative(z_i_(l+1)) * 1
            Matrix dAdz = neuronLinearComb[i+1].clone().map(sigmoidDerivative);
            // update the dCdA and clone the result in biasesGradient
            biasesGradient[i] = dCdA.dotMultiply(dAdz).clone();

            // weights gradient
            // dC / dw_ij_l = (dC / da_i_(l+1)) * (da_i_(l+1) / dz_i_(l+1)) * (dz_i_(l+1) / dw_ij_l)
            // dC / dw_ij_l = dCdA * sigmoidDerivative(z_i_(l+1)) * a_j_l
            // (n_l x 1) * (n_(l-1) x 1)T -> n_l x n_(l-1)
            weightsGradient[i] = biasesGradient[i].clone().multiply(neuronActivity[i].transpose());

            /* dC / da_k_l
            //    = sum_j[
            //          (dC / da_j_(l+1))           -> previous layer
            //        * (da_j_(l+1) / dz_j_(l+1))   -> sigmoidDerivative
            //        * (dz_j_(l+1) / da_k_l)       -> matMultiply with neuronActivity
            //    ]
            */
            //dCdA.dotMultiply(dAdz);
            dCdA = layerWeights[i].clone().transpose().multiply(dCdA);
            //dCdA.sum(1);
            //System.out.println(dCdA.equals(biasesGradient[i].clone().transpose().multiply(layerWeights[i]).transpose()));
            //dCdA = biasesGradient[i].clone();
            //dCdA.transpose().multiply(layerWeights[i]).transpose();
        }
        Matrix[][] toReturn = {weightsGradient, biasesGradient};
        return toReturn;
    }

    public double testAccuracy(LearningData batch) {
        double accuracy = 0;
        double averageCost = 0;
        Matrix[] data = batch.getData();
        Matrix[] labels = batch.getLabels();
        for(int i=0; i<data.length; i++) {
            Matrix output = propagate(data[i]);
            int answer = output.maxIndex()[0];
            int expected = labels[i].maxIndex()[0];
            if(i==0) {
                //System.out.println("Expected : " + expected);
                //System.out.println(output);
                //System.out.println(labels[i]);
            }
            averageCost += cost(output.transpose().getContent()[0], labels[i].transpose().getContent()[0]);
            if(answer == expected) {
                accuracy++;
            }
        }
        averageCost /= data.length;
        System.out.println("Average cost : " + averageCost + "\n");
        accuracy /= data.length;
        return accuracy;
    }

    private double cost(double[] results, double[] labels) {
        double toReturn = 0;
        for(int i=0; i<results.length; i++) {
            toReturn += (labels[i] - results[i]) * (labels[i] - results[i]);
        }
        return toReturn / 2;
    }


/*******************************************************************************
                                GENTETIC LEARNING
*******************************************************************************/
    public NeuralNetwork breed(NeuralNetwork nn) {
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

        NeuralNetwork newNN = this.clone();
        for(int i=0; i<nn.nbLayers; i++) {
            newNN.layerWeights[i].apply(nn.layerWeights[i], breedFunc);
            newNN.layerBiases[i].apply(nn.layerBiases[i], breedFunc);
        }

        return newNN;
    }
}
