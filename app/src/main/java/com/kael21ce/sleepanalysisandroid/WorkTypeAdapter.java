package com.kael21ce.sleepanalysisandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkTypeAdapter extends RecyclerView.Adapter<WorkTypeAdapter.ViewHolder> {
    ArrayList<WorkType> items = new ArrayList<>();
    private int selectedPosition = -1;
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
            } else {
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
            // 편집 버튼 누르면 시간 수정하는 칸으로 이동
            workTypeEditButton.setOnClickListener(v -> {

            });

            // 야간일 때는 하단의 회색 선을 제거
            if (workType == 3) {
                workTypeItemLayout.setBackgroundResource(R.drawable.white_gray_stroke);
            }
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.worktype, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        WorkType item = items.get(position);
        viewHolder.workTypeRadio.setChecked(selectedPosition == position);

        // Item에 click listener 설정
        viewHolder.itemView.setOnClickListener(v -> {
            int lastSelectedPosition = selectedPosition;
            selectedPosition = viewHolder.getAdapterPosition();
            if (lastSelectedPosition != -1) {
                notifyItemChanged(lastSelectedPosition);
            }
            notifyItemChanged(selectedPosition);
        });
        viewHolder.workTypeRadio.setOnClickListener(v -> {
            viewHolder.itemView.performClick();
        });
        viewHolder.setItem(item);
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
}
