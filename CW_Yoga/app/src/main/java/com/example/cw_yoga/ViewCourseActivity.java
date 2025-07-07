package com.example.cw_yoga;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViewCourseActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCourses;
    private Button btnBackToMenu;
    private DatabaseHelper dbHelper;
    private ArrayList<YogaCourse> courseList;
    private CourseAdapter courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_course);

        // Initialize views
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // Set up RecyclerView
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database and list
        dbHelper = new DatabaseHelper(this);
        courseList = new ArrayList<>();

        // Load courses from database
        loadCoursesFromDatabase();

        // Set up adapter
        courseAdapter = new CourseAdapter(courseList, this);
        recyclerViewCourses.setAdapter(courseAdapter);

        // Set up back button listener
        btnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Return to the previous screen
            }
        });
    }

    // Load courses from the database
    private void loadCoursesFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_COURSES, // Table name
                null, // All columns
                null, // Where clause
                null, // Arguments
                null, // Group by
                null, // Having
                null  // Order by
        );

        // Iterate through the cursor and add each course to the list
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DAY_OF_WEEK));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME));
            int capacity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAPACITY));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));

            // Add course to list using existing YogaCourse class
            YogaCourse course = new YogaCourse(dayOfWeek, time, capacity, duration, price, type, description);
            course.setId(id);
            courseList.add(course);
        }
        cursor.close();
    }
}