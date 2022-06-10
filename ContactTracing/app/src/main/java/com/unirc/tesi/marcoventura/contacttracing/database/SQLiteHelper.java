package com.unirc.tesi.marcoventura.contacttracing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.unirc.tesi.marcoventura.contacttracing.ble.BleContactPhase;
import com.unirc.tesi.marcoventura.contacttracing.token.Token;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.CONTACT_COL_1;
import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.CONTACT_COL_2;
import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.CONTACT_COL_3;
import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.CONTACT_COL_4;
import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.DATABASE_NAME;
import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.TABLE_CONTACT;
import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.TABLE_TOKEN;
import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.TOKEN_COL_1;
import static com.unirc.tesi.marcoventura.contacttracing.database.DBManager.TOKEN_COL_2;

public class SQLiteHelper extends SQLiteOpenHelper {


    public static Connection conn = null;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE "+ TABLE_TOKEN + " ( "+ TOKEN_COL_1 +" INTEGER PRIMARY KEY AUTOINCREMENT, "+ TOKEN_COL_2 +" TEXT) " );
        db.execSQL("CREATE TABLE "+ TABLE_CONTACT + " ( "+ CONTACT_COL_1 +" TEXT, "+ CONTACT_COL_2 +" DOUBLE, " +
                ""+ CONTACT_COL_3 +" DOUBLE, "+ CONTACT_COL_4 +" LONG) " );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_TOKEN);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_CONTACT);

        onCreate(db);
    }


    /* ####### TOKEN ####### */

    public boolean saveToken(Token token){

        String query = "INSERT INTO "+ TABLE_TOKEN + " VALUES (?,?)";
        boolean result = false;
        conn = DBManager.startConnection();
        PreparedStatement ps;

        try {
            assert conn != null;
            ps = conn.prepareStatement(query);
            ps.setString(2,token.getRandom_token());

            int tmp = ps.executeUpdate();
            if(tmp == 1){
                result = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        DBManager.closeConnection();
        return result;

    }


    public ArrayList<Token> readAllToken(){

        String query = "SELECT * FROM "+ TABLE_TOKEN;
        ArrayList<Token> res = new ArrayList<>();
        conn = DBManager.startConnection();
        PreparedStatement ps;

        try {

            assert conn != null;
            ps = conn.prepareStatement(query);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                res.add(recordToToken(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        DBManager.closeConnection();
        return res;

    }

    private Token recordToToken(ResultSet rs) throws SQLException {

        Token token = new Token();
        token.setId(rs.getInt(TOKEN_COL_1));
        token.setRandom_token(rs.getString(TOKEN_COL_2));
        return token;

    }


    /* ####### CONTACT ####### */

    public boolean saveContact(BleContactPhase contactPhase){

        String query = "INSERT INTO "+ TABLE_CONTACT + " VALUES (?,?,?,?)";
        boolean result = false;
        conn = DBManager.startConnection();
        PreparedStatement ps;

        try {
            assert conn != null;
            ps = conn.prepareStatement(query);
            ps.setDouble(2,contactPhase.getDistance());
            ps.setDouble(3,contactPhase.getAngle());
            ps.setLong(4,contactPhase.getTimestamp());

            int tmp = ps.executeUpdate();
            if(tmp == 1){
                result = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        DBManager.closeConnection();
        return result;

    }


    public ArrayList<BleContactPhase> readAllContact(){

        String query = "SELECT * FROM "+ TABLE_CONTACT;
        ArrayList<BleContactPhase> res = new ArrayList<>();
        conn = DBManager.startConnection();
        PreparedStatement ps;

        try {

            assert conn != null;
            ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                res.add(recordToContact(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        DBManager.closeConnection();
        return res;

    }

    private BleContactPhase recordToContact(ResultSet rs) throws SQLException {

        BleContactPhase contactPhase = new BleContactPhase();
        contactPhase.setMac_address(rs.getString(CONTACT_COL_1));
        contactPhase.setDistance(rs.getDouble(CONTACT_COL_2));
        contactPhase.setAngle(rs.getDouble(CONTACT_COL_3));
        contactPhase.setTimestamp(rs.getLong(CONTACT_COL_4));

        return contactPhase;

    }


}