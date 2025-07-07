package com.example.cw_yoga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private ArrayList<YogaCourse> courseList;
    private Context context;

    public CourseAdapter(ArrayList<YogaCourse> courseList, Context context) {
        this.courseList = courseList;
        this.context = context;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        YogaCourse course = courseList.get(position);
        holder.tvCourseName.setText(course.getType());
        holder.tvDayOfWeek.setText(course.getDayOfWeek());
        holder.tvTime.setText(course.getTime());
        holder.tvCapacity.setText("Capacity: " + course.getCapacity());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvDayOfWeek, tvTime, tvCapacity;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
        }
    }
}
