package com.example.yogaadmin;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class YogaClassAdapter extends RecyclerView.Adapter<YogaClassAdapter.YogaClassViewHolder> {

    private List<YogaClass> yogaClassList;
    private DatabaseHelper dbHelper;
    private Context context;

    public YogaClassAdapter(Context context, List<YogaClass> yogaClassList) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
        this.yogaClassList = yogaClassList;
    }

    public void setYogaClassList(List<YogaClass> yogaClassList) {
        this.yogaClassList.clear();
        this.yogaClassList.addAll(yogaClassList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public YogaClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for yoga classes
        View view = LayoutInflater.from(context).inflate(R.layout.yogaclass_item, parent, false);
        return new YogaClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YogaClassViewHolder holder, int position) {
        // Bind data to the view holder
        YogaClass yogaClass = yogaClassList.get(position);
        holder.classDate.setText(context.getString(R.string.yoga_class_date, yogaClass.getDate()));
        holder.teacherText.setText(context.getString(R.string.yoga_class_teacher, yogaClass.getTeacher()));
        holder.commentText.setText(context.getString(R.string.yoga_class_comment, yogaClass.getComments()));

        holder.deleteClassButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition(); // Get the current position
            if (currentPosition != RecyclerView.NO_POSITION) {
                YogaClass selectedYogaClass = yogaClassList.get(currentPosition);
                Log.d("YogaClassAdapter", "Attempting to delete yoga class with ID: " + selectedYogaClass.getId());
                dbHelper.deleteYogaClass(selectedYogaClass.getId());
                Cursor cursor = dbHelper.getCourseById(selectedYogaClass.getCourseId());
                if (cursor != null && cursor.moveToFirst()) {
                    // Extract course data from the cursor
                    int isSyncedIndex = cursor.getColumnIndex("isSynced"); // Adjust column name as needed
                    boolean isSynced = cursor.getInt(isSyncedIndex) == 1; // Assuming 1 is true, 0 is false

                    if (isSynced) {
                        // If the course is synced, delete from Firebase
                        FirebaseHelper firebaseHelper = new FirebaseHelper(dbHelper);
                        firebaseHelper.deleteSelectedYogaClass(selectedYogaClass.getCourseId(), selectedYogaClass.getId()); // Call Firebase delete
                    }
                    cursor.close(); // Always close the cursor when done
                }

                yogaClassList.remove(currentPosition);
                notifyDataSetChanged();
            }
        });

        holder.editClassButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition(); // Get the current position
            if (currentPosition != RecyclerView.NO_POSITION) {
                YogaClass selectedYogaClass = yogaClassList.get(currentPosition);
                ((YogaClassActivity) context).editYogaClass(selectedYogaClass);
            }
        });
    }

    @Override
    public int getItemCount() {
        return yogaClassList.size(); // Return the size of the list
    }

    public static class YogaClassViewHolder extends RecyclerView.ViewHolder {
        // ViewHolder for yoga class items
        TextView classDate;
        TextView teacherText;
        TextView commentText;
        Button editClassButton, deleteClassButton;

        public YogaClassViewHolder(@NonNull View itemView) {
            super(itemView);
            classDate = itemView.findViewById(R.id.classDate);
            teacherText = itemView.findViewById(R.id.teacherText);
            commentText = itemView.findViewById(R.id.commentText);
            editClassButton = itemView.findViewById(R.id.editClassButton);
            deleteClassButton = itemView.findViewById(R.id.deleteClassButton);
        }
    }
}
