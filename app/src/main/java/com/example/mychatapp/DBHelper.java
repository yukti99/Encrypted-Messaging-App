package com.example.mychatapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    // THE DATABASE FOR THE WHOLE APP
    public static final String DATABASE_USERS = "Users.db";
    public static final String TABLE_USERS = "UsersKey";
    private ProgressDialog loadingBar;
    // THE TABLE FOR USERS TO STORE THEIR KEYS
    public static final String user_id = "user_id";
    public static final String pkey = "pkey";

    public DBHelper(@Nullable Context context) {

        super(context, DATABASE_USERS, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        // executes whatever string query we enter as string
        db.execSQL("create table "+ TABLE_USERS +"('user_id' TEXT,'pkey' TEXT)");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS "+TABLE_USERS);
        onCreate(db);
    }
    //*****************************************USER DATABASE METHODS**********************************************************************



    public String getPkey(String userid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select pkey from "+TABLE_USERS+" where user_id = ?",new String[]{userid});
        if (cursor.moveToFirst()) {
            String k = cursor.getString(cursor.getColumnIndex("pkey"));
            return k;
        }
        cursor.close();
        return null;
    }

    public boolean insertData(String userid,String Pkey){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(user_id,userid);
        contentValues.put(pkey,Pkey);
        long result = db.insert(TABLE_USERS,null ,contentValues);
        db.close();
        // data not inserted successfully
        if(result == -1) {
            System.out.println("Sorry.. Some error occurred, Try again later!!!");
            return false;
        }
        else {
            System.out.println("user id and key stored to database successfully!");
            return true;
        }
    }

    //************************************************************************************************************************************

}
