package com.enki.jdbc.driver;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Us7AsciiDriver implements Driver {

    private static final String URL_PREFIX = "jdbc:oracle:us7ascii:";
    private static final String REAL_URL_PREFIX = "jdbc:oracle:";

    static {
        try {
            java.sql.DriverManager.registerDriver(new Us7AsciiDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Can't register driver!");
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        String realUrl = url.replace(URL_PREFIX, REAL_URL_PREFIX);
        Connection realConn = DriverManager.getConnection(realUrl, info);
        return new Us7AsciiConnection(realConn);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.startsWith(URL_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        String realUrl = url.replace(URL_PREFIX, REAL_URL_PREFIX);
        Driver realDriver = DriverManager.getDriver(realUrl);
        return realDriver.getPropertyInfo(realUrl, info);
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger("Us7AsciiDriver");
    }
}
