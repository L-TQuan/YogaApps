package com.example.yogaadmin;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddCourseActivity extends AppCompatActivity {

    private EditText editTextName;
    private Spinner spinnerDayOfWeek;
    private EditText editTextTime;
    private EditText editTextCapacity;
    private EditText editTextDuration;
    private EditText editTextPrice;
    private Spinner spinnerClassType;
    private EditText editTextDescription;
    private int courseId = -1; // Initialize courseId
    private DatabaseHelper databaseHelper;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        databaseHelper = new DatabaseHelper(this);
        firebaseHelper = new FirebaseHelper(databaseHelper);
        Intent intent = getIntent();

        // Initialize the views
        editTextName = findViewById(R.id.edit_text_course_name);
        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
        editTextTime = findViewById(R.id.edit_text_time);
        editTextCapacity = findViewById(R.id.edit_text_capacity);
        editTextDuration = findViewById(R.id.edit_text_duration);
        editTextPrice = findViewById(R.id.edit_text_price);
        spinnerClassType = findViewById(R.id.spinner_class_type);
        editTextDescription = findViewById(R.id.edit_text_description);
        Button buttonAddCourse = findViewById(R.id.button_add_course);

        // Check if editing an existing course
        courseId = intent.getIntExtra("courseId", -1);
        if (courseId != -1) {
            loadCourseData(courseId);
            buttonAddCourse.setText("Update");
        }

        // Set up the button click listener
        buttonAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCourseData();
            }
        });
    }

    private void loadCourseData(int courseId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = null;
        try {
            cursor = dbHelper.getCourseById(courseId);
            if (cursor != null && cursor.moveToFirst()) {
                // Populate the form with the course data
                setTextIfExists(cursor, DatabaseHelper.COLUMN_NAME, editTextName);
                setTextIfExists(cursor, DatabaseHelper.COLUMN_TIME, editTextTime);
                setTextIfExists(cursor, DatabaseHelper.COLUMN_CAPACITY, editTextCapacity);
                setTextIfExists(cursor, DatabaseHelper.COLUMN_DURATION, editTextDuration);
                setTextIfExists(cursor, DatabaseHelper.COLUMN_PRICE, editTextPrice);
                setTextIfExists(cursor, DatabaseHelper.COLUMN_DESCRIPTION, editTextDescription);

                // Set spinners for dayOfWeek and classType
                setupSpinner(spinnerDayOfWeek, getValueIfExists(cursor, DatabaseHelper.COLUMN_DAY_OF_WEEK), R.array.DayOfWeek);
                setupSpinner(spinnerClassType, getValueIfExists(cursor, DatabaseHelper.COLUMN_TYPE), R.array.ClassType);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setTextIfExists(Cursor cursor, String columnName, EditText editText) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            editText.setText(cursor.getString(index));
        }
    }

    private String getValueIfExists(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            return cursor.getString(index);
        }
        return null;
    }


    private void setupSpinner(Spinner spinner, String selectedValue, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayResId, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        int position = adapter.getPosition(selectedValue);
        spinner.setSelection(position);
    }

    private void saveCourseData() {
        // Get the user input
        String name = editTextName.getText().toString();
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        String time = editTextTime.getText().toString();
        String capacityStr = editTextCapacity.getText().toString();
        String durationStr = editTextDuration.getText().toString();
        String priceStr = editTextPrice.getText().toString();
        String classType = spinnerClassType.getSelectedItem().toString();
        String description = editTextDescription.getText().toString();

        // Validate input
        String validationError = validateInput(name, time, capacityStr, durationStr, priceStr);
        if (validationError != null) {
            Toast.makeText(AddCourseActivity.this, validationError, Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        int duration = Integer.parseInt(durationStr);
        double price = Double.parseDouble(priceStr);

        boolean isSynced = false;
        if (courseId != -1) {
            Cursor cursor = databaseHelper.getCourseById(courseId);
            if (cursor != null && cursor.moveToFirst()) {
                int isSyncedIndex = cursor.getColumnIndex("isSynced");
                if (isSyncedIndex != -1) {
                    isSynced = cursor.getInt(isSyncedIndex) == 1;
                }
                cursor.close();
            }
        }

        // Create a Course object
        Course course = new Course(name, dayOfWeek, time, capacity, duration, price, classType, description, isSynced);

        // Save the course data to the database
        DatabaseHelper dbHelper = new DatabaseHelper(AddCourseActivity.this);
        if (courseId != -1) {
            course.setId(courseId);

            if (course.getSynced()) {
                // Sync with Firestore if already synced
                firebaseHelper.updateCourse(course);
                Log.d("FirebaseHelper", "Course updated successfully");
            }
            dbHelper.updateCourse(course);
        } else {
            dbHelper.insertCourse(course);
        }

        // Display the course data in a toast
        Toast.makeText(AddCourseActivity.this, "Course saved", Toast.LENGTH_SHORT).show();

        finish();
    }

    private String validateInput(String name, String time, String capacityStr, String durationStr, String priceStr) {
        String timePattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        if (!time.matches(timePattern)) {
            return "Invalid time format. Please use HH:mm (e.g., 10:30)";
        }
        if (name.trim().isEmpty()) {
            return "Name is required.";
        }
        if (capacityStr.trim().isEmpty()) {
            return "Capacity is required.";
        }
        if (durationStr.trim().isEmpty()) {
            return "Duration is required.";
        }
        if (priceStr.trim().isEmpty()) {
            return "Price is required.";
        }

        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) return "Capacity must be > 0";
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) return "Duration must be > 0";
            double price = Double.parseDouble(priceStr);
            if (price < 0) return "Price must be >= 0";
        } catch (NumberFormatException e) {
            return "Invalid number format";
        }
        return null; // Return null if everything is fine
    }
}
