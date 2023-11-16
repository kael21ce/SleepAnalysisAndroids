package com.kael21ce.sleepanalysisandroid;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BarAdapter extends RecyclerView.Adapter<BarAdapter.ViewHolder> {
    ArrayList<Bar> items = new ArrayList<Bar>();
    private int selectedItemPosition = RecyclerView.NO_POSITION;
    private OnItemClickListener onBarClickListener;

    //Interface for onBarClickListener
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    //Method to set the click listener from HomeFragment
    public void setOnBarClickListener(OnItemClickListener listener) {
        this.onBarClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.chart_bar, viewGroup, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Bar item = items.get(position);
        LinearLayout positiveDisplayer = viewHolder.itemView.findViewById(R.id.PositiveDisplayer);
        LinearLayout negativeDisplayer = viewHolder.itemView.findViewById(R.id.NegativeDisplayer);
        //Set the background of bar whether it is clicked or not
        if (selectedItemPosition == position) {
            positiveDisplayer.setBackgroundResource(R.color.green_1);
            negativeDisplayer.setBackgroundResource(R.color.red_1);
        } else {
            positiveDisplayer.setBackgroundResource(R.color.green_2);
            negativeDisplayer.setBackgroundResource(R.color.red_2);
        }

        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Bar item) {
        items.add(item);
    }

    public void setItems(ArrayList<Bar> items) {
        this.items = items;
    }

    public Bar getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, Bar item) {
        items.set(position, item);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        LinearLayout positiveCompetitor;
        LinearLayout positiveDisplayer;
        LinearLayout negativeCompetitor;
        LinearLayout negativeDisplayer;

        public ViewHolder(View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.dateTextView);
            positiveCompetitor = itemView.findViewById(R.id.PositiveCompetitor);
            positiveDisplayer = itemView.findViewById(R.id.PositiveDisplayer);
            negativeCompetitor = itemView.findViewById(R.id.NegativeCompetitor);
            negativeDisplayer = itemView.findViewById(R.id.NegativeDisplayer);

            itemView.setOnClickListener(view -> {
                int previousSelectedPosition = selectedItemPosition;
                selectedItemPosition = getAdapterPosition();
                int position = getAdapterPosition();
                if (selectedItemPosition != RecyclerView.NO_POSITION && onBarClickListener != null) {
                    onBarClickListener.onItemClick(position);
                }
                notifyItemChanged(previousSelectedPosition);
                notifyItemChanged(selectedItemPosition);
            });
        }

        public void setItem(Bar item) {
            dateTextView.setText(item.getDate());
            //Set the height of positiveCompetitor and negativeDisplayer
            int competitor_pHDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, item.positiveWeight,
                    itemView.getContext().getResources().getDisplayMetrics());
            int sizeDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 124,
                    itemView.getContext().getResources().getDisplayMetrics());
            int zeroDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,
                    itemView.getContext().getResources().getDisplayMetrics());
            int pHDp = sizeDp - competitor_pHDp;
            int nHDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, item.negativeWeight,
                    itemView.getContext().getResources().getDisplayMetrics());
            if (pHDp < zeroDp || nHDp > sizeDp) {
                positiveCompetitor.getLayoutParams().height = sizeDp;
                positiveCompetitor.requestLayout();
                negativeDisplayer.getLayoutParams().height = zeroDp;
                negativeDisplayer.requestLayout();
            } else {
                positiveCompetitor.getLayoutParams().height = pHDp;
                positiveCompetitor.requestLayout();
                negativeDisplayer.getLayoutParams().height = nHDp;
                negativeDisplayer.requestLayout();
            }
        }
    }
}
