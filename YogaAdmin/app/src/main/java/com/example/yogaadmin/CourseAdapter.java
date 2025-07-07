package com.example.yogaadmin;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private Activity activity;
    private DatabaseHelper dbHelper;
    private List<Course> courseList;
    private FirebaseHelper firebaseHelper;

    public CourseAdapter(Activity activity, FirebaseHelper firebaseHelper) {
        this.activity = activity; // Initialize the activity
        this.firebaseHelper = firebaseHelper;
        dbHelper = new DatabaseHelper(activity);
        courseList = new ArrayList<>();
        loadCoursesFromDatabase();
    }

    // Load courses from the database and populate the list
    public void loadCoursesFromDatabase() {
        Cursor cursor = dbHelper.getAllCourses();
        if (cursor != null) {
            try {
                courseList.clear(); // Clear the current list before loading new data
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

                    // Validate columns and retrieve data
                    if (columnIndexId != -1 && columnIndexDayOfWeek != -1
                            && columnIndexTime != -1 && columnIndexCapacity != -1
                            && columnIndexDuration != -1 && columnIndexPrice != -1
                            && columnIndexType != -1 && columnIndexDescription != -1
                            && columnIndexIsSynced != -1) {

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
                        courseList.add(course); // Add to the course list
                    }
                }
            } finally {
                cursor.close(); // Ensure the cursor is closed
            }
        }
        notifyDataSetChanged(); // Notify the adapter that data has changed
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.isSynced.setText(activity.getString(R.string.course_synced, String.valueOf(course.getSynced())));
        holder.courseName.setText(activity.getString(R.string.course_name, course.getName()));
        holder.courseTypeText.setText(activity.getString(R.string.course_type, course.getType()));
        holder.dayOfWeekText.setText(activity.getString(R.string.course_day, course.getDayOfWeek()));
        holder.timeText.setText(activity.getString(R.string.course_time, course.getTime()));
        holder.capacityText.setText(activity.getString(R.string.course_capacity, course.getCapacity()));
        holder.durationText.setText(activity.getString(R.string.course_duration, course.getDuration()));
        holder.priceText.setText(activity.getString(R.string.course_price, course.getPrice()));
        holder.courseDescriptionText.setText(activity.getString(R.string.course_description, course.getDescription()));

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition(); // Get the current position
                if (currentPosition != RecyclerView.NO_POSITION) {
                    Course course = courseList.get(currentPosition);
                    dbHelper.deleteCourse(course.getId());
                    if (course.getSynced()) {
                        // Sync deletion with Firestore if the course is already synced
                        int courseId = course.getId();
                        firebaseHelper.deleteCourse(courseId);
                        firebaseHelper.deleteYogaClass(courseId);
                    }
                    courseList.remove(currentPosition);
                    notifyDataSetChanged();
                }
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition(); // Get the current position
                if (currentPosition != RecyclerView.NO_POSITION) {

                    // Create Intent using the stored activity
                    Intent intent = new Intent(activity, AddCourseActivity.class);
                    intent.putExtra("courseId", course.getId());
                    activity.startActivity(intent);
                }
            }
        });

        holder.classesButton.setOnClickListener(v -> {
            // Handle navigation to the YogaClass list for this course
            int currentPosition = holder.getAdapterPosition(); // Get the current position
            if (currentPosition != RecyclerView.NO_POSITION) {

                Intent intent = new Intent(activity, YogaClassActivity.class);
                intent.putExtra("courseId", course.getId()); // Pass the course ID to the next activity
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView isSynced, courseName, courseTypeText, dayOfWeekText, timeText, capacityText, durationText, priceText, courseDescriptionText;
        Button editButton, deleteButton, classesButton;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            isSynced = itemView.findViewById(R.id.isSyncedText);
            courseName = itemView.findViewById(R.id.courseName);
            courseTypeText = itemView.findViewById(R.id.courseTypeText);
            dayOfWeekText = itemView.findViewById(R.id.dayOfWeekText);
            timeText = itemView.findViewById(R.id.timeText);
            capacityText = itemView.findViewById(R.id.capacityText);
            durationText = itemView.findViewById(R.id.durationText);
            priceText = itemView.findViewById(R.id.priceText);
            courseDescriptionText = itemView.findViewById(R.id.courseDescriptionText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            classesButton = itemView.findViewById(R.id.classesButton);
        }
    }
}
