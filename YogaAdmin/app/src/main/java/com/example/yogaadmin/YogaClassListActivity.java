package com.example.yogaadmin;

import android.app.Dialog;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class YogaClassListActivity extends AppCompatActivity {
    private EditText searchTeacherEditText;
    private Button searchButton, advancedSearchButton, backButton;
    private RecyclerView recyclerView;
    private ListYogaClassAdapter yogaClassAdapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_class_list);

        dbHelper = new DatabaseHelper(this);
        searchTeacherEditText = findViewById(R.id.searchTeacherEditText);
        searchButton = findViewById(R.id.searchButton);
        advancedSearchButton = findViewById(R.id.advancedSearchButton);
        backButton = findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        yogaClassAdapter = new ListYogaClassAdapter(this, new ArrayList<>(), dbHelper);
        recyclerView.setAdapter(yogaClassAdapter);

        searchButton.setOnClickListener(v -> {
            String teacherName = searchTeacherEditText.getText().toString();
            Cursor cursor = dbHelper.searchYogaClassesByTeacher(teacherName);
            List<YogaClass> yogaClasses = getYogaClassListFromCursor(cursor);
            yogaClassAdapter.setYogaClassList(yogaClasses);
            cursor.close();
        });

        // Advanced search button (date or day of week)
        advancedSearchButton.setOnClickListener(v -> {
            showAdvancedSearchDialog();
        });

        // Return to course list
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(YogaClassListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Display all yoga classes initially
        Cursor cursor = dbHelper.getAllYogaClasses();
        Log.d("YogaClassListActivity", "Number of classes: " + cursor.getCount());
        List<YogaClass> yogaClasses = getYogaClassListFromCursor(cursor);
        yogaClassAdapter.setYogaClassList(yogaClasses);
        cursor.close();
    }

    private List<YogaClass> getYogaClassListFromCursor(Cursor cursor) {
        List<YogaClass> yogaClasses = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_ID);
                int courseIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COURSE_REF_ID);
                int classDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_DATE);
                int teacherIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_TEACHER);
                int commentsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_COMMENTS);

                // Check if indices are valid
                if (idIndex != -1 && courseIdIndex != -1 && classDateIndex != -1
                        && teacherIndex != -1 && commentsIndex != -1)
                {
                    int id = cursor.getInt(idIndex);
                    int courseId = cursor.getInt(courseIdIndex);
                    String date = cursor.getString(classDateIndex);
                    String teacher = cursor.getString(teacherIndex);
                    String comments = cursor.getString(commentsIndex);

                    YogaClass yogaClass = new YogaClass(courseId, date, teacher, comments);
                    yogaClass.setId(id);
                    yogaClasses.add(yogaClass);
                }
            } while (cursor.moveToNext());
        }
        return yogaClasses;
    }

    private void showAdvancedSearchDialog() {
        // Create a dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_advanced_search);

        // Get references to the input fields and button
        Spinner dayOfWeekSpinner = dialog.findViewById(R.id.dayOfWeekSpinner);
        EditText classDateEditText = dialog.findViewById(R.id.classDateEditText);
        Button applySearchButton = dialog.findViewById(R.id.applySearchButton);

        // Set click listener for the apply search button
        applySearchButton.setOnClickListener(v -> {
            String dayOfWeek = dayOfWeekSpinner.getSelectedItem().toString();
            String classDate = classDateEditText.getText().toString();

            // Check if both fields are filled or if both are empty
            if (dayOfWeek.equals("Select Day") && classDate.isEmpty()) {
                Toast.makeText(YogaClassListActivity.this, "Please enter either Day of the Week or Class Date.", Toast.LENGTH_SHORT).show();
            } else if (!dayOfWeek.equals("Select Day") && !classDate.isEmpty()) {
                Toast.makeText(YogaClassListActivity.this, "Please enter either Day of the Week or Class Date, not both.", Toast.LENGTH_SHORT).show();
            } else {
                // Proceed with search if only one field is filled
                performAdvancedSearch(dayOfWeek, classDate);
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void performAdvancedSearch(String dayOfWeek, String classDate) {
        Cursor cursor = null;

        if (!dayOfWeek.equals("Select Day")) {
            cursor = dbHelper.searchYogaClassesByDayOfWeek(dayOfWeek);
        } else if (!classDate.isEmpty()) {
            cursor = dbHelper.searchYogaClassesByDate(classDate);
        }

        if (cursor != null) {
            List<YogaClass> yogaClasses = getYogaClassListFromCursor(cursor);
            yogaClassAdapter.setYogaClassList(yogaClasses);
            cursor.close();
        }
    }
}