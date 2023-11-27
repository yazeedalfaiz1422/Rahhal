package com.example.rahhal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Vista_DB";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the necessary tables
        String createTableQuery = "CREATE TABLE IF NOT EXISTS landmarkTable " +
                "(picture_path TEXT, title TEXT PRIMARY KEY ON CONFLICT REPLACE, paragraph TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if needed
    }

    public void insertData(String picturePath, String title, String paragraph) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("picture_path", picturePath);
        contentValues.put("title", title);
        contentValues.put("paragraph", paragraph);
        db.insert("landmarkTable", null, contentValues);
    }

    public String[] getRowByPath(String path) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {"picture_path", "title", "paragraph"};
        String selection = "picture_path = ?";
        String[] selectionArgs = {path};
        Cursor cursor = db.query("landmarkTable", projection, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            int picturePathIndex = cursor.getColumnIndex("picture_path");
            int titleIndex = cursor.getColumnIndex("title");
            int paragraphIndex = cursor.getColumnIndex("paragraph");
            if (picturePathIndex != -1 && titleIndex != -1 && paragraphIndex != -1) {
                String picturePath = cursor.getString(picturePathIndex);
                String retrievedTitle = cursor.getString(titleIndex);
                String paragraph = cursor.getString(paragraphIndex);
                cursor.close();
                return new String[]{picturePath, retrievedTitle, paragraph};
            }
        }
        cursor.close();
        return null;
    }

    public List<String> getAllImagePaths() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {"picture_path"};
        Cursor cursor = db.query("landmarkTable", projection, null, null, null, null, null);

        List<String> imagePaths = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int picturePathIndex = cursor.getColumnIndex("picture_path");
            if (picturePathIndex != -1) {
                do {
                    String imagePath = cursor.getString(picturePathIndex);
                    imagePaths.add(imagePath);
                } while (cursor.moveToNext());
            }
        }

        cursor.close();
        return imagePaths;
    }

    public void deleteRow(String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        String tableName = "landmarkTable";
        String primaryKeyColumnName = "picture_path";

        String selection = primaryKeyColumnName + " = ?";
        String[] selectionArgs = { String.valueOf(imagePath) };

        db.delete(tableName, selection, selectionArgs);
        deleteImage(imagePath);
        db.close();
    }

    private void deleteImage(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            if (file.delete()) {
                // Image file deleted successfully
                Log.d("DeleteImage", "Image file deleted: " + imagePath);
            } else {
                // Failed to delete the image file
                Log.d("DeleteImage", "Failed to delete image file: " + imagePath);
            }
        } else {
            // Image file does not exist
            Log.d("DeleteImage", "Image file not found: " + imagePath);
        }
    }

}