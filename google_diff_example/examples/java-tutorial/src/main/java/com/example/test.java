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
public class test {

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

        for(int j = 0; j < 9; j++){

            String URL = "jdbc:mysql://localhost:3306/DATABASE_NAME;
            String UserName = "";
            String Password = "";
            Connection con = null;
            String answer = null;
            final double epsilon = 0.01;
            ArrayList<Double> dataPoints = new ArrayList<Double>();
            double cpuAVG = 0.0;
            double answerSum = 0.0;
            String colName = "";
            try {
                con = DriverManager.getConnection(URL, UserName, Password);
                Statement stmt = con.createStatement();

                for (int i = 4; i < 30; i++) {
                    ArrayList<Double> resArray = new ArrayList<Double>();
                    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    cpuAVG += osBean.getSystemLoadAverage();

                    if(i == 8 || i == 21 ){      //query 8 takes to long prbably some change which have gone wrong
                        continue;               //query 21 takes to long on bigger datasets
                    }
                    String filename = "query-" + i + ".sql";
                    String path = "/home/2.18.0_rc2/dbgen/" + filename;     //path to tpch queries
                    String query = readFile(path);


                    colName = query.substring(query.indexOf("t") + 1, query.indexOf("f"));
                    colName = colName.trim();


                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()) {
                        answer = rs.getString(colName);
                        answerSum = Double.parseDouble(answer);

                        resArray.add(answerSum);
                    }
                    if(i == 4 || i == 13 || i == 16 || i == 21 || i == 22 || i == 24){

                        Count dpCount = Count.builder()
                                .epsilon(epsilon)
                                .maxPartitionsContributed(1)
                                .build();
                        dpCount.incrementBy(Long.parseLong(answer));
                        double countRes = (double) dpCount.computeResult();

                        dataPoints.add(countRes);

                    }else if (i == 27 || i == 28 || i == 29){
                        BoundedMean bdMean = BoundedMean.builder()
                                .epsilon(epsilon)
                                .maxPartitionsContributed(1)
                                .maxContributionsPerPartition(1)
                                .lower(Double.parseDouble(answer)/2)
                                .upper(Double.parseDouble(answer)+1)
                                .build();

                        for(int index = 0; index < resArray.size(); index++){
                            bdMean.addEntry(resArray.get(index));
                        }
                        double meanRes = (double) bdMean.computeResult();

                        dataPoints.add(meanRes);
                    }else{

                        BoundedSum dpSum = BoundedSum.builder()
                                .epsilon(epsilon)
                                .maxPartitionsContributed(1)
                                .lower(0)
                                .upper(Double.parseDouble(answer)+1)
                                .build();
                        for(int index = 0; index < resArray.size(); index++){
                            dpSum.addEntry(resArray.get(index));
                    }
                        double sumRes = (double) dpSum.computeResult();

                        dataPoints.add(sumRes);

                    }
                    System.out.println(i);}

            }catch (Exception e) {
                e.printStackTrace();}
            try {
                con.close();
            } catch (SQLException e){
                e.printStackTrace();}

            double endUsedMem = (double)((runtime.totalMemory() - runtime.freeMemory()) / MB);
            dataPoints.add(endUsedMem - startUsedMem);
            dataPoints.add(cpuAVG/29);

            writeFile(dataPoints, "/home/google_diff_example/examples/java-tutorial/src/main/java/com/example/private/privateResult" + j + ".txt");

            System.out.println(j);
        }
    }
}