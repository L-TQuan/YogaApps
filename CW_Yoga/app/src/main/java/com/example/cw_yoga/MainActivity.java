package com.example.cw_yoga;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Button btnAddCourse, btnViewCourses, btnManageClassInstances, btnSyncToCloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        btnAddCourse = findViewById(R.id.btnAddCourse);
        btnViewCourses = findViewById(R.id.btnViewCourses);
        btnManageClassInstances = findViewById(R.id.btnManageClassInstances);
        btnSyncToCloud = findViewById(R.id.btnSyncToCloud);

        // Set onClickListeners to navigate to respective activities
        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddCourseActivity.class);
                startActivity(intent);
            }
        });

        btnViewCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewCourseActivity.class);
                startActivity(intent);
            }
        });

//        btnManageClassInstances.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, ManageClassInstancesActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        btnSyncToCloud.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SyncToCloudActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}
