package com.matrix;

import java.lang.IllegalArgumentException;
import java.util.Random;
import java.io.Serializable;

public class Matrix implements Serializable {
    private int width, height;
    private double[] content;

    /*
    Creates a matrix of zeros of size [height x width]
    */
    public Matrix(int height, int width) {
        this.width = width;
        this.height = height;
        content = new double[height*width];
    }

    /*
    Creates a matrix with the content provided as argument
    */
    public Matrix(double[][] content) {
        height = content.length;
        width = content[0].length;
        for(int i=1; i<height; i++) {
            if (content[i].length != width) {
                throw new IllegalArgumentException("The lines of the matrix are of different lengths");
            }
        }
        this.content = new double[height*width];
        for(int i=0; i<height*width; i++) {
            this.content[i] = content[i/width][i%width];
        }
    }

    /*
    Creates a matrix of size [height x width] with the content provided.
    */
    public Matrix(double[] content, int height, int width) {
        if(content.length != height*width) {
            throw new IllegalArgumentException("The content of the matrix is incopatible with the provided dimensions");
        }
        this.height = height;
        this.width = width;
        this.content = new double[height*width];
        for(int i=0; i<height*width; i++) {
            this.content[i] = content[i];
        }
    }

    /*
    Creates a matrix of size [content.length x 1]
    with the content provided as argument
    */
    public Matrix(double[] content) {
        this(content, content.length, 1);
    }

    /*
    Creates a matrix of ones of size [content.length x 1]
    */
    public static Matrix ones(int height, int width) {
        Matrix toReturn = new Matrix(height, width);
        for(int i=0; i<height*width; i++) {
            toReturn.content[i] = 1;
        }
        return toReturn;
    }

    public static Matrix eye(int diag) {
        Matrix toReturn = new Matrix(diag, diag);
        for(int i=0; i<diag; i++) {
            toReturn.content[i*(diag+1)] = 1;
        }
        return toReturn;
    }

    public Matrix clone() {
        return new Matrix(content, height, width);
    }

    public double getElement(int i, int j) { return content[i*width + j]; }
    public double[][] getContent() {
        double[][] toReturn = new double[height][width];
        for(int i=0; i<height*width; i++) {
            toReturn[i/width][i%width] = content[i];
        }
        return toReturn;
    }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public Matrix randomFill(double rangeMin, double rangeMax) {
        Random r = new Random();
        for(int i=0; i<height*width; i++) {
            content[i] = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        }
        return this;
    }

    public Matrix randomFillGauss(double mean, double stdev) {
        Random r = new Random();
        for(int i=0; i<height*width; i++) {
            content[i] = mean + stdev * r.nextGaussian();
        }
        return this;
    }

    public Matrix add(Matrix mat) {
        if((width != mat.width) || (height != mat.height)) {
            String errMsg = "Error in Matrix.add : The dimensions of the matrices do not match\n";
            errMsg += "The origin matrix is " + Integer.toString(height) + "x" + Integer.toString(width);
            errMsg += " and the other is " + Integer.toString(mat.height) + "x" + Integer.toString(mat.width);
            throw new IllegalArgumentException(errMsg);
        }
        for(int i=0; i<height*width; i++) {
            content[i] += mat.content[i];
        }
        return this;
    }

    public Matrix dotMultiply(Matrix mat) {
        if((width != mat.width) || (height != mat.height)) {
            String errMsg = "Error in Matrix.dotMultiply : The dimensions of the matrices do not match\n";
            errMsg += "The origin matrix is " + Integer.toString(height) + "x" + Integer.toString(width);
            errMsg += " and the other is " + Integer.toString(mat.height) + "x" + Integer.toString(mat.width);
            throw new IllegalArgumentException(errMsg);
        }
        for(int i=0; i<height*width; i++) {
            content[i] *= mat.content[i];
        }
        return this;
    }

    // multiply this * mat
    public Matrix multiply(Matrix mat) {
        if(width != mat.height) {
            String errMsg = "Error in Matrix.multiply : The dimensions of the matrices do not match\n";
            errMsg += "The origin matrix is " + Integer.toString(height) + "x" + Integer.toString(width);
            errMsg += " and the other is " + Integer.toString(mat.height) + "x" + Integer.toString(mat.width);
            throw new IllegalArgumentException(errMsg);
        }

        int newHeight = height;
        int newWidth = mat.width;
        double[] product = new double[newHeight*newWidth];
        for(int i=0; i<newHeight; i++) {
            for(int j=0; j<newWidth; j++) {
                for(int k=0; k<mat.height; k++) {
                    product[i*newWidth+j] += content[i*width+k] * mat.content[k*mat.width+j];
                }
            }
        }
        content = product;
        width = newWidth;
        return this;
    }

    // multiply mat * this
    public Matrix multiplyLeft(Matrix mat) {
        if(mat.width != height) {
            String errMsg = "Error in Matrix.multiply : The dimensions of the matrices do not match\n";
            errMsg += "The origin matrix is " + Integer.toString(height) + "x" + Integer.toString(width);
            errMsg += " and the other is " + Integer.toString(mat.height) + "x" + Integer.toString(mat.width);
            throw new IllegalArgumentException(errMsg);
        }

        int newHeight = mat.height;
        int newWidth = width;
        double[] product = new double[newHeight*newWidth];
        for(int i=0; i<newHeight; i++) {
            for(int j=0; j<newWidth; j++) {
                for(int k=0; k<height; k++) {
                    product[i*newWidth+j] += content[k*width+j] * mat.content[i*mat.width+k];
                }
            }
        }
        content = product;
        height = newHeight;
        return this;
    }

    public Matrix map(MapFunction func) {
        for(int i=0; i<height*width; i++) {
            content[i] = func.mapFunc(content[i]);
        }
        return this;
    }

    public Matrix apply(Matrix mat, ApplicationFunction func) {
        if(height != mat.height || width != mat.width) {
            String errMsg = "Error in Matrix.apply : The dimensions of the matrices do not match\n";
            errMsg += "The origin matrix is " + Integer.toString(height) + "x" + Integer.toString(width);
            errMsg += " and the other is " + Integer.toString(mat.height) + "x" + Integer.toString(mat.width);
            throw new IllegalArgumentException(errMsg);
        }
        for(int i=0; i<height*width; i++) {
            content[i] = func.applyFunc(content[i], mat.content[i]);
        }
        return this;
    }

    public Matrix transpose() {
        double[] newcontent = new double[width*height];
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                newcontent[j*height + i] = content[i*width + j];
            }
        }
        int aux = width;
        width = height;
        height = aux;
        content = newcontent;
        return this;
    }

    public Matrix normalize() {
        double sum = 0;
        for(int i=0; i<height*width; i++) {
            sum += Math.abs(content[i]);
        }
        if(sum != 0) {
            for(int i=0; i<height*width; i++) {
                content[i] /= sum;
            }
        }
        return this;
    }

    public Matrix normalize(int axis) {
        if(axis == 0) {
            for(int i=0; i<height; i++) {
                double sum = 0;
                for(int j=0; j<width; j++) {
                    sum += Math.abs(content[i*width + j]);
                }
                if(sum != 0) {
                    for(int j=0; i<width; i++) {
                        content[i*width + j] /= sum;
                    }
                }
            }
        }
        else {
            for(int i=0; i<width; i++) {
                double sum = 0;
                for(int j=0; j<height; j++) {
                    sum += Math.abs(content[i*height + j]);
                }
                if(sum != 0) {
                    for(int j=0; i<height; i++) {
                        content[i*height + j] /= sum;
                    }
                }
            }
        }
        return this;
    }

    public double maxValue() {
        double max = Double.NEGATIVE_INFINITY;
        for(int i=0; i<height*width; i++) {
            if(content[i] > max) {
                max = content[i];
            }
        }
        return max;
    }

    public int[] maxIndex() {
        int indexHeight = 0, indexWidth = 0;
        double max = Double.NEGATIVE_INFINITY;
        for(int i=0; i<height*width; i++) {
            if(content[i] > max) {
                max = content[i];
                indexHeight = i/width;
                indexWidth = i%width;
            }
        }
        int[] toReturn = new int[2];
        toReturn[0] = indexHeight;
        toReturn[1] = indexWidth;
        return toReturn;
    }

    public double minValue() {
        double min = Double.POSITIVE_INFINITY;
        for(int i=0; i<height*width; i++) {
            if(content[i] < min) {
                min = content[i];
            }
        }
        return min;
    }

    public int[] minIndex() {
        int indexHeight = 0, indexWidth = 0;
        double min = Double.POSITIVE_INFINITY;
        for(int i=0; i<height*width; i++) {
            if(content[i] < min) {
                min = content[i];
                indexHeight = i/width;
                indexWidth = i%width;
            }
        }
        int[] toReturn = new int[2];
        toReturn[0] = indexHeight;
        toReturn[1] = indexWidth;
        return toReturn;
    }

    public void sum(int axis) {
        if(axis == 0) {
            double[] newcontent = new double[width];
            for(int i=0; i<height*width; i++) {
                newcontent[i/width] += content[i];
            }
            height = 1;
            content = newcontent;
            return;
        }
        if(axis == 1) {
            double[] newcontent = new double[height];
            for(int i=0; i<height; i++) {
                newcontent[i%width] += content[i];
            }
            content = newcontent;
            width = 1;
            return;
        }
        throw new IllegalArgumentException
        ("Error in Matrix.sum : Axis must be 0 or 1");
    }

    public boolean equals(Matrix mat) {
        if(height != mat.height || width != mat.width) {
            return false;
        }
        for(int i=0; i<height*width; i++) {
            if(content[i] != mat.content[i]) {
                return false;
            }
        }
        return true;
    }

    public static Matrix average(Matrix[] matrices) {
        int height = matrices[0].height;
        int width = matrices[0].width;
        Matrix toReturn = new Matrix(height, width);
        for(int i=0; i<height*width; i++) {
            for(int k=0; k<matrices.length; k++) {
                toReturn.content[i] += matrices[k].content[i];
            }
            toReturn.content[i] /= matrices.length;
        }
        return toReturn;
    }

    public String toString() {
        String s = "";
        s += "\n";
        s += "---- MATRIX ----\n";
        s += "Dimensions : " + Integer.toString(height) + "x" + Integer.toString(width) + "\n";
        s += "Content : \n";

        for(int i=0; i<height*width; i++) {
            s += Double.toString(content[i]) + "\t";
            if(i%width==width-1) {
                s += "\n";
            }
        }
        s += "-----------------\n";
        return s;
    }

    public static void main(String[] args) {
        // 1 2   1
        // 4 5   2
        double[][] content1 = {{1,2},{4,5}};
        double[] content2 = {1, 2};
        Matrix mat1 = new Matrix(content1);
        Matrix mat2 = new Matrix(content2);
        System.out.println(mat2.multiplyLeft(mat1));
    }
}
