package co.ke.tonyoa.android.vossified.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class VossitOpenHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "HADZA";
    public static final int DBVERSION = 2;

    public VossitOpenHelper(@Nullable Context context) {
        super(context, DBNAME + ".db", null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(VossitContract.CATEGORIESENTRY.CREATETABLE);
        db.execSQL(VossitContract.CATEGORYIMAGESENTRY.CREATETABLE);
        db.execSQL(VossitContract.ITEMSENTRY.CREATETABLE);
        db.execSQL(VossitContract.ITEMIMAGESENTRY.CREATETABLE);
        db.execSQL(VossitContract.ITEMLINKSENTRY.CREATETABLE);
        db.execSQL(VossitContract.USERSENTRY.CREATETABLE);
        db.execSQL(VossitContract.USERREQUESTSENTRY.CREATETABLE);
        db.execSQL(VossitContract.USERREQUESTIMAGESENTRY.CREATETABLE);
        db.execSQL(VossitContract.ITEMCLICKSENTRY.CREATETABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion<2){
            db.execSQL(VossitContract.ITEMCLICKSENTRY.CREATETABLE);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

}
