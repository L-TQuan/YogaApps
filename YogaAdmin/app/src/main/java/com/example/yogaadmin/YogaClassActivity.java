package com.example.yogaadmin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class YogaClassActivity extends AppCompatActivity {

    private TextView courseNameTextView;
    private EditText editTextDate, editTextTeacher, editTextComments;
    private Button btnSaveYogaClass;
    private int selectedClassId = -1;

    private RecyclerView recyclerView;
    private YogaClassAdapter yogaClassAdapter;
    private DatabaseHelper dbHelper;

    private int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_yoga_class);

        dbHelper = new DatabaseHelper(this);

        // Get the course ID from the intent
        courseId = getIntent().getIntExtra("courseId", 0);

        // Initialize the course name TextView
        courseNameTextView = findViewById(R.id.textView2);
        loadCourseName(courseId);

        // Get the class ID from the intent
        selectedClassId = getIntent().getIntExtra("yogaClassId", -1);

        // Initialize the form fields
        editTextDate = findViewById(R.id.editTextDate);
        editTextTeacher = findViewById(R.id.editTextTeacher);
        editTextComments = findViewById(R.id.editTextComments);
        btnSaveYogaClass = findViewById(R.id.btnSaveYogaClass);

        // Check if an existing class is being edited
        selectedClassId = getIntent().getIntExtra("yogaClassId", -1);
        if (selectedClassId != -1) {
            loadYogaClassDetails(selectedClassId);
        }


        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        yogaClassAdapter = new YogaClassAdapter(this, new ArrayList<YogaClass>());
        recyclerView.setAdapter(yogaClassAdapter);

        // Load the yoga class list from the database
        loadYogaClassList();

        // Set the date field to match the day of week of the course
        editTextDate.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        // Set the save button click listener
        btnSaveYogaClass.setOnClickListener(v -> {
            String date = editTextDate.getText().toString();
            String teacher = editTextTeacher.getText().toString();
            String comments = editTextComments.getText().toString();

            if (date.isEmpty()) {
                editTextDate.setError("Date is required");
                editTextDate.requestFocus();
                return;
            }
            if (teacher.isEmpty()) {
                editTextTeacher.setError("Teacher is required");
                editTextTeacher.requestFocus();
                return;
            }

            FirebaseHelper firebaseHelper = new FirebaseHelper(dbHelper);
            Cursor courseCursor = dbHelper.getCourseById(courseId);

            if (courseCursor != null && courseCursor.moveToFirst()) {
                // Get the synced status from the cursor
                int isSyncedIndex = courseCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_SYNCED);
                if (isSyncedIndex != -1) {
                    boolean isSynced = courseCursor.getInt(isSyncedIndex) == 1;

                    if (selectedClassId == -1) {
                        // Create a new yoga class object
                        YogaClass yogaClass = new YogaClass(courseId, date, teacher, comments);

                        // Insert the yoga class into the database
                        dbHelper.insertYogaClass(yogaClass);

                        if (isSynced) {
                            firebaseHelper.syncYogaClasses(courseId);
                        }
                        // Load the updated yoga class list from the database
                        loadYogaClassList();
                    } else {
                        // Update the selected yoga class object
                        YogaClass yogaClass = new YogaClass(courseId, date, teacher, comments);
                        yogaClass.setId(selectedClassId);
                        yogaClass.setDate(date);
                        yogaClass.setTeacher(teacher);
                        yogaClass.setComments(comments);

                        // Update the yoga class in the database
                        dbHelper.updateYogaClass(yogaClass);

                        if (isSynced) {
                            firebaseHelper.updateYogaClass(courseId, yogaClass);
                        }

                        // Load the updated yoga class list from the database
                        loadYogaClassList();

                        // Reset the selected class ID
                        selectedClassId = -1;
                    }
                }
                else {
                    Log.e("DatabaseHelper", "Cannot find sync status of the course");
                }

            }
            editTextDate.setText("");
            editTextTeacher.setText("");
            editTextComments.setText("");
        });
    }

    private void loadCourseName(int courseId) {
        Cursor cursor = dbHelper.getCourseById(courseId);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME); // Use the correct column name
            if (nameIndex != -1) {
                String courseName = cursor.getString(nameIndex);
                // Set the course name in the TextView
                courseNameTextView.setText("Class List of " + courseName);
            } else {
                Log.e("YogaClassActivity", "Course name column not found.");
            }
        } else {
            Log.e("YogaClassActivity", "Cursor is null or empty for course ID: " + courseId);
        }

        // Close the Cursor
        if (cursor != null) {
            cursor.close();
        }
    }

    private void loadYogaClassDetails(int yogaClassId) {
        Cursor cursor = dbHelper.getYogaClassById(yogaClassId);
        if (cursor != null && cursor.moveToFirst()) {
            int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_DATE);
            int teacherIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_TEACHER);
            int commentsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_COMMENTS);

            // Check if indices are valid
            if (dateIndex != -1 && teacherIndex != -1 && commentsIndex != -1) {
                String date = cursor.getString(dateIndex);
                String teacher = cursor.getString(teacherIndex);
                String comments = cursor.getString(commentsIndex);

                // Set values to EditText fields
                editTextDate.setText(date);
                editTextTeacher.setText(teacher);
                editTextComments.setText(comments);
            } else {
                // Handle the case where one or more columns do not exist
                Log.e("YogaClassActivity", "One or more columns not found in the cursor.");
            }
        } else {
            Log.e("YogaClassActivity", "Cursor is null or empty for yoga class ID: " + yogaClassId);
        }

        // Close the Cursor
        if (cursor != null) {
            cursor.close();
        }
    }

    public void editYogaClass(YogaClass yogaClass) {
        selectedClassId = yogaClass.getId();
        loadYogaClassDetails(selectedClassId);
    }

    private void loadYogaClassList() {
        Cursor cursor = dbHelper.getYogaClassesByCourse(courseId);
        List<YogaClass> yogaClassList = getYogaClassListFromCursor(cursor);

        // Close the Cursor
        cursor.close();

        // Update the adapter
        yogaClassAdapter.setYogaClassList(yogaClassList);
        Log.d("YogaClassList", "Number of classes loaded: " + yogaClassList.size());
    }


    private List<YogaClass> getYogaClassListFromCursor(Cursor cursor) {
        List<YogaClass> yogaClassList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_ID);
                int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_DATE);
                int teacherIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_TEACHER);
                int commentsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_COMMENTS);
                int courseIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COURSE_REF_ID);

                // Check if indices are valid
                if (idIndex >= 0 && dateIndex >= 0 && teacherIndex >= 0 && commentsIndex >= 0 && courseIdIndex >= 0) {
                    int idValue = cursor.getInt(idIndex);
                    String date = cursor.getString(dateIndex);
                    String teacher = cursor.getString(teacherIndex);
                    String comments = cursor.getString(commentsIndex);
                    int courseId = cursor.getInt(courseIdIndex);

                    YogaClass yogaClass = new YogaClass(courseId, date, teacher, comments);
                    yogaClass.setId(idValue);
                    yogaClassList.add(yogaClass);
                } else {
                    // Handle the case where one or more columns do not exist
                    throw new RuntimeException("One or more columns not found in the cursor.");
                }
            } while (cursor.moveToNext());
        }
        return yogaClassList;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Get the day of week for the selected date
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // Check if the day of week matches the course day of week
                if (dayOfWeek == getDayOfWeekForCourse(courseId)) {
                    editTextDate.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth));
                } else {
                    Toast.makeText(YogaClassActivity.this, "The selected date does not match the course day of week", Toast.LENGTH_SHORT).show();
                }
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private int getDayOfWeekForCourse(int courseId) {
        // Query the database to get the day of week for the course
        Cursor cursor = dbHelper.getCourseById(courseId);

        // Check if the course exists
        if (cursor != null && cursor.moveToFirst()) {
            // Get the day of week from the cursor
            int dayOfWeekIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DAY_OF_WEEK);

            if (dayOfWeekIndex != -1) { // Ensure the column exists
                String dayOfWeek = cursor.getString(dayOfWeekIndex);

                // Convert the day of week string to a Calendar constant
                switch (dayOfWeek) {
                    case "Monday":
                        return Calendar.MONDAY;
                    case "Tuesday":
                        return Calendar.TUESDAY;
                    case "Wednesday":
                        return Calendar.WEDNESDAY;
                    case "Thursday":
                        return Calendar.THURSDAY;
                    case "Friday":
                        return Calendar.FRIDAY;
                    case "Saturday":
                        return Calendar.SATURDAY;
                    case "Sunday":
                        return Calendar.SUNDAY;
                    default:
                        return Calendar.MONDAY; // Default case
                }
            } else {
                // Handle the case where the column does not exist
                throw new RuntimeException("Day of week column not found");
            }
        } else {
            // Handle the case where the course does not exist
            throw new RuntimeException("Course not found with ID: " + courseId);
        }
    }
}

