package com.example.cw_yoga;

public class YogaClassInstance {
    private int id;  // Primary Key
    private int courseId; // Foreign Key to YogaCourse
    private String date;
    private String teacher;
    private String comments;

    public YogaClassInstance(int courseId, String date, String teacher, String comments) {
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}

