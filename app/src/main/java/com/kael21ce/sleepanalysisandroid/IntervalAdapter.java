package com.kael21ce.sleepanalysisandroid;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class IntervalAdapter extends RecyclerView.Adapter<IntervalAdapter.ViewHolder> {
    ArrayList<Interval> items = new ArrayList<Interval>();
    FragmentManager fragmentManager;
    String date;

    public void setDate(String date){
        this.date = date;
    }

    public IntervalAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //Make view by using inflater
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        if (viewType != Interval.Activity_Type) {
            View itemView = inflater.inflate(R.layout.interval_edit_card, viewGroup, false);
            EditIntervalFragment editIntervalFragment = new EditIntervalFragment();

            //Click listener of editButton
            ImageButton editButton = itemView.findViewById(R.id.editIntervalButton);
            editButton.setOnClickListener(view -> {
                //Move to the edit page
                TextView itemTextView = itemView.findViewById(R.id.intervalTextEdit);
                String itemText = itemTextView.getText().toString();
                String[] hours = itemText.split(" - ");
                //Make a bundle
                Bundle bundle = new Bundle();
                bundle.putString("date", date);
                bundle.putString("startHour", hours[0]);
                bundle.putString("endHour", hours[1]);
                editIntervalFragment.setArguments(bundle);

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.IntervalFrame, editIntervalFragment).commit();
            });
            return new ViewHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.interval_card, viewGroup, false);
            return new ViewHolder(itemView);
        }
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

    @Override
    public int getItemViewType(int position) {
        int type = items.get(position).getIsNap();
        return type;
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
            //For sleep interval items
            if (itemView.findViewById(R.id.intervalText) == null) {
                intervalText = itemView.findViewById(R.id.intervalTextEdit);
                classificationText = itemView.findViewById(R.id.classificationTextEdit);
                classificationImage = itemView.findViewById(R.id.classificationImageEdit);
            } else {
                intervalText = itemView.findViewById(R.id.intervalText);
                classificationText = itemView.findViewById(R.id.classificationText);
                classificationImage = itemView.findViewById(R.id.classificationImage);
            }
        }

        public void setItem(Interval item) {
            intervalText.setText(item.getInterval());
            Integer classify = item.getIsNap();
            if (classify == Interval.Nap_Type) {
                classificationText.setText("낮잠");
                classificationImage.setBackgroundResource(R.drawable.sleep_icon);
            } else if (classify == Interval.Activity_Type) {
                classificationText.setText("활동");
                classificationImage.setBackgroundResource(R.drawable.activity_icon);
            } else if (classify == Interval.Sleep_Type) {
                classificationText.setText("수면");
                classificationImage.setBackgroundResource(R.drawable.sleep_icon);
            }
        }
    }
}
