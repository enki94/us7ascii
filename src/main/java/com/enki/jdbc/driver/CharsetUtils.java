package com.enki.jdbc.driver;

import java.io.UnsupportedEncodingException;

public class CharsetUtils {

    public static String toApp(String dbStr) {
        if (dbStr == null) {
            return null;
        }
        try {
            return new String(dbStr.getBytes("ISO-8859-1"), "EUC-KR");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding conversion failed: " + e.getMessage(), e);
        }
    }

    public static String toDb(String appStr) {
        if (appStr == null) {
            return null;
        }
        try {
            return new String(appStr.getBytes("EUC-KR"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding conversion failed: " + e.getMessage(), e);
        }
    }

    public static String transformSql(String sql) {
        if (sql == null)
            return null;
        StringBuilder sb = new StringBuilder();
        StringBuilder literal = new StringBuilder();
        boolean inString = false;
        int len = sql.length();

        for (int i = 0; i < len; i++) {
            char c = sql.charAt(i);
            if (!inString) {
                if (c == '\'') {
                    inString = true;
                    literal.setLength(0);
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '\'') {
                    if (i + 1 < len && sql.charAt(i + 1) == '\'') {
                        literal.append('\'');
                        i++; // Skip next quote
                    } else {
                        inString = false;
                        String val = literal.toString();
                        if (hasNonAscii(val)) {
                            sb.append("UTL_RAW.CAST_TO_VARCHAR2(HEXTORAW('");
                            sb.append(toHex(val));
                            sb.append("'))");
                        } else {
                            sb.append('\'').append(val).append('\'');
                        }
                    }
                } else {
                    literal.append(c);
                }
            }
        }
        if (inString) {
            sb.append('\'').append(literal);
        }
        return sb.toString();
    }

    public static String transformCommentOn(String sql) {
        // Simple parser for COMMENT ON ... IS 'literal'
        String trimmed = sql.trim();
        int isIndex = trimmed.toUpperCase().lastIndexOf(" IS ");
        if (isIndex == -1)
            return sql; // Fallback

        String prefix = trimmed.substring(0, isIndex + 4); // Includes " IS "
        String literal = trimmed.substring(isIndex + 4).trim();

        if (literal.startsWith("'") && literal.endsWith("'")) {
            String content = literal.substring(1, literal.length() - 1);
            if (hasNonAscii(content)) {
                // Construct PL/SQL block
                // BEGIN EXECUTE IMMEDIATE 'prefix ''' ||
                // UTL_RAW.CAST_TO_VARCHAR2(HEXTORAW('hex')) || ''''; END;
                String escapedPrefix = prefix.replace("'", "''");
                return "BEGIN EXECUTE IMMEDIATE '" + escapedPrefix + "''' || UTL_RAW.CAST_TO_VARCHAR2(HEXTORAW('"
                        + toHex(content) + "')) || ''''; END;";
            }
        }
        return sql;
    }

    private static boolean hasNonAscii(String s) {
        for (char c : s.toCharArray()) {
            if (c > 127)
                return true;
        }
        return false;
    }

    private static String toHex(String s) {
        try {
            byte[] bytes = s.getBytes("EUC-KR");
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
