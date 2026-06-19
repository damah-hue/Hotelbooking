package com.example.hotelbooking;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    BookingAdapter adapter;
    List<Booking> bookingList;
    DatabaseHelper db;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        userId = getIntent().getIntExtra("USER_ID", -1);
        db = DatabaseHelper.getInstance(this);
        recyclerView = findViewById(R.id.rvHistory);

        bookingList = new ArrayList<>();
        loadHistory();

        adapter = new BookingAdapter(bookingList, bookingId -> {
            if (db.cancelBooking(bookingId)) {
                Toast.makeText(HistoryActivity.this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                loadHistory();
            } else {
                Toast.makeText(HistoryActivity.this, "Failed to cancel", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadHistory() {
        bookingList.clear();
        Cursor cursor = null;
        try {
            cursor = db.getBookingHistory(userId);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int nameIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_NAME);
                    int locIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_LOCATION);
                    int priceIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_PRICE);
                    int statusIndex = cursor.getColumnIndex(DatabaseHelper.COL_BOOKING_STATUS);
                    int idIndex = cursor.getColumnIndex(DatabaseHelper.COL_BOOKING_ID);

                    if (nameIndex != -1 && locIndex != -1 && priceIndex != -1 && statusIndex != -1 && idIndex != -1) {
                        bookingList.add(new Booking(
                                cursor.getInt(idIndex),
                                cursor.getString(nameIndex),
                                cursor.getString(locIndex),
                                cursor.getString(statusIndex),
                                cursor.getDouble(priceIndex)
                        ));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
