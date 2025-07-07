package com.example.yogaadmin;

import android.database.Cursor;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {
    private FirebaseFirestore db;
    private DatabaseHelper databaseHelper;

    public FirebaseHelper(DatabaseHelper dbHelper) {
        db = FirebaseFirestore.getInstance();
        this.databaseHelper = dbHelper;
    }

    public void syncCourse(Course course, DatabaseHelper dbHelper) {
        // Sync course with Firestore
        db.collection("courses").document(String.valueOf(course.getId())).set(course)
                .addOnSuccessListener(aVoid -> {
                    // Update local database to set isSynced to true
                    course.setSynced(true);
                    dbHelper.updateCourse(course); // Update the course in the local database
                    Log.d("FirebaseHelper", "Course uploaded successfully");
                })
                .addOnFailureListener(e -> {
                    Log.w("FirebaseHelper", "Error uploading course", e);
                });
    }

    public void syncYogaClasses(int courseId) {
        // Retrieve yoga classes from the local database using a Cursor
        Cursor cursor = databaseHelper.getYogaClassesByCourse(courseId);
        List<YogaClass> yogaClasses = getYogaClassesByCourse(cursor); // Pass the Cursor

        // Log the number of yoga classes fetched
        Log.d("FirebaseHelper", "Number of yoga classes to upload: " + yogaClasses.size());

        for (YogaClass yogaClass : yogaClasses) {
            db.collection("courses").document(String.valueOf(courseId))
                    .collection("yoga_classes").document(String.valueOf(yogaClass.getId())).set(yogaClass)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Yoga class uploaded successfully"))
                    .addOnFailureListener(e -> Log.w("FirebaseHelper", "Error uploading yoga class", e));
        }
    }

    // Method to retrieve yoga classes for a specific course
    private List<YogaClass> getYogaClassesByCourse(Cursor cursor) {
        List<YogaClass> yogaClasses = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve column indices
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_ID);
                int courseIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COURSE_REF_ID);
                int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_DATE);
                int teacherIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_TEACHER);
                int commentsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CLASS_COMMENTS);

                // Check if indices are valid
                if (courseIdIndex != -1 && dateIndex != -1 && teacherIndex != -1
                        && commentsIndex != -1 && idIndex != -1) {
                    // Create a new YogaClass object with the retrieved data
                    YogaClass yogaClass = new YogaClass(
                            cursor.getInt(courseIdIndex),
                            cursor.getString(dateIndex),
                            cursor.getString(teacherIndex),
                            cursor.getString(commentsIndex)
                    );

                    // Set the ID for the yoga class
                    yogaClass.setId(cursor.getInt(idIndex));

                    // Add the yoga class to the list
                    yogaClasses.add(yogaClass);
                } else {
                    Log.w("FirebaseHelper", "One or more column indices are invalid.");
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close(); // Always close the cursor to prevent memory leaks
        }
        return yogaClasses;
    }

    public void updateCourse(Course course) {
        db.collection("courses").document(String.valueOf(course.getId())).set(course)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Course updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.w("FirebaseHelper", "Error updating course", e);
                });
    }

    public void deleteCourse(int courseId) {
        db.collection("courses").document(String.valueOf(courseId)).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Course deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.w("FirebaseHelper", "Error deleting course", e);
                });
    }

    public void updateYogaClass(int courseId, YogaClass yogaClass) {
        // Reference to the specific yoga class document within the course
        db.collection("courses").document(String.valueOf(courseId))
                .collection("yoga_classes").document(String.valueOf(yogaClass.getId()))
                .set(yogaClass) // Use set to update the document
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Yoga class updated successfully: " + yogaClass.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("FirebaseHelper", "Error updating yoga class: " + yogaClass.getId(), e);
                });
    }

    public void deleteYogaClass(int courseId) {
        // Reference to the yoga classes collection for the specified course
        db.collection("courses").document(String.valueOf(courseId))
                .collection("yoga_classes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each yoga class document
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Yoga class deleted successfully: " + document.getId()))
                                    .addOnFailureListener(e -> Log.w("FirebaseHelper", "Error deleting yoga class: " + document.getId(), e));
                        }
                    } else {
                        Log.w("FirebaseHelper", "Error getting yoga classes", task.getException());
                    }
                });
    }

    public void deleteSelectedYogaClass(int courseId, int yogaClassId) {
        db.collection("courses").document(String.valueOf(courseId))
                .collection("yoga_classes").document(String.valueOf(yogaClassId))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Yoga class deleted successfully: " + yogaClassId))
                .addOnFailureListener(e -> Log.w("FirebaseHelper", "Error deleting yoga class: " + yogaClassId, e));
    }
}
