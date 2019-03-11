package com.murphy.pokotalk.data.file;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PokoDatabaseHelper {
    public static Cursor readSessionData(SQLiteDatabase db) {
        return query(db, PokoDatabaseQuery.readSessionData, null);
    }

    public static Cursor readAllUserData(SQLiteDatabase db) {
        return db.rawQuery(PokoDatabaseQuery.readAllUserData, null);
    }

    public static Cursor readGroupData(SQLiteDatabase db) {
        return query(db, PokoDatabaseQuery.readGroupData, null);
    }

    public static Cursor readGroupMemberData(SQLiteDatabase db, int groupId) {
        String[] selectionArgs = {Integer.toString(groupId)};

        return query(db, PokoDatabaseQuery.readGroupMemberData, selectionArgs);
    }

    public static Cursor readMessageData(SQLiteDatabase db, int groupId, int offset, int num) {
        PokoDatabaseQuery query = PokoDatabaseQuery.readGroupData;
        String[] selectionArgs = {Integer.toString(groupId)};
        String limitOffset = Integer.toString(offset) + ", " + Integer.toString(num);

        return db.query(query.table, query.projection, query.selection, selectionArgs,
                null, null, query.sortOrder, limitOffset);
    }

    public static Cursor query(SQLiteDatabase db, PokoDatabaseQuery query, String[] selectionArgs) {
        Cursor cursor = db.query(query.table, query.projection, query.selection, selectionArgs,
                null, null, query.sortOrder, query.limitOffset);

        return cursor;
    }

    public static long insert(SQLiteDatabase db, PokoDatabaseQuery query, ContentValues values) {
        long result = db.insert(query.table, null, values);

        return result;
    }

    public static long update(SQLiteDatabase db, PokoDatabaseQuery query, ContentValues contentValues,
                              String[] selectionArgs) {
        long result = db.update(query.table, contentValues, query.selection, selectionArgs);

        return result;
    }

    public static long delete(SQLiteDatabase db, PokoDatabaseQuery query, String[] selectionArgs) {
        long result = db.delete(query.table, query.selection, selectionArgs);

        return result;
    }
}
