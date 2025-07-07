package com.example.yogaadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListYogaClassAdapter extends RecyclerView.Adapter<ListYogaClassAdapter.ViewHolder> {
    private List<YogaClass> yogaClasses;
    private Context context;
    private DatabaseHelper databaseHelper;

    public ListYogaClassAdapter(Context context, List<YogaClass> yogaClasses, DatabaseHelper databaseHelper) {
        this.context = context;
        this.yogaClasses = yogaClasses;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_yogaclass_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YogaClass yogaClass = yogaClasses.get(position);

        holder.classDate.setText(context.getString(R.string.yoga_class_date, yogaClass.getDate()));
        holder.teacherText.setText(context.getString(R.string.yoga_class_teacher, yogaClass.getTeacher()));
        holder.commentText.setText(context.getString(R.string.yoga_class_comment, yogaClass.getComments()));
        String courseName = databaseHelper.getCourseNameById(yogaClass.getCourseId());
        holder.courseNameText.setText(context.getString(R.string.yoga_class_course_name, courseName));

        // Initially hide the details
        holder.detailsLayout.setVisibility(View.GONE);

        // Set click listener to expand/collapse details
        holder.itemView.setOnClickListener(v -> {
            if (holder.detailsLayout.getVisibility() == View.VISIBLE) {
                holder.detailsLayout.setVisibility(View.GONE);
            } else {
                holder.detailsLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return yogaClasses.size();
    }

    public void setYogaClassList(List<YogaClass> yogaClasses) {
        this.yogaClasses = yogaClasses;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView classDate, teacherText, commentText, courseNameText;
        LinearLayout detailsLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            classDate = itemView.findViewById(R.id.classDate);
            teacherText = itemView.findViewById(R.id.teacherText);
            commentText = itemView.findViewById(R.id.commentText);
            courseNameText = itemView.findViewById(R.id.courseNameText);
            detailsLayout = itemView.findViewById(R.id.detailsLayout);
        }
    }
}
