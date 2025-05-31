package com.example.seatmanager.util;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;

/**
 * DBUtil：嵌入式 MariaDB4j 数据库初始化及连接工具类
 */
public class DBUtil {

    // 嵌入式数据库实例
    private static DB embeddedDB;

    /**
     * 初始化嵌入式 MariaDB 数据库并执行 schema.sql（建库、建表、预装数据）。
     * 需要在程序入口（Main.main）中调用此方法，且会阻塞直到 schema.sql 完全执行完毕。
     */
    public static void initDatabase() {
        try {
            // 1. 构建 MariaDB4j 配置（使用随机端口，数据目录为 ./embeddedDB）
            DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
            config.setPort(0);                     // 0 表示随机端口
            config.setDataDir(new File("embeddedDB")); // 使用 File 对象指定目录

            // 2. 启动嵌入式数据库
            embeddedDB = DB.newEmbeddedDB(config.build());
            embeddedDB.start();

            // 3. 读取 classpath 下的 schema.sql，并切分成单条 SQL 语句执行
            String schemaSql = loadResourceAsString("schema.sql");
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                // schema.sql 中应包含：CREATE DATABASE IF NOT EXISTS SeatManagerDB; USE SeatManagerDB;
                // 此处以分号分隔并逐条执行
                for (String sql : schemaSql.split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }

            System.out.println("[DBUtil] Embedded MariaDB initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize embedded MariaDB4j.", e);
        }
    }

    /**
     * 获取到 SeatManagerDB 数据库的 JDBC 连接。
     * 注意：调用本方法前，必须先执行 initDatabase()。
     *
     * @return Connection 对象，用完后请关闭
     * @throws Exception 如果获取连接失败
     */
    public static Connection getConnection() throws Exception {
        // embeddedDB.getConfiguration().getURL("SeatManagerDB") 会返回类似 jdbc:mariadb://localhost:XXXXX/SeatManagerDB
        String jdbcUrl = embeddedDB.getConfiguration().getURL("SeatManagerDB");
        String username = "root";
        String password = "";  // 嵌入式 MariaDB 默认无密码
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    /**
     * 读取 classpath 下的资源文件为字符串，常用于加载 schema.sql。
     *
     * @param resourceName 资源相对路径，如 "schema.sql"
     * @return 文件完整内容
     */
    private static String loadResourceAsString(String resourceName) {
        InputStream in = DBUtil.class.getClassLoader().getResourceAsStream(resourceName);
        if (in == null) {
            throw new RuntimeException("Resource not found: " + resourceName);
        }
        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
        String text = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        scanner.close();
        return text;
    }
}
