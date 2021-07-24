package com.matrix;

import java.lang.IllegalArgumentException;
import java.util.Random;
import java.io.Serializable;

public class Matrix implements Serializable {
    private int width, height;
    private double[][] table;

    public Matrix(int height, int width) {
        this.width = width;
        this.height = height;
        table = new double[height][width];
    }

    public Matrix(double[][] content) {
        height = content.length;
        width = content[0].length;
        // TODO make it abstract and remove this check
        for(int i=1; i<height; i++) {
            if (content[i].length != width) {
                throw new IllegalArgumentException("The lines of the matrix are of different lengths");
            }
        }
        table = new double[height][width];
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                table[i][j] = content[i][j];
            }
        }
    }

    public Matrix(double[] content) {
        height = content.length;
        width = 1;
        table = new double[height][1];
        for(int i=0; i<height; i++) {
            table[i][0] = content[i];
        }
    }

    public static Matrix ones(int height, int width) {
        Matrix toReturn = new Matrix(height, width);
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                toReturn.table[i][j] = 1;
            }
        }
        return toReturn;
    }

    public Matrix clone() {
        double[][] tableClone = new double[height][width];
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                tableClone[i][j] = table[i][j];
            }
        }
        return new Matrix(tableClone);
    }

    public double getElement(int i, int j) { return table[i][j]; }
    public double[][] getTable() { return table; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public Matrix randomFill(double rangeMin, double rangeMax) {
        Random r = new Random();
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                table[i][j] = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
            }
        }
        return this;
    }

    public Matrix randomFillGauss(double mean, double stdev) {
        Random r = new Random();
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                table[i][j] = mean + stdev * r.nextGaussian();
            }
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
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                table[i][j] += mat.table[i][j];
            }
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
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                table[i][j] *= mat.table[i][j];
            }
        }
        return this;
    }

    public Matrix multiply(Matrix mat) {
        if(width != mat.height) {
            String errMsg = "Error in Matrix.multiply : The dimensions of the matrices do not match\n";
            errMsg += "The origin matrix is " + Integer.toString(height) + "x" + Integer.toString(width);
            errMsg += " and the other is " + Integer.toString(mat.height) + "x" + Integer.toString(mat.width);
            throw new IllegalArgumentException(errMsg);
        }

        int newHeight = height;
        int newWidth = mat.width;
        double[][] product = new double[newHeight][newWidth];
        for(int i=0; i<newHeight; i++) {
            for(int j=0; j<newWidth; j++) {
                double sum = 0.0;
                for(int k=0; k<mat.height; k++) {
                    sum += table[i][k] * mat.table[k][j];
                }
                product[i][j] = sum;
            }
        }
        table = product;
        height = newHeight;
        width = newWidth;
        return this;
    }

    public Matrix map(MapFunction func) {
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                table[i][j] = func.mapFunc(table[i][j]);
            }
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
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                table[i][j] = func.applyFunc(table[i][j], mat.table[i][j]);
            }
        }
        return this;
    }

    public Matrix transpose() {
        double[][] newTable = new double[width][height];
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                newTable[j][i] = table[i][j];
            }
        }
        width = height;
        height = newTable.length;
        table = newTable;
        return this;
    }

    public double maxValue() {
        double max = Double.NEGATIVE_INFINITY;
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                if(table[i][j] > max) {
                    max = table[i][j];
                }
            }
        }
        return max;
    }

    public int[] maxIndex() {
        int indexHeight = 0, indexWidth = 0;
        double max = Double.NEGATIVE_INFINITY;
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                if(table[i][j] > max) {
                    max = table[i][j];
                    indexHeight = i;
                    indexWidth = j;
                }
            }
        }
        int[] toReturn = new int[2];
        toReturn[0] = indexHeight;
        toReturn[1] = indexWidth;
        return toReturn;
    }

    public double minValue() {
        double min = Double.POSITIVE_INFINITY;
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                if(table[i][j] < min) {
                    min = table[i][j];
                }
            }
        }
        return min;
    }

    public int[] minIndex() {
        int indexHeight = 0, indexWidth = 0;
        double min = Double.POSITIVE_INFINITY;
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                if(table[i][j] < min) {
                    min = table[i][j];
                    indexHeight = i;
                    indexWidth = j;
                }
            }
        }
        int[] toReturn = new int[2];
        toReturn[0] = indexHeight;
        toReturn[1] = indexWidth;
        return toReturn;
    }

    public void sum(int axis) {
        double[][] newTable = new double[1][width];
        if(axis == 0) {
            for(int j=0; j<width; j++) {
                double sum = 0;
                for(int i=0; i<height; i++) {
                    sum += table[i][j];
                }
                table[0][j] = sum;
            }
            height = 1;
            return;
        }
        if(axis == 1) {
            for(int i=0; i<height; i++) {
                double sum = 0;
                for(int j=0; j<width; j++) {
                    sum += table[i][j];
                }
                table[i] = new double[1];
                table[i][0] = sum;
            }
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
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                if(table[i][j] != mat.table[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Matrix average(Matrix[] matrices) {
        Matrix toReturn = new Matrix(matrices[0].height, matrices[0].width);
        for(int i=0; i<toReturn.height; i++) {
            for(int j=0; j<toReturn.width; j++) {
                for(int k=0; k<matrices.length; k++) {
                    toReturn.table[i][j] += matrices[k].table[i][j];
                }
                toReturn.table[i][j] /= matrices.length;
            }
        }
        return toReturn;
    }


    /*public double determinant() {

    }*/

    private Matrix subMatrix(int removedRow, int removedColumn) {
        if(removedRow > height || removedColumn > width) {
            System.out.println("Error : Cannot remove this column or row");
        }
        double[][] subTable = new double[height-1][width-1];
        int incRow = 0;
        for(int i=0; i<height; i++) {
            int incCol = 0;
            if(i == removedRow) {
                incRow -= 1;
                continue;
            }
            for(int j=0; j<width; j++) {
                if(j == removedColumn) {
                    incCol -= 1;
                    continue;
                }
                subTable[i+incRow][j+incCol] = table[i][j];
            }
        }
        return new Matrix(subTable);
    }

    public String toString() {
        String s = "";
        s += "\n";
        s += "---- MATRIX ----\n";
        s += "Dimensions : " + Integer.toString(height) + "x" + Integer.toString(width) + "\n";
        s += "Content : \n";

        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                s += Double.toString(table[i][j]) + "\t";
            }
            s += "\n";
        }
        s += "-----------------\n";
        return s;
    }
}
