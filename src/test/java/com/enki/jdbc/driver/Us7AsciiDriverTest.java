package com.enki.jdbc.driver;

import org.junit.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Us7AsciiDriverTest {

    @Test
    public void testConnectionAndQuery() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Load the driver
            Class.forName("com.enki.jdbc.driver.Us7AsciiDriver");

            // Connection details
            String url = "jdbc:oracle:us7ascii:thin:@//localhost:1521/XE";
            String user = "kidi";
            String password = "rlagywls";

            System.out.println("Connecting to database: " + url);
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!");

            stmt = conn.createStatement();

            // 1. Insert using PreparedStatement
            String testStr3 = "한글테스트3";
            String testStr4 = "한글테스트4";
            String pstmtSql = "INSERT INTO kotest (str1, str2) VALUES (?, ?)";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(pstmtSql);
            pstmt.setString(1, testStr3);
            pstmt.setString(2, testStr4);
            System.out.println("Executing PreparedStatement INSERT");
            int rows2 = pstmt.executeUpdate();
            System.out.println("Inserted rows (pstmt): " + rows2);
            pstmt.close();

            // 2. Verify PreparedStatement insert
            String selectSql2 = "SELECT str1, str2 FROM kotest WHERE str1 = ?";
            System.out.println("Executing SELECT for PreparedStatement: " + selectSql2);
            java.sql.PreparedStatement pstmtSelect = conn.prepareStatement(selectSql2);
            pstmtSelect.setString(1, testStr3);
            rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                String val1 = rs.getString("str1");
                String val2 = rs.getString("str2");

                System.out.println("Retrieved str1: " + val1);
                System.out.println("Retrieved str2: " + val2);

                if (!testStr3.equals(val1)) {
                    throw new RuntimeException(
                            "Verification Failed! str1 expected: " + testStr3 + ", but got: " + val1);
                }
                if (!testStr4.equals(val2)) {
                    throw new RuntimeException(
                            "Verification Failed! str2 expected: " + testStr4 + ", but got: " + val2);
                }
                System.out.println("Verification Successful (PreparedStatement)!");
            } else {
                throw new RuntimeException("Verification Failed! No rows found for PreparedStatement insert.");
            }
            rs.close();

            // 3. Insert Korean Data (Statement)
            String testStr1 = "한글테스트1";
            String testStr2 = "한글테스트2";
            String insertSql = "INSERT INTO kotest (str1, str2) VALUES ('" + testStr1 + "', '" + testStr2 + "')";
            System.out.println("Executing INSERT: " + insertSql);
            int rows = stmt.executeUpdate(insertSql);
            System.out.println("Inserted rows: " + rows);

            // 4. Select and Verify (Statement)
            String selectSql = "SELECT str1, str2 FROM kotest WHERE str1 = '" + testStr1 + "'";
            System.out.println("Executing SELECT: " + selectSql);
            rs = stmt.executeQuery(selectSql);

            if (rs.next()) {
                String val1 = rs.getString("str1");
                String val2 = rs.getString("str2");

                System.out.println("Retrieved str1: " + val1);
                System.out.println("Retrieved str2: " + val2);

                if (!testStr1.equals(val1)) {
                    throw new RuntimeException(
                            "Verification Failed! str1 expected: " + testStr1 + ", but got: " + val1);
                }
                if (!testStr2.equals(val2)) {
                    throw new RuntimeException(
                            "Verification Failed! str2 expected: " + testStr2 + ", but got: " + val2);
                }
                System.out.println("Verification Successful (Statement)!");
            } else {
                throw new RuntimeException("Verification Failed! No rows found for Statement insert.");
            }

            // 5. Test COMMENT ON
            String commentSql = "COMMENT ON TABLE kotest IS '한글테스트'";
            System.out.println("Executing COMMENT ON: " + commentSql);
            stmt.execute(commentSql);
            System.out.println("COMMENT ON executed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }
}
