package com.example.hotelbooking;

public class Booking {
    private int id;
    private String hotelName;
    private String location;
    private String status;
    private double price;

    public Booking(int id, String hotelName, String location, String status, double price) {
        this.id = id;
        this.hotelName = hotelName;
        this.location = location;
        this.status = status;
        this.price = price;
    }

    public int getId() { return id; }
    public String getHotelName() { return hotelName; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }
}