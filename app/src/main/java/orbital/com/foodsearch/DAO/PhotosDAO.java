package orbital.com.foodsearch.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import orbital.com.foodsearch.DAO.PhotosContract.PhotosEntry;
/**
 * Created by zhiyong on 27/7/2016.
 */

public class PhotosDAO {
    public static Cursor readDatabaseGetRow(String fileTitle, PhotosDBHelper mDBHelper) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] results = {
                PhotosEntry._ID,
                PhotosEntry.COLUMN_NAME_DATA,
                PhotosEntry.COLUMN_NAME_ENTRY_TIME,
                PhotosEntry.COLUMN_NAME_FORMATTED_DATE,
                PhotosEntry.COLUMN_NAME_FORMATTED_STRING};
        Cursor c = db.query(
                PhotosEntry.TABLE_NAME,
                results,
                PhotosEntry.COLUMN_NAME_ENTRY_TIME + " = '" + fileTitle + "'",
                null,
                null,
                null,
                null);
        return c;
    }

    public static Cursor readDatabaseAllRowsOrderByTime(PhotosDBHelper mDBHelper) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] results = {
                PhotosEntry._ID,
                PhotosEntry.COLUMN_NAME_DATA,
                PhotosEntry.COLUMN_NAME_ENTRY_TIME,
                PhotosEntry.COLUMN_NAME_FORMATTED_DATE,
                PhotosEntry.COLUMN_NAME_FORMATTED_STRING};
        Cursor c = db.query(
                PhotosEntry.TABLE_NAME,
                results,
                null,
                null,
                null,
                null,
                PhotosEntry.COLUMN_NAME_ENTRY_TIME);
        return c;
    }

    public static long writeToDatabase(String entryTime, String data, String fDate, String fTime, PhotosDBHelper mDBHelper) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Create a new map of values, where column names are the keys
        values.put(PhotosEntry.COLUMN_NAME_ENTRY_TIME, entryTime);
        values.put(PhotosEntry.COLUMN_NAME_TITLE, "Photo_Data");
        values.put(PhotosEntry.COLUMN_NAME_DATA, data);
        values.put(PhotosEntry.COLUMN_NAME_FORMATTED_DATE, fDate);
        values.put(PhotosEntry.COLUMN_NAME_FORMATTED_STRING, fTime);
        long rowId = db.insert(PhotosEntry.TABLE_NAME, null, values);
        return rowId;
    }

    public static void deleteOnEntryTime(String entryTime, PhotosDBHelper mDBHelper) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        db.delete(PhotosEntry.TABLE_NAME, PhotosEntry.COLUMN_NAME_TITLE + " = '" + entryTime + "'", null);
    }
}
