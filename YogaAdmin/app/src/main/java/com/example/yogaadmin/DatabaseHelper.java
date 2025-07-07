package com.example.yogaadmin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "courses.db";

    //Table for courses
    private static final String TABLE_COURSES = "courses";

    // Columns for courses
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DAY_OF_WEEK = "day_of_week";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CAPACITY = "capacity";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IS_SYNCED = "isSynced";

    // Table for yoga classes
    private static final String TABLE_YOGA_CLASSES = "yoga_classes";

    // Columns for yoga classes
    public static final String COLUMN_CLASS_ID = "class_id";
    public static final String COLUMN_CLASS_DATE = "class_date"; // Format: yyyy/MM/dd
    public static final String COLUMN_CLASS_TEACHER = "teacher";
    public static final String COLUMN_CLASS_COMMENTS = "comments";
    public static final String COLUMN_COURSE_REF_ID = "course_ref_id"; // Foreign key

    // SQL queries to create tables
    private static final String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_DAY_OF_WEEK + " TEXT, " +
            COLUMN_TIME + " TEXT, " +
            COLUMN_CAPACITY + " INTEGER, " +
            COLUMN_DURATION + " INTEGER, " +
            COLUMN_PRICE + " REAL, " +
            COLUMN_TYPE + " TEXT, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_IS_SYNCED + " INTEGER DEFAULT 0)";

    private static final String CREATE_YOGA_CLASSES_TABLE = "CREATE TABLE " + TABLE_YOGA_CLASSES + " (" +
            COLUMN_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CLASS_DATE + " TEXT, " +
            COLUMN_CLASS_TEACHER + " TEXT, " +
            COLUMN_CLASS_COMMENTS + " TEXT, " +
            COLUMN_COURSE_REF_ID + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_COURSE_REF_ID + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_YOGA_CLASSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        onCreate(db);
    }

    // Insert course into database
    public void insertCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, course.getName());
        values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COLUMN_TIME, course.getTime());
        values.put(COLUMN_CAPACITY, course.getCapacity());
        values.put(COLUMN_DURATION, course.getDuration());
        values.put(COLUMN_PRICE, course.getPrice());
        values.put(COLUMN_TYPE, course.getType());
        values.put(COLUMN_DESCRIPTION, course.getDescription());
        db.insert(TABLE_COURSES, null, values);
        db.close();
    }

    // Get all courses from database
    public Cursor getAllCourses() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_COURSES;
        return db.rawQuery(query, null);
    }

    // Get course by ID from database
    public Cursor getCourseById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_COURSES + " WHERE " + COLUMN_ID + " = ?";
        String[] args = {String.valueOf(id)};
        return db.rawQuery(query, args);
    }

    // Update course in database
    public void updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, course.getName());
        values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COLUMN_TIME, course.getTime());
        values.put(COLUMN_CAPACITY, course.getCapacity());
        values.put(COLUMN_DURATION, course.getDuration());
        values.put(COLUMN_PRICE, course.getPrice());
        values.put(COLUMN_TYPE, course.getType());
        values.put(COLUMN_DESCRIPTION, course.getDescription());
        db.update(TABLE_COURSES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(course.getId())});
        db.close();
    }

    // Delete course from database
    public void deleteCourse(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete yoga classes tied to the course
        db.delete(TABLE_YOGA_CLASSES, COLUMN_COURSE_REF_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_COURSES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public String getCourseNameById(int courseId) {
        String courseName = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_COURSES,
                new String[]{COLUMN_NAME},  // Column want to retrieve
                COLUMN_ID + " = ?",  // Where clause
                new String[]{String.valueOf(courseId)},  // Arguments for the where clause
                null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int courseNameIndex = cursor.getColumnIndex(COLUMN_NAME);
                if (courseNameIndex != -1) {
                    courseName = cursor.getString(courseNameIndex);
                }
            }
            cursor.close();
        }
        return courseName;
    }


    // Update course sync status
    public void updateCourseSyncStatus(int courseId, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_SYNCED, isSynced ? 1 : 0); // (1 for true, 0 for false)
        db.update("courses", values, "id = ?", new String[]{String.valueOf(courseId)});
    }

    // Methods for managing yoga classes
    public void insertYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_DATE, yogaClass.getDate()); // Date as dd/MM/yyyy
        values.put(COLUMN_CLASS_TEACHER, yogaClass.getTeacher());
        values.put(COLUMN_CLASS_COMMENTS, yogaClass.getComments());
        values.put(COLUMN_COURSE_REF_ID, yogaClass.getCourseId());
        db.insert(TABLE_YOGA_CLASSES, null, values);
        db.close();
    }

    // Get yoga class by ID from database
    public Cursor getYogaClassById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_YOGA_CLASSES + " WHERE " + COLUMN_CLASS_ID + " = ?";
        String[] args = {String.valueOf(id)};
        return db.rawQuery(query, args);
    }

    public Cursor searchYogaClassesByTeacher(String teacherName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_YOGA_CLASSES + " WHERE " + COLUMN_CLASS_TEACHER + " LIKE ?";
        String[] args = {teacherName + "%"}; // Use LIKE for partial matching
        return db.rawQuery(query, args);
    }

    public Cursor searchYogaClassesByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_YOGA_CLASSES + " WHERE " + COLUMN_CLASS_DATE + " = ?";
        String[] args = {date};
        return db.rawQuery(query, args);
    }

    public Cursor searchYogaClassesByDayOfWeek(String dayOfWeek) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_YOGA_CLASSES + " INNER JOIN " + TABLE_COURSES +
                " ON " + TABLE_YOGA_CLASSES + "." + COLUMN_COURSE_REF_ID + " = " + TABLE_COURSES + "." + COLUMN_ID +
                " WHERE " + COLUMN_DAY_OF_WEEK + " = ?";
        String[] args = {dayOfWeek};
        return db.rawQuery(query, args);
    }

    public Cursor getAllYogaClasses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_YOGA_CLASSES, null);
    }

    public Cursor getYogaClassesByCourse(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_YOGA_CLASSES + " WHERE " + COLUMN_COURSE_REF_ID + " = ?", new String[]{String.valueOf(courseId)});
    }

    public void updateYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_DATE, yogaClass.getDate());
        values.put(COLUMN_CLASS_TEACHER, yogaClass.getTeacher());
        values.put(COLUMN_CLASS_COMMENTS, yogaClass.getComments());
        int rowsUpdated = db.update(TABLE_YOGA_CLASSES, values, COLUMN_CLASS_ID + " = ?", new String[]{String.valueOf(yogaClass.getId())});
        Log.d("DatabaseHelper", "Updated " + rowsUpdated + " rows");
        db.close();
    }

    public void deleteYogaClass(int classId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_YOGA_CLASSES, COLUMN_CLASS_ID + " = ?", new String[]{String.valueOf(classId)});
        Log.d("DatabaseHelper", "Deleted " + rowsDeleted + " rows");
        db.close();
    }
}
