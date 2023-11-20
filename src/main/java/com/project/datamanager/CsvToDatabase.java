package com.project.datamanager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
public class CsvToDatabase {
	
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:log4jdbc:oracle:thin:@localhost:1521:xe"; //oracle
        String jdbcUser = "project"; // id
        String jdbcPassword = "project"; // password
        String csvFilePath = "C:\\DBDataSet.csv"; //csv파일 경로
        
        try {
        	Class.forName("oracle.jdbc.driver.OracleDriver"); // 오라클DB 드라이버
            // 데이터베이스 연결 설정
            Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
            // CSV 파일 읽기
            try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
                String line;
                // CSV 파일의 각 행을 읽어서 데이터베이스에 삽입
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(","); // CSV 파일의 구분자
                    String query = "INSERT INTO menu "
                    				+ "(no, name, en_name, explanation, search_count)"
                    			 + " VALUES "
                    			 	+ "(seq_menu_no.nextval, ?, ?, ?, 0)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, data[0]);
                        preparedStatement.setString(2, data[1]);
                        preparedStatement.setString(3, data[2]);
                        preparedStatement.executeUpdate();
                    }
                }
            }
            // 연결 닫기
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
