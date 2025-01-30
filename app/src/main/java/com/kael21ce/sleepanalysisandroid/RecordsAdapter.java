package com.kael21ce.sleepanalysisandroid;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {
    ArrayList<Records> items = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.records, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Records item = items.get(position);
        holder.setItem(item);
    }

    public void addItem(Records item) {
        items.add(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView recordsDate, progressText, recordsText1, recordsDate1, recordsText2, recordsDate2, recordsText3, recordsDate3;
        ProgressBar progressBar;
        ImageView bullet2, bullet3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordsDate = itemView.findViewById(R.id.recordsDate);
            progressText = itemView.findViewById(R.id.progressText);
            recordsText1 = itemView.findViewById(R.id.recordsText1);
            recordsDate1 = itemView.findViewById(R.id.recordsDate1);
            recordsText2 = itemView.findViewById(R.id.recordsText2);
            recordsDate2 = itemView.findViewById(R.id.recordsDate2);
            recordsText3 = itemView.findViewById(R.id.recordsText3);
            recordsDate3 = itemView.findViewById(R.id.recordsDate3);
            progressBar = itemView.findViewById(R.id.progressBar);
            bullet2 = itemView.findViewById(R.id.bullet2);
            bullet3 = itemView.findViewById(R.id.bullet3);
        }
        public void setItem(Records item) {
            boolean type = item.isAlertness();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. M. d.", Locale.KOREA);
            SimpleDateFormat timeFormat = new SimpleDateFormat("a h:mm", Locale.KOREA);
            if (type) {
                // Text: recent 3 alertness survey
                Log.v("RecordsAdapter", "Size: " + item.dataSurvey.size());
                if (item.dataSurvey.size() == 0) {
                    Date latestDate = item.getRecordDate();
                    recordsDate.setText(dateFormat.format(latestDate));
                    progressText.setText("일일 목표 달성률: 0/3");
                    recordsText1.setText("일일 각성도 설문을 진행해주세요");
                    recordsDate1.setVisibility(View.INVISIBLE);
                    bullet2.setVisibility(View.INVISIBLE);
                    recordsText2.setVisibility(View.INVISIBLE);
                    recordsDate2.setVisibility(View.INVISIBLE);
                    bullet3.setVisibility(View.INVISIBLE);
                    recordsText3.setVisibility(View.INVISIBLE);
                    recordsDate3.setVisibility(View.INVISIBLE);
                    progressBar.setProgress(0);
                } else {
                    ArrayList<DataSurvey> latestAlerts = getLatestAlerts(item.getDataSurvey());
                    ArrayList<DataSurvey> alertsShow = new ArrayList<>();
                    Date latestDate1, latestDate2, latestDate3;
                    for (int l = 0; l < latestAlerts.size(); l++) {
                        if (latestAlerts.get(l).getTime() > 0) {
                            alertsShow.add(latestAlerts.get(l));
                        }
                    }
                    Date latestDate = new Date(alertsShow.get(0).getTime());
                    recordsDate.setText(dateFormat.format(latestDate));
                    progressText.setText("일일 목표 달성률: " + alertsShow.size() + "/3");
                    progressBar.setProgress(100*alertsShow.size()/3);
                    recordsText1.setText("각성도: " + alertsShow.get(0).getSurvey_result());
                    if (alertsShow.size() == 3) {
                        latestDate1 = new Date(alertsShow.get(0).getTime());
                        recordsDate1.setText(timeFormat.format(latestDate1));
                        recordsText2.setText("각성도: " + alertsShow.get(1).getSurvey_result());
                        latestDate2 = new Date(alertsShow.get(1).getTime());
                        recordsDate2.setText(timeFormat.format(latestDate2));
                        recordsText3.setText("각성도: " + alertsShow.get(2).getSurvey_result());
                        latestDate3 = new Date(alertsShow.get(2).getTime());
                        recordsDate3.setText(timeFormat.format(latestDate3));
                    } else if (alertsShow.size() == 2) {
                        latestDate1 = new Date(alertsShow.get(0).getTime());
                        recordsDate1.setText(timeFormat.format(latestDate1));
                        recordsText2.setText("각성도: " + alertsShow.get(1).getSurvey_result());
                        latestDate2 = new Date(alertsShow.get(1).getTime());
                        recordsDate2.setText(timeFormat.format(latestDate2));
                        recordsText3.setVisibility(View.INVISIBLE);
                        bullet3.setVisibility(View.INVISIBLE);
                        recordsDate3.setVisibility(View.INVISIBLE);
                    } else {
                        latestDate1 = new Date(alertsShow.get(0).getTime());
                        recordsDate1.setText(timeFormat.format(latestDate1));
                        recordsText2.setVisibility(View.INVISIBLE);
                        recordsText2.setVisibility(View.INVISIBLE);
                        bullet2.setVisibility(View.INVISIBLE);
                        recordsDate2.setVisibility(View.INVISIBLE);
                        recordsText3.setVisibility(View.INVISIBLE);
                        bullet3.setVisibility(View.INVISIBLE);
                        recordsDate3.setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                // Text: daily alertness, daily sleep quality
                if (item.dataMood.size() == 0) {
                    Date latestDate = item.getRecordDate();
                    recordsDate.setText(dateFormat.format(latestDate));
                    progressText.setText("일일 목표 달성률: 0/1");
                    recordsText1.setText("오후 12시 이후 일일 설문을 진행해주세요");
                    recordsDate1.setVisibility(View.INVISIBLE);
                    bullet2.setVisibility(View.INVISIBLE);
                    recordsText2.setVisibility(View.INVISIBLE);
                    progressBar.setProgress(0);
                } else {
                    DataMood latestMood = getLatestMood(item.getDataMood());
                    Date latestDate = new Date(latestMood.getTime());
                    recordsDate.setText(dateFormat.format(latestDate));
                    progressText.setText("일일 목표 달성률: " + item.getDataMood().size() + "/1");
                    recordsText1.setText("일별 각성도: " + latestMood.getDaily_alertness());
                    recordsDate1.setText(timeFormat.format(latestDate));
                    recordsText2.setText("일별 수면의 질: " + latestMood.getSleep_quality());
                    progressBar.setProgress(100);
                }
                recordsDate2.setVisibility(View.INVISIBLE);
                recordsText3.setVisibility(View.INVISIBLE);
                recordsDate3.setVisibility(View.INVISIBLE);
                bullet3.setVisibility(View.INVISIBLE);
            }
        }

        public DataMood getLatestMood(ArrayList<DataMood> moodList) {
            long baseTime = 0;
            int index = -1;
            for (int i = 0; i < moodList.size(); i++) {
                DataMood mood = moodList.get(i);
                if (mood.getTime() >= baseTime) {
                    baseTime = mood.getTime();
                    index = i;
                }
            }
            if (index >= 0) {
                return moodList.get(index);
            } else {
                return null;
            }
        }

        public ArrayList<DataSurvey> getLatestAlerts(ArrayList<DataSurvey> surveyList) {
            DataSurvey dummySurvey;
            ArrayList<DataSurvey> results = new ArrayList<>(3);
            ArrayList<Long> times = new ArrayList<>(3);
            for (int k = 0; k < 3; k++) {
                dummySurvey = new DataSurvey("tester",0,0,0,0,0);
                results.add(dummySurvey);
                times.add(0L);
            }
            for (int i = 0; i < surveyList.size(); i++) {
                DataSurvey alert = surveyList.get(i);
                long alertTime = alert.getTime();
                if (alertTime >= times.get(2)) {
                    results.set(0, results.get(1));
                    results.set(1, results.get(2));
                    results.set(2, alert);

                    times.set(0, times.get(1));
                    times.set(1, times.get(2));
                    times.set(2, alertTime);
                } else if (alertTime >= times.get(1)) {
                    results.set(0, results.get(1));
                    results.set(1, alert);

                    times.set(0, times.get(1));
                    times.set(1, alertTime);
                } else if (alertTime >= times.get(0)) {
                    results.set(0, alert);
                    times.set(0, alertTime);
                }
            }
            return results;
        }


    }
}