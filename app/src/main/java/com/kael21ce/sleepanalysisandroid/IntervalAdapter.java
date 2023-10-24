package com.kael21ce.sleepanalysisandroid;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class IntervalAdapter extends RecyclerView.Adapter<IntervalAdapter.ViewHolder> {
    ArrayList<Interval> items = new ArrayList<Interval>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //Make view by using inflater
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.interval_card, viewGroup, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Interval item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Interval item) {
        items.add(item);
    }

    public void setItems(ArrayList<Interval> items) {
        this.items = items;
    }

    public Interval getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, Interval item) {
        items.set(position, item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView intervalText;
        TextView classificationText;
        LinearLayout classificationImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            intervalText = itemView.findViewById(R.id.intervalText);
            classificationText = itemView.findViewById(R.id.classificationText);
            classificationImage = itemView.findViewById(R.id.classificationImage);
        }

        public void setItem(Interval item) {
            intervalText.setText(item.getInterval());
            Integer classify = item.getIsNap();
            if (classify == 1) {
                classificationText.setText("낮잠");
                classificationImage.setBackgroundResource(R.drawable.sleep_icon);
            } else if (classify == 2) {
                classificationText.setText("활동");
                classificationImage.setBackgroundResource(R.drawable.activity_icon);
            } else if (classify == 3) {
                classificationText.setText("수면");
                classificationImage.setBackgroundResource(R.drawable.sleep_icon);
            }
        }
    }
}
