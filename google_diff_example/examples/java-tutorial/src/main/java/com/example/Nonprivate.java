package com.example;

import java.util.*;
import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class Nonprivate {
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

    public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/DATABASE_NAME";
        String UserName = "";
        String Password = "";
        Connection con = null;

        int MB = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        double startUsedMem = (double)((runtime.totalMemory() - runtime.freeMemory()) / MB);

        for(int j = 0; j < 9; j++){

        String answer = null;
        ArrayList<Double> dataPoints = new ArrayList<Double>();
        double cpuAVG = 0.0;

        try {
            con = DriverManager.getConnection(URL, UserName, Password);
            Statement stmt = con.createStatement();

            for (int i = 1; i < 30; i++) {
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                cpuAVG += osBean.getSystemLoadAverage();
                if(i = 8 || i != 21 ){
                    continue;
                }
                String filename = "query-" + i + ".sql";
                String path = "/home/2.18.0_rc2/dbgen/queries2/" + filename;

                String query = readFile(path);
                String colName = query.substring(query.indexOf("t") + 1, query.indexOf("f"));
                colName = colName.trim();

                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    System.out.println("query: " + i);
                    answer = rs.getString(colName);

                    dataPoints.add(Double.parseDouble(answer));
                    System.out.println(colName + ": " + answer);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();}


        double endUsedMem = (double)((runtime.totalMemory() - runtime.freeMemory()) / MB);
        dataPoints.add(endUsedMem - startUsedMem);
        dataPoints.add(cpuAVG/29);

        writeFile(dataPoints, "/home/google_diff_example/examples/java-tutorial/src/main/java/com/example/nonPrivate/nonPrivateResult" + j + ".txt");


        System.out.println(j);
        }
    }
}