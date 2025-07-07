package com.example.yogaadmin;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Course implements Serializable {
    private int id; // Primary Key
    private String name;
    public Boolean isSynced;
    private String dayOfWeek;
    private String time;
    private int capacity;
    private int duration;
    private double price;
    private String type;
    private String description;

    // Constructor
    public Course(String name, String dayOfWeek, String time, int capacity, int duration, double price, String type, String description, Boolean isSynced) {
        this.name = name;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.type = type;
        this.description = description;
        this.isSynced = isSynced;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return "Course{" +
                "dayOfWeek='" + dayOfWeek + '\'' +
                ", time='" + time + '\'' +
                ", capacity='" + capacity + '\'' +
                ", duration='" + duration + '\'' +
                ", price='" + price + '\'' +
                ", classType='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
