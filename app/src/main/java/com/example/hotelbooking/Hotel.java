package com.example.hotelbooking;

public class Hotel {
    private int id;
    private String name;
    private String location;
    private double price;
    private int rooms;
    private String image;

    public Hotel(int id, String name, String location, double price, int rooms, String image) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.price = price;
        this.rooms = rooms;
        this.image = image;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public int getRooms() { return rooms; }
    public String getImage() { return image; }
}
