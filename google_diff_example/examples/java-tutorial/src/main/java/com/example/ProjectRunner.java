package com.example;
import java.util.*;
import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import com.google.privacy.differentialprivacy.Count;
import com.google.privacy.differentialprivacy.BoundedSum;
import com.google.privacy.differentialprivacy.BoundedMean;
public class ProjectRunner {

    public static String readFile(String s) {
        StringBuilder sb = new StringBuilder();
        try {
            File myObj = new File(s);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                sb.append(data);

            }
            myReader.close();
        }catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        String query = sb.toString();
        query = query.replace(";","");
        return query;
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
        int MB = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        double startUsedMem = (double)((runtime.totalMemory() - runtime.freeMemory()) / MB);

        for(int j = 0; j < 3; j++){

        String URL = "jdbc:mysql://localhost:3306/tpch";
        String UserName = "tarik";
        String Password = "";
        Connection con = null;
        String answer = null;
        final double epsilon = 1;
        ArrayList<Double> dataPoints = new ArrayList<Double>();
        double cpuAVG = 0.0;
        try {
            con = DriverManager.getConnection(URL, UserName, Password);
            Statement stmt = con.createStatement();

            for (int i = 1; i < 30; i++) {
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                cpuAVG += osBean.getSystemLoadAverage();

                if(i == 8){
                    //System.out.println(i);
                    continue;
                }
                String filename = "query-" + i + ".sql";
                String path = "/home/tarik/Desktop/2.18.0_rc2/dbgen/" + filename;

                String query = readFile(path);
                String colName = query.substring(query.indexOf("t") + 1, query.indexOf("f"));
                colName = colName.trim();

                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    //System.out.println("query: " + i);
                    answer = rs.getString(colName);
                    //System.out.println(colName + ": " + answer);
                }
                if(i == 4 || i == 13 || i == 24){

                    //long num = Long.parseLong(answer);
                    Count dpCount = Count.builder()
                            .epsilon(epsilon)
                            .maxPartitionsContributed(1)
                            .build();
                    dpCount.incrementBy(Long.parseLong(answer));
                    double countRes = (double) dpCount.computeResult();

                    //System.out.println("noise countRes: " + countRes);
                    //System.out.println("Count diff: " + (Double.parseDouble(answer) - countRes));
                    dataPoints.add(countRes);

                }else if (i == 27 || i == 28 || i == 29){
                    //double num = Double.parseDouble(answer);
                    BoundedMean bdMean = BoundedMean.builder()
                            .epsilon(epsilon)
                            .maxPartitionsContributed(1)
                            .maxContributionsPerPartition(1)
                            .lower(0)
                            .upper(Double.parseDouble(answer))
                            .build();
                    bdMean.addEntry(Double.parseDouble(answer));
                    double meanRes = (double) bdMean.computeResult();

                    //System.out.println("mean Result: " + meanRes);
                    //System.out.println("'Mean diff: " + (Double.parseDouble(answer) - meanRes));
                    dataPoints.add(meanRes);
                }else{
                    //double num = Double.parseDouble(answer);
                    BoundedSum dpSum = BoundedSum.builder()
                            .epsilon(epsilon)
                            .maxPartitionsContributed(1)
                            .lower(0)
                            .upper(Double.parseDouble(answer))
                            .build();
                    dpSum.addEntry(Double.parseDouble(answer));
                    double sumRes = (double) dpSum.computeResult();

                    //System.out.println("noise sumRes: " + sumRes);
                   // System.out.println("Sum diff: " + (Double.parseDouble(answer) - sumRes));
                    dataPoints.add(sumRes);

                }
            }

        }catch (Exception e) {
            e.printStackTrace();}
        try {
            con.close();
        } catch (SQLException e){
            e.printStackTrace();}

        double endUsedMem = (double)((runtime.totalMemory() - runtime.freeMemory()) / MB);
        dataPoints.add(endUsedMem - startUsedMem);
        dataPoints.add(cpuAVG/29);

        writeFile(dataPoints, "/home/tarik/Desktop/google_diff_example/examples/java-tutorial/src/main/java/com/example/small/privateResult" + j + ".txt");

        System.out.println(j);
        }

    }
}