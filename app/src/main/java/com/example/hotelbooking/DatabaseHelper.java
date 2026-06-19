package com.example.hotelbooking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public static final String DATABASE_NAME = "HotelBooking.db";
    public static final int DATABASE_VERSION = 2;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "username";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_ROLE = "role"; // 'user' or 'admin'

    // Hotels table
    public static final String TABLE_HOTELS = "hotels";
    public static final String COL_HOTEL_ID = "id";
    public static final String COL_HOTEL_NAME = "name";
    public static final String COL_HOTEL_LOCATION = "location";
    public static final String COL_HOTEL_PRICE = "price";
    public static final String COL_HOTEL_ROOMS = "rooms";
    public static final String COL_HOTEL_IMAGE = "image";

    // Bookings table
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COL_BOOKING_ID = "id";
    public static final String COL_BOOKING_USER_ID = "user_id";
    public static final String COL_BOOKING_HOTEL_ID = "hotel_id";
    public static final String COL_BOOKING_STATUS = "status"; // 'booked', 'cancelled'

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_ROLE + " TEXT)";
        db.execSQL(createUsersTable);

        String createHotelsTable = "CREATE TABLE " + TABLE_HOTELS + " (" +
                COL_HOTEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HOTEL_NAME + " TEXT, " +
                COL_HOTEL_LOCATION + " TEXT, " +
                COL_HOTEL_PRICE + " REAL, " +
                COL_HOTEL_ROOMS + " INTEGER, " +
                COL_HOTEL_IMAGE + " TEXT)";
        db.execSQL(createHotelsTable);

        String createBookingsTable = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COL_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BOOKING_USER_ID + " INTEGER, " +
                COL_BOOKING_HOTEL_ID + " INTEGER, " +
                COL_BOOKING_STATUS + " TEXT, " +
                "FOREIGN KEY(" + COL_BOOKING_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "), " +
                "FOREIGN KEY(" + COL_BOOKING_HOTEL_ID + ") REFERENCES " + TABLE_HOTELS + "(" + COL_HOTEL_ID + "))";
        db.execSQL(createBookingsTable);

        // Indices for performance
        db.execSQL("CREATE INDEX idx_user_name ON " + TABLE_USERS + "(" + COL_USER_NAME + ")");
        db.execSQL("CREATE INDEX idx_hotel_location ON " + TABLE_HOTELS + "(" + COL_HOTEL_LOCATION + ")");
        db.execSQL("CREATE INDEX idx_booking_user ON " + TABLE_BOOKINGS + "(" + COL_BOOKING_USER_ID + ")");

        // Add default admin
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, "admin");
        values.put(COL_USER_PASSWORD, "admin123");
        values.put(COL_USER_ROLE, "admin");
        db.insert(TABLE_USERS, null, values);

        // Add some default hotels
        addDefaultHotel(db, "Ocean View", "Miami", 200.0, 10);
        addDefaultHotel(db, "Mountain Retreat", "Denver", 150.0, 5);
        addDefaultHotel(db, "City Central", "New York", 300.0, 20);
    }

    private void addDefaultHotel(SQLiteDatabase db, String name, String location, double price, int rooms) {
        ContentValues values = new ContentValues();
        values.put(COL_HOTEL_NAME, name);
        values.put(COL_HOTEL_LOCATION, location);
        values.put(COL_HOTEL_PRICE, price);
        values.put(COL_HOTEL_ROOMS, rooms);
        values.put(COL_HOTEL_IMAGE, "default.jpg");
        db.insert(TABLE_HOTELS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOTELS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        onCreate(db);
    }

    public boolean addUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, username);
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_ROLE, role);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public Cursor checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_NAME + "=? AND " + COL_USER_PASSWORD + "=?", new String[]{username, password});
    }

    public boolean addHotel(String name, String location, double price, int rooms, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_HOTEL_NAME, name);
        values.put(COL_HOTEL_LOCATION, location);
        values.put(COL_HOTEL_PRICE, price);
        values.put(COL_HOTEL_ROOMS, rooms);
        values.put(COL_HOTEL_IMAGE, image);
        long result = db.insert(TABLE_HOTELS, null, values);
        return result != -1;
    }

    public Cursor getAllHotels() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HOTELS, null);
    }

    public Cursor searchHotels(String location) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HOTELS + " WHERE " + COL_HOTEL_LOCATION + " LIKE ?", new String[]{"%" + location + "%"});
    }

    public boolean updateHotel(int id, String name, String location, double price, int rooms) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_HOTEL_NAME, name);
        values.put(COL_HOTEL_LOCATION, location);
        values.put(COL_HOTEL_PRICE, price);
        values.put(COL_HOTEL_ROOMS, rooms);
        int result = db.update(TABLE_HOTELS, values, COL_HOTEL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteHotel(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_HOTELS, COL_HOTEL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean bookRoom(int userId, int hotelId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COL_HOTEL_ROOMS + " FROM " + TABLE_HOTELS + " WHERE " + COL_HOTEL_ID + "=?", new String[]{String.valueOf(hotelId)});
            if (cursor != null && cursor.moveToFirst()) {
                int roomsIndex = cursor.getColumnIndex(COL_HOTEL_ROOMS);
                if (roomsIndex != -1) {
                    int rooms = cursor.getInt(roomsIndex);
                    if (rooms > 0) {
                        db.execSQL("UPDATE " + TABLE_HOTELS + " SET " + COL_HOTEL_ROOMS + " = " + COL_HOTEL_ROOMS + " - 1 WHERE " + COL_HOTEL_ID + " = " + hotelId);
                        ContentValues values = new ContentValues();
                        values.put(COL_BOOKING_USER_ID, userId);
                        values.put(COL_BOOKING_HOTEL_ID, hotelId);
                        values.put(COL_BOOKING_STATUS, "booked");
                        long result = db.insert(TABLE_BOOKINGS, null, values);
                        return result != -1;
                    }
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    public Cursor getBookingHistory(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h." + COL_HOTEL_NAME + ", h." + COL_HOTEL_LOCATION + ", h." + COL_HOTEL_PRICE + ", b." + COL_BOOKING_STATUS + ", b." + COL_BOOKING_ID +
                " FROM " + TABLE_BOOKINGS + " b JOIN " + TABLE_HOTELS + " h ON b." + COL_BOOKING_HOTEL_ID + " = h." + COL_HOTEL_ID +
                " WHERE b." + COL_BOOKING_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public boolean cancelBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COL_BOOKING_HOTEL_ID + " FROM " + TABLE_BOOKINGS + " WHERE " + COL_BOOKING_ID + "=?", new String[]{String.valueOf(bookingId)});
            if (cursor != null && cursor.moveToFirst()) {
                int hotelIdIndex = cursor.getColumnIndex(COL_BOOKING_HOTEL_ID);
                if (hotelIdIndex != -1) {
                    int hotelId = cursor.getInt(hotelIdIndex);
                    db.execSQL("UPDATE " + TABLE_HOTELS + " SET " + COL_HOTEL_ROOMS + " = " + COL_HOTEL_ROOMS + " + 1 WHERE " + COL_HOTEL_ID + " = " + hotelId);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(COL_BOOKING_STATUS, "cancelled");
        int result = db.update(TABLE_BOOKINGS, values, COL_BOOKING_ID + "=?", new String[]{String.valueOf(bookingId)});
        return result > 0;
    }

    public Cursor getAllBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u." + COL_USER_NAME + ", h." + COL_HOTEL_NAME + ", b." + COL_BOOKING_STATUS +
                " FROM " + TABLE_BOOKINGS + " b " +
                " JOIN " + TABLE_USERS + " u ON b." + COL_BOOKING_USER_ID + " = u." + COL_USER_ID +
                " JOIN " + TABLE_HOTELS + " h ON b." + COL_BOOKING_HOTEL_ID + " = h." + COL_HOTEL_ID;
        return db.rawQuery(query, null);
    }
}
