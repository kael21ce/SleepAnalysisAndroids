package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.compose.ui.text.font.FontVariation;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecommendFragment extends Fragment {

    public ImageButton sleepButton;
    public ImageButton napButton;
    public ImageButton workButton;
    private TextView startTime;
    private TextView endTime;
    private TextView sleepImportanceText;
    private TextView sleepTypeText;
    private TextView stateDescriptionText;
    private TextView stateDescriptionSmallText;
    private ImageView stateDescriptionImage;
    private LinearLayout InfoView, RecommendClockView;
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy"+ "HH:mm", Locale.KOREA);
    SimpleDateFormat sdfDateTimeRecomm = new SimpleDateFormat("a hh:mm", Locale.KOREA);
    SimpleDateFormat sdfDateTimeRecomm2 = new SimpleDateFormat("a h시 mm분", Locale.KOREA);
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.KOREA);
    String mainSleepStartString,sleepOnsetString, mainSleepEndString, workOnsetString, workOffsetString, napSleepStartString, napSleepEndString;
    String sleepOnsetDisplaying, workOnsetDisplaying, workOffsetDisplaying;
    long now, nineHours;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy"+ "HH:mm", Locale.KOREA);
//        long sleepOnset = AppDatabase.sleepOnset;
//        String test = sdfDateTime.format(new Date(sleepOnset));
//        Log.v("tag_test", test);
        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        long sleepOnset = sharedPref.getLong("sleepOnset", now);
        String test = sdfDateTime.format(new Date(sleepOnset));
        Log.v("tag_test", test);

        AppDatabase db = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();
        SleepDao sleepDao = db.sleepDao();
        List<Sleep> sleeps = sleepDao.getAll();
        for(Sleep sleep: sleeps){
            String sleepStart = sdfDateTime.format(new Date(sleep.sleepStart));
            String sleepEnd = sdfDateTime.format(new Date(sleep.sleepEnd));
            Log.v("theSleepR", sleepStart);
            Log.v("theSleepR2", sleepEnd);
        }

        MainActivity mainActivity = (MainActivity)getActivity();

        SharedPreferences sharedPref2 = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref2.edit();

        String user_name = sharedPref2.getString("User_Name", "UserName");

        mainSleepStartString = sdfTime.format(new Date(mainActivity.getMainSleepStart()));
        mainSleepEndString = sdfTime.format(new Date(mainActivity.getMainSleepEnd()));
        napSleepStartString = sdfTime.format(new Date(mainActivity.getNapSleepStart()));
        napSleepEndString = sdfTime.format(new Date(mainActivity.getNapSleepEnd()));
        sleepOnsetString = sdfTime.format(new Date(mainActivity.getSleepOnset()));
        workOnsetString = sdfTime.format(new Date(sharedPref2.getLong("workOnset", now)));
        workOffsetString = sdfTime.format(new Date(sharedPref2.getLong("workOffset", now)));
        sleepOnsetDisplaying = sdfDateTimeRecomm2.format(new Date(sharedPref2.getLong("sleepOnset", now)));
        workOnsetDisplaying = sdfDateTimeRecomm2.format(new Date(sharedPref2.getLong("workOnset", now)));
        workOffsetDisplaying = sdfDateTimeRecomm2.format(new Date(sharedPref2.getLong("workOffset", now)));

        View v = inflater.inflate(R.layout.fragment_recommend, container, false);

        //No data
        LinearLayout noDataLayout = v.findViewById(R.id.noDataLayout);
        ImageView no_data = v.findViewById(R.id.no_data);
        Glide.with(v.getContext()).load(R.raw.no_data).into(no_data);
        Button addDataButton = v.findViewById(R.id.addDataButton);

        //If no onset, offset data, show noDataLayout
        InfoView = v.findViewById(R.id.InfoView);
        RecommendClockView = v.findViewById(R.id.RecommendClockView);
        TextView noDataDescription = v.findViewById(R.id.noDataDescription);
        noDataDescription.setText(user_name + "님에게 딱 맞는 \n수면 패턴을 추천해 드릴게요");

        //Check whether recommendation is hidden
        if (!sharedPref2.contains("isHidden")) {
            editor.putBoolean("isHidden", true).apply();
        }
        boolean isHidden = false;

        if (sharedPref2.contains("sleepOnset") && sharedPref2.contains("workOnset") && sharedPref2.contains("workOffset")) {
            noDataLayout.setVisibility(View.GONE);
            if (!isHidden) {
                InfoView.setVisibility(View.VISIBLE);
                RecommendClockView.setVisibility(View.VISIBLE);
            } else {
                InfoView.setVisibility(View.VISIBLE);
                RecommendClockView.setVisibility(View.GONE);
            }
        } else {
            InfoView.setVisibility(View.GONE);
            RecommendClockView.setVisibility(View.GONE);
        }
        RecommendClockView.setVisibility(View.GONE);

        //Move to WhenSleepFragment
        addDataButton.setOnClickListener(view -> {
            WhenSleepFragment whenSleepFragment = new WhenSleepFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, whenSleepFragment).commit();
            mainActivity.setGoneBottomNavi();
        });

        sleepButton = v.findViewById(R.id.sleepButton);
        napButton = v.findViewById(R.id.napButton);
        workButton = v.findViewById(R.id.workButton);
        startTime = v.findViewById(R.id.StartTime);
        endTime = v.findViewById(R.id.EndTime);
        sleepTypeText = v.findViewById(R.id.sleepTypeText);
        sleepImportanceText = v.findViewById(R.id.sleepImportanceText);
        stateDescriptionText = v.findViewById(R.id.StateDescriptionText);
        stateDescriptionSmallText = v.findViewById(R.id.StateDescriptionSmallText);
        stateDescriptionImage = v.findViewById(R.id.StateDescriptionImage);
        ClockView clockView = v.findViewById(R.id.sweepingClockRecommend);
        TextView hopeTimeText = v.findViewById(R.id.HopeTimeDetail);
        TextView workTimeStart = v.findViewById(R.id.WorkTimeStart);
        TextView workTimeEnd = v.findViewById(R.id.WorkTimeEnd);
        TextView infoText = v.findViewById(R.id.InfoText);
        TextView clockTitleRecommend = v.findViewById(R.id.ClockTitleRecommend);
        ImageButton infoButton = v.findViewById(R.id.infoButton);

        hopeTimeText.setText(sleepOnsetDisplaying);
        workTimeStart.setText(workOnsetDisplaying);
        workTimeEnd.setText(workOffsetDisplaying);

        //User name setting
        infoText.setText(user_name + "님의 일정");
        clockTitleRecommend.setText(user_name + "님을 위한 추천 수면");

        //Recommendation information
        ImageButton recommendInfoButton = v.findViewById(R.id.RecommendInfoButton);
        recommendInfoButton.setOnClickListener(view->{
            Intent infoIntent = new Intent(v.getContext(), InfoActivity.class);
            infoIntent.putExtra("Location",5);
            startActivity(infoIntent);
        });

        //Move to setting if infoButton is clicked
        infoButton.setOnClickListener(view -> {
            Intent sleepOnsetIntent = new Intent(v.getContext(), SleepOnsetActivity.class);
            startActivity(sleepOnsetIntent);
        });

        //Initial Button Setting
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        sleepButton.setOnClickListener(v1 -> sleepButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        napButton.setOnClickListener(v1 -> napButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        workButton.setOnClickListener(v1 -> workButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        sleepButton.performClick();
        clockView.setTypeOfInterval(1);
        clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);

        //startTime.setText(mainSleepStartString);
        //endTime.setText(mainSleepEndString);
        if (mainActivity.getMainSleepStart() == mainActivity.getMainSleepEnd()) {
            startTime.setText("--:--");
            endTime.setText("--:--");
        } else {
            startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart())));
            endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepEnd())));
        }

        return v;
    }

    public void sleepButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                 ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                 TextView sleepTypeText, TextView sleepImportanceText,
                                 TextView stateDescriptionText, TextView stateDescriptionSmallText,
                                 ImageView stateDescriptionImage, ClockView clockView)
    {
        Log.v("THE MAIN SLEEP STRING", mainSleepEndString);
        Log.v("THE MAIN SLEEP STRING", mainSleepStartString);
        //startTime.setText(mainSleepStartString);
        //endTime.setText(mainSleepEndString);
        stateDescriptionSmallText.setVisibility(View.VISIBLE);
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        //Change the content of displaying text
        if (mainSleepStartString.equals(mainSleepEndString)) {
            startTime.setText("--:--");
            endTime.setText("--:--");
            stateDescriptionText.setText("잠에 들기 어려운 시각이에요");
            stateDescriptionSmallText.setText("다른 취침 시각을 선택해보세요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sad, null));
        } else {
            startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart())));
            endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepEnd())));
            if (isenough) {
                if (isearly) {
                    stateDescriptionText.setText("선택하신 취침 시각이\n너무 일러요");
                    stateDescriptionSmallText.setText("조금 더 늦은 수면을 추천드려요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sad, null));
                } else {
                    stateDescriptionText.setText("잠을 자기 좋은 시간이에요");
                    stateDescriptionSmallText.setText("기상시간을 지켜주세요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sleep, null));
                }
            } else {
                if (isearly) {
                    stateDescriptionText.setText("잠이 충분하지 않아요");
                    stateDescriptionSmallText.setText("최대한 잠을 자도 피곤할 수 있어요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
                } else {
                    stateDescriptionText.setText("잠이 충분하지 않아요");
                    stateDescriptionSmallText.setText("희망 취침 시각을 앞으로 당겨보세요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
                }
            }
        }
        sleepTypeText.setText("밤잠");
        sleepImportanceText.setText("중요");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.important_caption, null));

        //Change the clock angle using setAngle and color using setTypeOfInterval
        //Just example
        if(!mainSleepStartString.equals(mainSleepEndString)) {
            Log.v("FDLSJK", "DFSLJ");
            clockView.setIsRecommended(true);
            clockView.setTypeOfInterval(1);
            clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);
        }else{
            Log.v("GONE", "GONE");
            clockView.setTypeOfInterval(1);
            clockView.setIsRecommended(false);
        }
    }

    public void napButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                               ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                               TextView sleepTypeText, TextView sleepImportanceText,
                               TextView stateDescriptionText, TextView stateDescriptionSmallText,
                               ImageView stateDescriptionImage, ClockView clockView)
    {
        stateDescriptionSmallText.setVisibility(View.VISIBLE);
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getNapSleepStart())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getNapSleepEnd())));
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        //Change the content of displaying text
        sleepTypeText.setText("낮잠");
        sleepImportanceText.setText("권장");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.recommend_caption, null));
        clockView.setTypeOfInterval(2);
        //Just example
        if(!napSleepStartString.equals(napSleepEndString)) {
            clockView.setIsRecommended(true);
            clockView.setAngleFromTime(napSleepStartString, napSleepEndString);
            stateDescriptionText.setText("낮잠을 주무세요");
            stateDescriptionSmallText.setText("이 시간에 충분히 자야해요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sleep, null));
        }else{
            clockView.setIsRecommended(false);
            if (isenough) {
                stateDescriptionText.setText("낮잠이 필요하지 않아요");
                stateDescriptionSmallText.setText("낮잠 없이도 맑은 정신을 유지할 수 있어요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
            } else {
                stateDescriptionText.setText("낮잠을 잘 수 없어요");
                stateDescriptionSmallText.setText("수면이 충분하지 않아요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
            }
            startTime.setText("--:--");
            endTime.setText("--:--");
        }
    }

    public void workButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                TextView sleepTypeText, TextView sleepImportanceText,
                                TextView stateDescriptionText, TextView stateDescriptionSmallText,
                                ImageView stateDescriptionImage, ClockView clockView)
    {
        clockView.setIsRecommended(true);
        //startTime.setText(workOnsetString);
        //endTime.setText(workOffsetString);
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getWorkOnset())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getWorkOffset())));
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        //Change the content of displaying text
        sleepTypeText.setText("근무");
        sleepImportanceText.setText("중요");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.important_caption, null));
        if (isenough) {
            stateDescriptionText.setText("근무 시간이에요");
            stateDescriptionSmallText.setText("맑은 정신을 유지할 수 있어요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
        } else {
            stateDescriptionText.setText("근무 시간이에요");
            stateDescriptionSmallText.setText("맑은 정신을 유지하기 어려워요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
        }
        clockView.setTypeOfInterval(3);
        //Just example
        clockView.setAngleFromTime(workOnsetString, workOffsetString);
    }
}