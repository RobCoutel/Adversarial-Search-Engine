package com.neuralNetworks;

import com.matrix.*;

import java.lang.Math;
import java.lang.IllegalArgumentException;
import java.util.Random;
import java.io.Serializable;

public class LearningNeuralNetwork extends NeuralNetwork {
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

    // derivative of the activation function :
    // da_i_L / dz_i_L = exp(-z_i_L) / (1 + exp(-z_i_L))^2
    private static transient MapFunction sigmoidDerivative = new MapFunction() {
        public double mapFunc(double x) {
            double exp = Math.exp(-x);
            return exp / ((1 + exp) * (1 + exp));
        }
    };

    // make a step in the gradient direction
    private double step = 0.15;
    private transient ApplicationFunction makeStep = new ApplicationFunction() {
        public double applyFunc(double x, double y) {
            return x - step * y;
        }
    };

    private void updateStep() {
        step = 0.1;
        /*if(lastAccuracy > accuracy) {
            step *= decay;
        }
        else if (step < 0.15) {
            step *= gain;
        }*/
        //step = 0.15 * (1 - accuracy);
    }

/*******************************************************************************
                                 CLASS DECLARATION
*******************************************************************************/
    protected Matrix[] nActivity;
    protected Matrix[] nLinComb;
    private double accuracy;
    private double lastAccuracy;
    private final double decay = 0.75;
    private final double gain = 1.1;


    public LearningNeuralNetwork(int[] dimensions) {
        super(dimensions);
        nActivity = new Matrix[nbLayers + 1];
        nLinComb = new Matrix[nbLayers + 1];
        accuracy = 0;
        lastAccuracy = 0;
    }

/*******************************************************************************
                            GRADIENT DESCENT LEARNING
*******************************************************************************/
    // overwrite the method, to store additional data
    public Matrix propagate(Matrix input) {
        nActivity[0] = input.clone();
        for(int i=0; i<nbLayers; i++) {
            nLinComb[i+1] = layerWeights[i].clone();
            nLinComb[i+1].multiply(nActivity[i]).add(layerBiases[i]);
            nActivity[i+1] = nLinComb[i+1].clone().map(sigmoid);
        }

        return nActivity[nbLayers].clone();
    }

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

        accuracy = 0;
        // compute the gradient for each training data
        for(int i=0; i<batchSize; i++) {
            Matrix[][] gradient = learnOneData(data[i], labels[i]);
            for(int j=0; j<nbLayers; j++) {
                gradientsWeights[j][i] = gradient[0][j];
                gradientsBiases[j][i] = gradient[1][j];
            }
        }
        accuracy /= batchSize;
        updateStep();
        lastAccuracy = accuracy;

        // compute the averageGradient on the batch
        Matrix[] averageGradWeights = new Matrix[nbLayers];
        Matrix[] averageGradBiases = new Matrix[nbLayers];
        for(int i=0; i<nbLayers; i++) {
            averageGradWeights[i] = Matrix.average(gradientsWeights[i]);
            averageGradBiases[i] = Matrix.average(gradientsBiases[i]);
        }

        for(int i=0; i<nbLayers; i++) {
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
        Matrix[] wGrad = new Matrix[nbLayers];
        Matrix[] bGrad = new Matrix[nbLayers];
        Matrix output = propagate(inputs);
        if(output.maxIndex()[0] == label.maxIndex()[0]) {
            accuracy += 1;
        }

        Matrix dCdA = output.clone();
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
            Matrix z_next = nLinComb[i+1];
            Matrix a = nActivity[i];

            Matrix dAdz = z_next.clone().map(sigmoidDerivative);
            // update the dCdA and clone the result in bGrad
            bGrad[i] = dCdA.dotMultiply(dAdz).clone();

            /* weights gradient
               dC / dw_ij_l = (dC / da_i_(l+1)) * (da_i_(l+1) / dz_i_(l+1)) * (dz_i_(l+1) / dw_ij_l)
               dC / dw_ij_l = dCdA * sigmoidDerivative(z_i_(l+1)) * a_j_l
               (n_l x 1) * (n_(l-1) x 1)T -> n_l x n_(l-1)
            */
            wGrad[i] = bGrad[i].clone().multiply(a.transpose());

            /* dC / da_k_l
            //    = sum_j[
            //          (dC / da_j_(l+1))           -> previous layer
            //        * (da_j_(l+1) / dz_j_(l+1))   -> sigmoidDerivative
            //        * (dz_j_(l+1) / da_k_l)       -> matMultiply with nActivity
            //    ]
            */
            dCdA = layerWeights[i].clone().transpose().multiply(dCdA);
        }
        Matrix[][] toReturn = {wGrad, bGrad};
        return toReturn;
    }

    public double testAccuracy(LearningData batch) {
        double acc = 0;
        double averageCost = 0;
        Matrix[] data = batch.getData();
        Matrix[] labels = batch.getLabels();
        for(int i=0; i<data.length; i++) {
            Matrix output = propagate(data[i]);
            int answer = output.maxIndex()[0];
            int expected = labels[i].maxIndex()[0];
            //averageCost += cost(output.transpose().getContent()[0], labels[i].transpose().getContent()[0]);
            if(answer == expected) {
                acc++;
            }
        }
        acc /= data.length;
        return acc;
    }

    private double cost(double[] results, double[] labels) {
        if(results.length != labels.length) {
            String errMsg = "The length of the two inputs is not the same (";
            errMsg += results.length + ", " + labels.length + ")";
            throw new IllegalArgumentException(errMsg);
        }
        double toReturn = 0;
        for(int i=0; i<results.length; i++) {
            toReturn += (labels[i] - results[i]) * (labels[i] - results[i]);
        }
        return toReturn / 2;
    }
}
