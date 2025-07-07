package com.example.cw_yoga;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddCourseActivity extends AppCompatActivity {

    private EditText etDayOfWeek, etTime, etCapacity, etDuration, etPrice, etType, etDescription;
    private Button btnSaveCourse;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI elements
        etDayOfWeek = findViewById(R.id.etDayOfWeek);
        etTime = findViewById(R.id.etTime);
        etCapacity = findViewById(R.id.etCapacity);
        etDuration = findViewById(R.id.etDuration);
        etPrice = findViewById(R.id.etPrice);
        etType = findViewById(R.id.etType);
        etDescription = findViewById(R.id.etDescription);
        btnSaveCourse = findViewById(R.id.btnSaveCourse);

        // Set click listener for the Save button
        btnSaveCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCourseToDatabase();
            }
        });
    }

    // Method to save course details to the database
    private void saveCourseToDatabase() {
        String dayOfWeek = etDayOfWeek.getText().toString();
        String time = etTime.getText().toString();
        String capacity = etCapacity.getText().toString();
        String duration = etDuration.getText().toString();
        String price = etPrice.getText().toString();
        String type = etType.getText().toString();
        String description = etDescription.getText().toString();

        // Check if all required fields are filled
        if (TextUtils.isEmpty(dayOfWeek) || TextUtils.isEmpty(time) || TextUtils.isEmpty(capacity) ||
                TextUtils.isEmpty(duration) || TextUtils.isEmpty(price) || TextUtils.isEmpty(type)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert course into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DAY_OF_WEEK, dayOfWeek);
        values.put(DatabaseHelper.COLUMN_TIME, time);
        values.put(DatabaseHelper.COLUMN_CAPACITY, Integer.parseInt(capacity));
        values.put(DatabaseHelper.COLUMN_DURATION, Integer.parseInt(duration));
        values.put(DatabaseHelper.COLUMN_PRICE, Double.parseDouble(price));
        values.put(DatabaseHelper.COLUMN_TYPE, type);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);

        long newRowId = db.insert(DatabaseHelper.TABLE_COURSES, null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Error adding course", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to clear the fields after saving
    private void clearFields() {
        etDayOfWeek.setText("");
        etTime.setText("");
        etCapacity.setText("");
        etDuration.setText("");
        etPrice.setText("");
        etType.setText("");
        etDescription.setText("");
    }
}