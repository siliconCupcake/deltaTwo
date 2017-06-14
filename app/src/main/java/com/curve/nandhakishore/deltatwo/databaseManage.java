package com.curve.nandhakishore.deltatwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import java.util.ArrayList;

public class databaseManage {

    private static final String DB_NAME = "DELTA_TWO";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "IMAGE_CARDS";
    private static final String C_ID = "ID";
    private static final String C_IMG = "IMAGE";
    private static final String C_CAPTION = "CAPTION";
    private String[] allColumns = {C_ID, C_IMG, C_CAPTION};

    private static final String CREATE_DB = "CREATE TABLE " + TABLE_NAME + "( " + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + C_IMG + " TEXT, " + C_CAPTION + " TEXT);";

    private dbHelper myHelper;
    private final Context myContext;
    private SQLiteDatabase myDatabase;

    public databaseManage (Context c) {
        myContext = c;
    }

    public class dbHelper extends SQLiteOpenHelper {

        public dbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_DB);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    public databaseManage open() {
        myHelper = new dbHelper(myContext);
        myDatabase = myHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        myHelper.close();
    }

    public long createEntry (cardItem c) {
        ContentValues cv = new ContentValues();
        cv.put(C_IMG, c.image.toString());
        cv.put(C_CAPTION, c.caption);
        return myDatabase.insert(TABLE_NAME, null, cv);
    }

    public ArrayList<cardItem> getData() {
        ArrayList<cardItem> list = new ArrayList<>();
        Cursor c = myDatabase.query(TABLE_NAME, allColumns, null, null, null, null, null);
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            cardItem row = convertData(c);
            list.add(row);
        }
        c.close();
        return list;
    }

    public void removeRow(cardItem c) {
        int args = c.place;
        myDatabase.delete(TABLE_NAME, C_ID + " = " + args, null);
    }

    private cardItem convertData(Cursor cursor) {
        Uri bmp = Uri.parse(cursor.getString(1));
        return new cardItem(bmp, cursor.getString(2), cursor.getInt(0));
    }

}
