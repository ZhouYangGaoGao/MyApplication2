package com.idcard.modernskyticketing1;

/**
 * Created by daxu on 5/11/17.
 */



import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseOpenHelper extends SQLiteAssetHelper {
    //private static final String DATABASE_NAME = "Tickets.sqlite";
    private static final String DATABASE_NAME = "天津草莓.sqlite";
    private static final int DATABASE_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
