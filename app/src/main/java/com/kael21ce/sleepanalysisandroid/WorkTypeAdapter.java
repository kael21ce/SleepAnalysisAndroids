package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkTypeAdapter extends RecyclerView.Adapter<WorkTypeAdapter.ViewHolder> {



    public interface OnEditClickListener {
        void onItemClick(int position, WorkType item);
    }

    public interface OnWorkTypeSelectedListener {
        void onWorkTypeSelected(WorkType item); // 선택된 WorkType 객체를 전달
    }

    ArrayList<WorkType> items = new ArrayList<>();
    private int selectedPosition = -1;
    private OnWorkTypeSelectedListener listener;
    private OnEditClickListener clickListener;
    private Context context;

    public void setOnItemClickListener(OnEditClickListener listener) {
        this.clickListener = listener;
    }

    public WorkTypeAdapter(OnWorkTypeSelectedListener listener) {
        this.listener = listener;
    }

    public void addItem(WorkType item) {
        items.add(item);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout workTypeItemLayout;
        private final TextView workTypeTitleText, workTypeTimeText;
        private final RadioButton workTypeRadio;
        private final AppCompatButton workTypeEditButton;
        public ViewHolder(View view) {
            super(view);
            workTypeItemLayout = view.findViewById(R.id.WorkTypeItemLayout);
            workTypeTitleText = view.findViewById(R.id.workTypeTitleText);
            workTypeTimeText = view.findViewById(R.id.workTypeTimeText);
            workTypeRadio = view.findViewById(R.id.workTypeRadio);
            workTypeEditButton = view.findViewById(R.id.workTypeEditButton);
        }
        public void setItem(WorkType item) {
            int workType = item.getWorkType(); // 0: 휴무, 1: 아침, 2: 저녁, 3: 야간
            String workStart = item.getWorkStart();
            String workEnd = item.getWorkEnd();
            boolean isChosen = item.isChosen();

            // Work type, 근무 시작 시간, 종료 시간 설정
            if (workType == 0) {
                workTypeTitleText.setText("휴무");
                workTypeTimeText.setText("시간 없음");
                workTypeEditButton.setVisibility(View.INVISIBLE);
            } else {
                workTypeEditButton.setVisibility(View.VISIBLE);
                workTypeTimeText.setText(workStart + " - " + workEnd);
                if (workType == 1) {
                    workTypeTitleText.setText("아침");
                } else if (workType == 2) {
                    workTypeTitleText.setText("저녁");
                } else if (workType == 3) {
                    workTypeTitleText.setText("야간");
                } else {
                    workTypeTitleText.setText("오류");
                }
            }

            // 야간일 때는 하단의 회색 선을 제거
            if (workType == 3) {
                workTypeItemLayout.setBackgroundResource(R.drawable.white_gray_stroke);
            } else {
                workTypeItemLayout.setBackgroundResource(R.drawable.bottom_line);
            }
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.worktype, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        WorkType item = items.get(position);
        viewHolder.workTypeRadio.setChecked(selectedPosition == position);

        // workTypeEditButton을 누르면 ClickListener를 할당
        viewHolder.workTypeEditButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(position, item);
            }
        });

        // Item에 click listener 설정
        viewHolder.itemView.setOnClickListener(v -> {
            int lastSelectedPosition = selectedPosition;
            selectedPosition = viewHolder.getAdapterPosition();
            if (lastSelectedPosition != -1) {
                notifyItemChanged(lastSelectedPosition);
            }
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onWorkTypeSelected(items.get(selectedPosition));
            }
        });
        viewHolder.workTypeRadio.setOnClickListener(v -> {
            viewHolder.itemView.performClick();
        });
        viewHolder.setItem(item);
    }

    public void updateItem(int position, WorkType newItem) {
        if (position >= 0 && position < items.size()) {
            items.set(position, newItem);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
