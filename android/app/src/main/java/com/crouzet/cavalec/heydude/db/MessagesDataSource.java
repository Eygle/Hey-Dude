package com.crouzet.cavalec.heydude.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.crouzet.cavalec.heydude.model.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Johan on 04/03/2015.
 * Database requests management
 */
public class MessagesDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_MESSAGE,
            MySQLiteHelper.COLUMN_AUTH_NAME,
            MySQLiteHelper.COLUMN_DEST_NAME,
            MySQLiteHelper.COLUMN_IMAGE,
            MySQLiteHelper.COLUMN_DATE
    };

    public MessagesDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Create message instance from database query result
     * @param msg message
     * @param authName message author's name
     * @param destName messaage receiver's name
     * @param image author's image
     * @param date date of the message
     * @return new instance of Message
     */
    public Message createMessage(String msg, String authName, String destName, String image, Date date) {
        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.COLUMN_MESSAGE, msg);
        values.put(MySQLiteHelper.COLUMN_AUTH_NAME, authName);
        values.put(MySQLiteHelper.COLUMN_DEST_NAME, destName);
        values.put(MySQLiteHelper.COLUMN_IMAGE, image);
        values.put(MySQLiteHelper.COLUMN_DATE, date.getTime());


        long insertId = database.insert(MySQLiteHelper.TABLE_MESSAGES, null,
                values);

        Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGES,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();

        Message newMsg = cursorToMessage(cursor);
        cursor.close();
        return newMsg;
    }

    /**
     * Remove message from database
     * @param message message to remove
     */
    public void deleteMessage(Message message) {
        System.out.println("Comment deleted with id: " + message.getId());
        database.delete(MySQLiteHelper.TABLE_MESSAGES, MySQLiteHelper.COLUMN_ID
                + " = " + message.getId(), null);
    }

    /**
     * Get all messages from database for a chat
     * @param name author's name
     * @return list of messages
     */
    public List<Message> getAllMessages(String name) {
        List<Message> messages = new ArrayList<>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGES,
                allColumns,
                MySQLiteHelper.COLUMN_AUTH_NAME + " LIKE '" + name + "' or " + MySQLiteHelper.COLUMN_DEST_NAME + " LIKE '" + name + "'",
                null, null, null,
                "date ASC"
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message message = cursorToMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return messages;
    }

    /**
     * Transform cursor to Message isntance
     * @param cursor issued from database query
     * @return new message instance
     */
    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();

        message.setId(cursor.getLong(0));
        message.setMessage(cursor.getString(1));
        message.setAuthorName(cursor.getString(2));
        message.setDestName(cursor.getString(3));
        message.setImage(cursor.getString(4));
        message.setDate(new Date(cursor.getLong(5)));

        return message;
    }
}
