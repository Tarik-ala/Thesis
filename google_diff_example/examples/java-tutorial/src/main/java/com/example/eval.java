package com.example;
import java.util.*;
import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;



public class eval {

    public static void print2D(double mat[][])
    {
        for (double[] row : mat)
            System.out.println(Arrays.toString(row));
    }

    public static ArrayList<Double> readFile(String s) {
        ArrayList<Double> dataPoints = new ArrayList<Double>();
        try {
            File myObj = new File(s);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                dataPoints.add(Double.parseDouble(data));

            }
            myReader.close();
        }catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return dataPoints;
    }

    public static void writeFile(ArrayList<Double> dataPoints, String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            for(double str: dataPoints) {
                writer.write(str + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        int row = 10;
        int col = 30;

        double[] idle = {3195, 0.15};
        double[] finalUtility = new double[30];
        double[] finalNonPOverhead = new double[2];

        double[][] privateMatrix = new double[row][col];
        double[][] nonPrivateMatrix = new double[row][col];
        double[][] accuracyRating = new double[row][col];
        double[][] overHeadNonp = new double[row][2];

        ArrayList<Double> finalListValue = new ArrayList<Double>();


        for(int i = 0; i < row; i++ ){

            String fileName = "/home/google_diff_example/examples/java-tutorial/src/main/java/com/example/private/privateResult" + i + ".txt";
            String fileName2 = "/home/google_diff_example/examples/java-tutorial/src/main/java/com/example/nonPrivate/nonPrivateResult" + i + ".txt";

            ArrayList<Double> privateValues = readFile(fileName);
            ArrayList<Double> nonPrivateValues = readFile(fileName2);

            for(int j = 0; j < col; j++){
                privateMatrix[i][j] = privateValues.get(j);
                nonPrivateMatrix[i][j] = nonPrivateValues.get(j);
            }
        }
        for(int k = 0; k < row; k++){
            for(int l = 0; l < (col-2); l++){
               double tempNumerator =  privateMatrix[k][l]-nonPrivateMatrix[k][l];
               accuracyRating[k][l] = (1 - Math.abs(tempNumerator/nonPrivateMatrix[k][l]));
            }
            int s = 0;
            for(int m = 6; m < col; m++){
                accuracyRating[k][m] = (privateMatrix[k][m]/idle[s]);
                overHeadNonp[k][s] = (nonPrivateMatrix[k][m]/idle[s]);
                s = 1;
            }
        }
        for(int n = 0; n < col; n++){
            double sumPrivateTemp = 0.0;
            double sumNonPUtility = 0.0;
            for(int o = 0; o < row; o++){
                sumPrivateTemp = sumPrivateTemp + accuracyRating[o][n];
                if(n < 2){
                    sumNonPUtility = sumNonPUtility + overHeadNonp[o][n];
                }
            }
            if(n < 2){
                finalNonPOverhead[n] = sumNonPUtility/row;
            }
            finalUtility[n] = sumPrivateTemp/row;
        }
        int length = finalNonPOverhead.length + finalUtility.length;
        double[] finalResult = new double[length];

        System.arraycopy(finalUtility, 0, finalResult, 0, finalUtility.length);
        System.arraycopy(finalNonPOverhead, 0, finalResult, finalUtility.length, finalNonPOverhead.length);

        System.out.println(Arrays.toString(finalResult));

        for(int p = 0; p < finalResult.length; p++){
            finalListValue.add(finalResult[p]);
        }


        writeFile(finalListValue, "/home/google_diff_example/examples/java-tutorial/src/main/java/com/example/Results/Result.txt");
   }
}
