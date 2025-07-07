package com.example.yogaadmin;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private Button createCourseButton, uploadButton, allClassButton;
    private DatabaseHelper databaseHelper;
    private FirebaseHelper firebaseHelper;

    private final ActivityResultLauncher<Intent> addCourseActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // Reload courses from the database
                            loadCoursesFromDatabase();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        createCourseButton = findViewById(R.id.button);
        uploadButton = findViewById(R.id.button2);
        allClassButton = findViewById(R.id.button3);

        // Set up RecyclerView
        databaseHelper = new DatabaseHelper(this);
        firebaseHelper = new FirebaseHelper(databaseHelper);
        courseAdapter = new CourseAdapter(this, firebaseHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(courseAdapter);

        // Load courses from database on creation
        loadCoursesFromDatabase();

        // Create Course button click listener
        createCourseButton.setOnClickListener(v -> {
            // Start AddCourseActivity
            Intent intent = new Intent(MainActivity.this, AddCourseActivity.class);
            addCourseActivityLauncher.launch(intent);
        });

        // Upload to Firebase button click listener (placeholder)
        uploadButton.setOnClickListener(v -> {
            if (NetworkHelper.isNetworkAvailable(getApplicationContext())) {
                syncCoursesToFirebase();
            } else {
                Toast.makeText(getApplicationContext(), "No internet connection. Please connect to a network and try again.", Toast.LENGTH_SHORT).show();
            }
        });

        allClassButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, YogaClassListActivity.class);
            startActivity(intent);
        });
    }

    private void syncCoursesToFirebase() {
        Log.d("MainActivity", "syncCoursesToFirebase called");
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        FirebaseHelper firebaseHelper = new FirebaseHelper(databaseHelper);
        Cursor cursor = databaseHelper.getAllCourses(); // Get the Cursor

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    // Get the columns
                    int columnIndexId = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                    int columnIndexName = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
                    int columnIndexDayOfWeek = cursor.getColumnIndex(DatabaseHelper.COLUMN_DAY_OF_WEEK);
                    int columnIndexTime = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIME);
                    int columnIndexCapacity = cursor.getColumnIndex(DatabaseHelper.COLUMN_CAPACITY);
                    int columnIndexDuration = cursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION);
                    int columnIndexPrice = cursor.getColumnIndex(DatabaseHelper.COLUMN_PRICE);
                    int columnIndexType = cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE);
                    int columnIndexDescription = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION);
                    int columnIndexIsSynced = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_SYNCED);

                    // Create a course object
                    Course course = new Course(
                            cursor.getString(columnIndexName),
                            cursor.getString(columnIndexDayOfWeek),
                            cursor.getString(columnIndexTime),
                            cursor.getInt(columnIndexCapacity),
                            cursor.getInt(columnIndexDuration),
                            cursor.getDouble(columnIndexPrice),
                            cursor.getString(columnIndexType),
                            cursor.getString(columnIndexDescription),
                            cursor.getInt(columnIndexIsSynced) == 1
                    );
                    course.setId(cursor.getInt(columnIndexId)); // Set course ID

                    // Sync the course if it hasn't been synced
                    if (!course.getSynced()) {
                        // Sync the course to Firestore
                        firebaseHelper.syncCourse(course, databaseHelper);

                        // Sync related yoga classes
                        firebaseHelper.syncYogaClasses(course.getId());

                        // Update the isSynced status in the local database
                        databaseHelper.updateCourseSyncStatus(course.getId(), true); // Assuming you have this method
                    }

                    loadCoursesFromDatabase();
                }
            } finally {
                cursor.close(); // Ensure the cursor is closed
            }
        }
    }

    public void loadCoursesFromDatabase() {
        courseAdapter.loadCoursesFromDatabase();
        courseAdapter.notifyDataSetChanged();
    }

    // Reload courses when activity resumes
    @Override
    protected void onResume() {
        super.onResume();
        loadCoursesFromDatabase(); // Ensures the list is updated when returning to the activity
    }
}

