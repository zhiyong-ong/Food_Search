package orbital.com.menusnap.DAO;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import orbital.com.menusnap.DAO.PhotosContract.PhotosEntry;
/**
 * Created by zhiyong on 19/7/2016.
 */

public class PhotosDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "PhotosData.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + PhotosEntry.TABLE_NAME + " (" +
                    PhotosEntry._ID + " INTEGER PRIMARY KEY," +
                    PhotosEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    PhotosEntry.COLUMN_NAME_ENTRY_TIME + TEXT_TYPE + COMMA_SEP +
                    PhotosEntry.COLUMN_NAME_DATA + TEXT_TYPE + COMMA_SEP +
                    PhotosEntry.COLUMN_NAME_FORMATTED_DATE + TEXT_TYPE + COMMA_SEP +
                    PhotosEntry.COLUMN_NAME_FORMATTED_STRING + TEXT_TYPE +
            " )";
    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + PhotosEntry.TABLE_NAME;

    public PhotosDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}


