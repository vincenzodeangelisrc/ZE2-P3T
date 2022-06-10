package com.unirc.tesi.marcoventura.contacttracing.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {

    private static Connection conn = null;
    private static final String DbDriver = "org.sqldroid.SQLDroidDriver";
    private static final String DbURL = "jdbc:sqldroid:/data/data/com.unirc.tesi.marcoventura.contacttracing/databases/Database.db";

    public static final String DATABASE_NAME = "Database.db";
    public static final String TABLE_TOKEN = "TOKEN";
    public static final String TOKEN_COL_1 = "ID";
    public static final String TOKEN_COL_2 = "RANDOM";

    public static final String TABLE_CONTACT = "CONTACT";
    public static final String CONTACT_COL_1 = "ADDRESS";
    public static final String CONTACT_COL_2 = "DISTANCE";
    public static final String CONTACT_COL_3 = "ANGLE";
    public static final String CONTACT_COL_4 = "TIMESTAMP";


    private DBManager(){
    }

    public static boolean isOpen(){
        return conn != null;
    }

    public static Connection startConnection(){
        if ( isOpen() )
            return conn;
        try {
            Class.forName(DbDriver);
            conn = DriverManager.getConnection(DbURL);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return conn;
    }
    public static boolean closeConnection(){
        if ( !isOpen() )
            return true;
        try {
            conn.close();
            conn = null;
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


}