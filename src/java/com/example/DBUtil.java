package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    private static final String HOST = "jdbc:mysql://192.168.228.100:3306";
    private static final String DB = "yabo_address_book";
    private static final String FULL_URL = HOST + "/" + DB + "?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PWD = "root123";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found", e);
        }
        init();
    }

    // 启动时自动建库 + 建表
    private static void init() {
        // 第一步：连 MySQL（不带库名），建库
        try (Connection conn = DriverManager.getConnection(HOST, USER, PWD);
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB + " DEFAULT CHARSET utf8mb4");
        } catch (SQLException e) {
            System.err.println("建库失败: " + e.getMessage());
            return;
        }
        // 第二步：连目标库，建表
        try (Connection conn = DriverManager.getConnection(FULL_URL, USER, PWD);
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `user` (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(60) NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `grp` (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "group_name VARCHAR(50) NOT NULL," +
                    "is_default TINYINT NOT NULL DEFAULT 0," +
                    "user_id INT NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS contact (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL," +
                    "phone VARCHAR(20)," +
                    "province VARCHAR(50)," +
                    "city VARCHAR(50)," +
                    "county VARCHAR(50)," +
                    "company VARCHAR(100)," +
                    "email VARCHAR(100)," +
                    "remark VARCHAR(200)," +
                    "group_id INT," +
                    "user_id INT NOT NULL," +
                    "FOREIGN KEY (group_id) REFERENCES `grp`(id) ON DELETE SET NULL," +
                    "FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            System.out.println("数据库初始化完成");
        } catch (SQLException e) {
            System.err.println("建表失败: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(FULL_URL, USER, PWD);
    }
}
