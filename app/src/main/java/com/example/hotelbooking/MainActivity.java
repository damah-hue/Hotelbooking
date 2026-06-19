package com.example.hotelbooking;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    HotelAdapter adapter;
    List<Hotel> hotelList;
    DatabaseHelper db;
    SearchView searchView;
    Button btnHistory;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            // Redirect to login if userId is missing
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        db = DatabaseHelper.getInstance(this);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        btnHistory = findViewById(R.id.btnHistory);

        hotelList = new ArrayList<>();
        adapter = new HotelAdapter(hotelList, hotel -> {
            if (userId == -1) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }
            // Book room on click for simplicity in this demo
            boolean success = db.bookRoom(userId, hotel.getId());
            if (success) {
                Toast.makeText(MainActivity.this, "Booked " + hotel.getName(), Toast.LENGTH_SHORT).show();
                loadHotels(searchView.getQuery().toString()); // Refresh
            } else {
                Toast.makeText(MainActivity.this, "Booking failed or No rooms available", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadHotels("");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadHotels(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadHotels(newText);
                return false;
            }
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    private void loadHotels(String location) {
        hotelList.clear();
        Cursor cursor = null;
        try {
            if (location.isEmpty()) {
                cursor = db.getAllHotels();
            } else {
                cursor = db.searchHotels(location);
            }

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_ID);
                    int nameIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_NAME);
                    int locIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_LOCATION);
                    int priceIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_PRICE);
                    int roomsIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_ROOMS);
                    int imgIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_IMAGE);

                    if (idIndex != -1 && nameIndex != -1 && locIndex != -1 && priceIndex != -1 && roomsIndex != -1 && imgIndex != -1) {
                        hotelList.add(new Hotel(
                                cursor.getInt(idIndex),
                                cursor.getString(nameIndex),
                                cursor.getString(locIndex),
                                cursor.getDouble(priceIndex),
                                cursor.getInt(roomsIndex),
                                cursor.getString(imgIndex)
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