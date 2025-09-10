package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kael21ce.sleepanalysisandroid.databinding.FragmentWorkPresetBinding;

import nl.joery.timerangepicker.TimeRangePicker;

public class WorkPresetFragment extends BottomSheetDialogFragment {

    // 업데이트된 내용을 WorkTypeAdapter에 전달하기 위한 callback 메소드
    public interface OnItemUpdateListener {
        void onItemUpdated(int position, WorkType updatedItem);
    }

    private FragmentWorkPresetBinding binding;
    private int position, workType;
    private String onset, offset;
    private OnItemUpdateListener mListener;

    // WorkPresetFragment 호출 시 전달할 bundle 생성
    public static WorkPresetFragment newInstance(int position, WorkType item) {
        WorkPresetFragment fragment = new WorkPresetFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("workType", item.getWorkType());
        fragment.setArguments(args);
        return fragment;
    }

    // 자신을 호출한 Activity/Fragment가 인터페이스를 구현했는지 확인
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 1. 부모 Fragment로부터 리스너를 가져오려고 시도
        if (getParentFragment() instanceof OnItemUpdateListener) {
            mListener = (OnItemUpdateListener) getParentFragment();
        }
        // 2. 부모 Fragment가 없다면, 호스팅 Activity로부터 가져오려고 시도
        else if (context instanceof OnItemUpdateListener) {
            mListener = (OnItemUpdateListener) context;
        }
        // 둘 다 구현되어 있지 않다면 에러 발생
        else {
            throw new ClassCastException(context.toString()
                    + " or parent fragment must implement OnItemUpdateListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt("position");
            workType = getArguments().getInt("workType");
            Log.d("WorkPresetFragment", "번들 가져오기 성공");
        } else {
            workType = 1; // null workType: sharedPreference가 작동하도록 1로 고정하기
            position = 1;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWorkPresetBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // 선택된 workType에 해당하는 workOnset, workOffset 가져오기
        onset = sharedPref.getString("workOnset_" + workType, "09:00");
        offset = sharedPref.getString("workOffset_" + workType, "18:00");

        // 취소 TextView 클릭 시 BottomSheetDialogFragment 닫기
        binding.cancelEditText.setOnClickListener(v -> dismiss());

        // WorkType에 맞게 presetTitle 설정
        if (workType == 1) {
            binding.presetTitle.setText("아침 근무 일정 편집");
        } else if (workType == 2) {
            binding.presetTitle.setText("저녁 근무 일정 편집");
        } else if (workType == 3) {
            binding.presetTitle.setText("새벽 근무 일정 편집");
        }

        // TimeRangePicker 초기 설정
        binding.PresetTimePicker.setEndTimeMinutes(parseHHMM(onset)); // 시작 시간
        binding.PresetTimePicker.setStartTimeMinutes(parseHHMM(offset)); // 끝 시간
        binding.presetRangeText.setText(onset + " → " + offset);

        // TimeRangePicker에 대한 설정: Start thumb가 offset, end thumb가 onset
        binding.PresetTimePicker.setOnTimeChangeListener(new TimeRangePicker.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(@NonNull TimeRangePicker.Time time) {
                offset = AddIntervalFragment.time2String(time.getTotalMinutes());
                String results = onset + " → " + offset;
                binding.presetRangeText.setText(results);
            }

            @Override
            public void onEndTimeChange(@NonNull TimeRangePicker.Time time) {
                onset = AddIntervalFragment.time2String(time.getTotalMinutes());
                String results = onset + " → " + offset;
                binding.presetRangeText.setText(results);
            }

            @Override
            public void onDurationChange(@NonNull TimeRangePicker.TimeDuration timeDuration) {

            }
        });

        // 저장 TextView 클릭 시 preset 저장 후 종료
        binding.saveEditText.setOnClickListener(v -> {
            editor.putString("workOnset_" + workType, onset);
            editor.putString("workOffset_" + workType, offset);
            editor.apply();
            if (mListener != null) {
                WorkType updatedWorkType = new WorkType(workType, onset, offset, true);
                mListener.onItemUpdated(position, updatedWorkType);
            }

            dismiss();
        });
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        View parentView = (View) requireView().getParent();
//        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parentView);
//
//        // BottomSheet의 높이 설정
//        int desiredHeightInDp = 400;
//        int heightInPixels = (int) (desiredHeightInDp * getResources().getDisplayMetrics().density);
//        behavior.setPeekHeight(heightInPixels);
//        // behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private int parseHHMM(String HHMM) {
        try {
            String[] parts = HHMM.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour * 60 + minute;
        } catch (Exception e) {
            Log.e("WorkPresetFragment", "HH:mm String을 파싱하는 과정에서 문제 발생");
            return 0;
        }
    }
}