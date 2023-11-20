package com.project.datamanager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CsvToMysql {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL JDBC 드라이버
            String jdbcUrl = "jdbc:mysql://koreanfood.ceawwpk7wakz.ap-northeast-2.rds.amazonaws.com:3306/koreanfood"; // MySQL 연결 URL
            String jdbcUser = "jyj";
            String jdbcPassword = "19960426";
            String csvFilePath = "C:\\DBDataSet.csv"; // CSV 파일 경로

            Connection connection = null;
            try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
                String line;
                connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);

                while ((line = br.readLine()) != null) {
                    String[] data = line.split(","); // CSV 파일의 구분자
                    String query = "INSERT INTO menu "
                            + "(name, en_name, explanation, search_count)"
                            + " VALUES "
                            + "(?, ?, ?, 0)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, data[0]);
                        preparedStatement.setString(2, data[1]);
                        preparedStatement.setString(3, data[2]);
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

