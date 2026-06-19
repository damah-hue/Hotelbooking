package com.example.hotelbooking;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    EditText etName, etLocation, etPrice, etRooms;
    Button btnAdd;
    ListView lvBookings, lvHotels;
    DatabaseHelper db;
    List<String> bookingList, hotelDisplayList;
    List<Hotel> hotelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = DatabaseHelper.getInstance(this);
        etName = findViewById(R.id.etAdminHotelName);
        etLocation = findViewById(R.id.etAdminLocation);
        etPrice = findViewById(R.id.etAdminPrice);
        etRooms = findViewById(R.id.etAdminRooms);
        btnAdd = findViewById(R.id.btnAddHotel);
        lvBookings = findViewById(R.id.lvAdminBookings);
        lvHotels = findViewById(R.id.lvAdminHotels);

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String loc = etLocation.getText().toString();
            String priceStr = etPrice.getText().toString();
            String roomsStr = etRooms.getText().toString();

            if (name.isEmpty() || loc.isEmpty() || priceStr.isEmpty() || roomsStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    double price = Double.parseDouble(priceStr);
                    int rooms = Integer.parseInt(roomsStr);
                    if (db.addHotel(name, loc, price, rooms, "default.jpg")) {
                        Toast.makeText(this, "Hotel added", Toast.LENGTH_SHORT).show();
                        clearFields();
                        loadData();
                    } else {
                        Toast.makeText(this, "Failed to add", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvHotels.setOnItemClickListener((parent, view, position, id) -> {
            showHotelOptions(hotelList.get(position));
        });

        loadData();
    }

    private void clearFields() {
        etName.setText("");
        etLocation.setText("");
        etPrice.setText("");
        etRooms.setText("");
    }

    private void loadData() {
        loadAllBookings();
        loadAllHotels();
    }

    private void loadAllHotels() {
        hotelList = new ArrayList<>();
        hotelDisplayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.getAllHotels();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_ID);
                    int nameIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_NAME);
                    int locIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_LOCATION);
                    int priceIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_PRICE);
                    int roomsIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_ROOMS);
                    int imgIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_IMAGE);

                    if (idIndex != -1 && nameIndex != -1 && locIndex != -1 && priceIndex != -1 && roomsIndex != -1 && imgIndex != -1) {
                        Hotel hotel = new Hotel(
                                cursor.getInt(idIndex),
                                cursor.getString(nameIndex),
                                cursor.getString(locIndex),
                                cursor.getDouble(priceIndex),
                                cursor.getInt(roomsIndex),
                                cursor.getString(imgIndex)
                        );
                        hotelList.add(hotel);
                        hotelDisplayList.add(hotel.getName() + " (" + hotel.getLocation() + ") - Shs " + hotel.getPrice() + " [" + hotel.getRooms() + " rooms]");
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hotelDisplayList);
        lvHotels.setAdapter(adapter);
    }

    private void loadAllBookings() {
        bookingList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.getAllBookings();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int userIndex = cursor.getColumnIndex(DatabaseHelper.COL_USER_NAME);
                    int hotelIndex = cursor.getColumnIndex(DatabaseHelper.COL_HOTEL_NAME);
                    int statusIndex = cursor.getColumnIndex(DatabaseHelper.COL_BOOKING_STATUS);

                    if (userIndex != -1 && hotelIndex != -1 && statusIndex != -1) {
                        bookingList.add(cursor.getString(userIndex) + " booked " + cursor.getString(hotelIndex) + " (" + cursor.getString(statusIndex) + ")");
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookingList);
        lvBookings.setAdapter(adapter);
    }

    private void showHotelOptions(Hotel hotel) {
        String[] options = {"Edit Hotel Details", "Delete Hotel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(hotel.getName());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Edit Details
                showEditHotelDialog(hotel);
            } else {
                // Delete
                new AlertDialog.Builder(this)
                        .setTitle("Delete Hotel")
                        .setMessage("Are you sure you want to delete " + hotel.getName() + "?")
                        .setPositiveButton("Yes", (d, w) -> {
                            if (db.deleteHotel(hotel.getId())) {
                                Toast.makeText(this, "Hotel deleted", Toast.LENGTH_SHORT).show();
                                loadData();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        builder.show();
    }

    private void showEditHotelDialog(Hotel hotel) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etEditName = new EditText(this);
        etEditName.setHint("Name");
        etEditName.setText(hotel.getName());
        layout.addView(etEditName);

        final EditText etEditLocation = new EditText(this);
        etEditLocation.setHint("Location");
        etEditLocation.setText(hotel.getLocation());
        layout.addView(etEditLocation);

        final EditText etEditPrice = new EditText(this);
        etEditPrice.setHint("Price");
        etEditPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etEditPrice.setText(String.valueOf(hotel.getPrice()));
        layout.addView(etEditPrice);

        final EditText etEditRooms = new EditText(this);
        etEditRooms.setHint("Available Rooms");
        etEditRooms.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etEditRooms.setText(String.valueOf(hotel.getRooms()));
        layout.addView(etEditRooms);

        new AlertDialog.Builder(this)
                .setTitle("Edit Hotel")
                .setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etEditName.getText().toString();
                    String loc = etEditLocation.getText().toString();
                    String priceStr = etEditPrice.getText().toString();
                    String roomsStr = etEditRooms.getText().toString();

                    if (!name.isEmpty() && !loc.isEmpty() && !priceStr.isEmpty() && !roomsStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            int rooms = Integer.parseInt(roomsStr);
                            if (db.updateHotel(hotel.getId(), name, loc, price, rooms)) {
                                Toast.makeText(this, "Hotel updated", Toast.LENGTH_SHORT).show();
                                loadData();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Invalid numbers", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
