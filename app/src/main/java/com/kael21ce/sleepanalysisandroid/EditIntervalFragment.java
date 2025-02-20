package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditIntervalFragment extends Fragment implements ButtonTextUpdater {

    public Button startTimeEditButton;
    public Button endTimeEditButton;
    private TextView intervalTextView;
    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "yyyy/MM/dd H:mm", Locale.KOREA);
    SimpleDateFormat sdf;
    SimpleDateFormat sdfSimple = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    SimpleDateFormat sdf24H = new SimpleDateFormat("H:mm", Locale.getDefault());
    SimpleDateFormat sdfAMPM;

    private static final String TAG = "EditIntervalFragment";
    private static final long oneDay = 1000*60*60*24;
    private final String languageSetting = Locale.getDefault().getLanguage();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_interval, container, false);
        ImageButton backButtonEdit = v.findViewById(R.id.backButtonEdit);
        intervalTextView = v.findViewById(R.id.editIntervalText);
        Button deleteButton = v.findViewById(R.id.deleteButton);
        Button editButton = v.findViewById(R.id.editButton);
        startTimeEditButton = v.findViewById(R.id.startTimeEditButton);
        endTimeEditButton = v.findViewById(R.id.endTimeEditButton);

        MainActivity mainActivity = (MainActivity)getActivity();

        // Setup for alert dialog
        View dimBackground = getParentFragment() != null ? getParentFragment().getView().findViewById(R.id.dimBackgroundSched) : v.findViewById(R.id.dimBackgroundEditIntv);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        TimeZone timeZone = TimeZone.getDefault();
        sdfDateTime.setTimeZone(timeZone);
        sdf24H.setTimeZone(timeZone);
        sdfSimple.setTimeZone(timeZone);

        IntervalFragment intervalFragment = new IntervalFragment();
        //Back to intervalFragment if backButtonEdit is clicked
        Bundle bundle = this.getArguments();
        assert bundle != null;
        intervalFragment.setArguments(bundle.getBundle("bundle"));
        backButtonEdit.setOnClickListener(view -> getParentFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit());

        //Set the time picker to each timeEditButton
        EditIntervalFragment editIntervalFragment = this;

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("isSchedule", true);
        editor.apply();

        //get the bundle
        String date = bundle.getString("date");
        String startHour = bundle.getString("startHour");
        String endHour = bundle.getString("endHour");
        long startSleep = 0;
        long endSleep = 0;
        Date startHourD = new Date();
        Date endHourD = new Date();
        try {
            startSleep = sdfDateTime.parse(date + " " + startHour).getTime();
            endSleep = sdfDateTime.parse(date + " " + endHour).getTime();
            startHourD = sdf24H.parse(startHour);
            endHourD = sdf24H.parse(endHour);
        } catch (ParseException e) {
            Log.e("EditIntervalFragment", e.toString());

            if (languageSetting.equals("ko")) {
                showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                        "시스템 에러",
                        "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
            } else {
                showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                        "System Error",
                        "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
            }
        }

        if (languageSetting.equals("ko")) {
            sdf = new SimpleDateFormat("yyyy/MM/dd a h:mm", Locale.KOREA);
            sdfAMPM = new SimpleDateFormat("a h:mm", Locale.KOREA);
        } else {
            sdf = new SimpleDateFormat("yyyy/MM/dd h:mm a", Locale.getDefault());
            sdfAMPM = new SimpleDateFormat("h:mm a", Locale.getDefault());
        }
        sdf.setTimeZone(timeZone);
        sdfAMPM.setTimeZone(timeZone);

        Log.v("AM PM FORMAT", sdfAMPM.format(startHourD));
        Log.v("AM PM FORMAT", sdfAMPM.format(endHourD));

        startTimeEditButton.setText(sdfAMPM.format(startHourD));
        endTimeEditButton.setText(sdfAMPM.format(endHourD));
        //Set the text of editIntervalText
        try {
            intervalTextView.setText("수면 시간: "
                   + getInterval(sdfAMPM.format(startHourD), sdfAMPM.format(endHourD)));
        } catch (ParseException e) {
            Log.e("EditIntervalFragment", e.toString());

            // Display alert message
            if (languageSetting.equals("ko")) {
                showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                        "시스템 에러",
                        "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
            } else {
                showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                        "System Error",
                        "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
            }
        }

        startTimeEditButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), editIntervalFragment);
            timePickerDialog.setData(1);
            timePickerDialog.setTimePicker((String) startTimeEditButton.getText());
            timePickerDialog.show();
        });
        endTimeEditButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), editIntervalFragment);
            timePickerDialog.setData(0);
            timePickerDialog.setTimePicker((String) endTimeEditButton.getText());
            timePickerDialog.show();
        });

        //Delete interval if deleteButton is clicked
        long finalStartSleep = startSleep;
        long finalEndSleep = endSleep;
        Sleep initSleep = new Sleep();
        initSleep.sleepStart = finalStartSleep;
        initSleep.sleepEnd = finalEndSleep;
        deleteButton.setOnClickListener(view -> {
            Log.v("DELETED", "DELETED");
            mainActivity.deleteSleep(initSleep);

            Intent scheduleIntent = new Intent(mainActivity, SplashActivity.class);

            //Send the information of the selected date
            Calendar calendar = Calendar.getInstance();
            try {
                Date curDate = sdfSimple.parse(date);
                calendar.setTime(curDate);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                Log.v(TAG, "Selected: " + year + "-" + month + 1 + "-" + day);

                scheduleIntent.putExtra("Year", year);
                scheduleIntent.putExtra("Month", month);
                scheduleIntent.putExtra("Day", day);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            startActivity(scheduleIntent);
        });

        //Edit interval if editButton is clicked
        editButton.setOnClickListener(view -> {
            Log.v("EDITED", "EDITED");
            Sleep edit_sleep = new Sleep();
            String startTime = (String) startTimeEditButton.getText();
            String endTime = (String) endTimeEditButton.getText();
            String startSDF = date + ' ' + startTime;
            String endSDF = date + ' ' + endTime;
            Log.v("START SDF", startSDF);
            Log.v("END SDF", endSDF);

            Date sleepStartDate = null;
            Date sleepEndDate = null;
            try {
                sleepStartDate = sdf.parse(startSDF);
                sleepEndDate = sdf.parse(endSDF);
            } catch (ParseException e) {
                Log.e("EditIntervalFragment", e.toString());

                // Display alert message
                if (languageSetting.equals("ko")) {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "시스템 에러",
                            "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
                } else {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "System Error",
                            "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
                }
            }
            Log.v("START DATE", sleepStartDate.toString());
            Log.v("END DATE", sleepEndDate.toString());

            Date curDate = new Date();
            try {
                curDate = sdfSimple.parse(date);
            } catch (ParseException e) {
                Log.e("EditIntervalFragment", e.toString());

                // Display alert message
                if (languageSetting.equals("ko")) {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "시스템 에러",
                            "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
                } else {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "System Error",
                            "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
                }
            }

            long sleepStartMillis = sleepStartDate.getTime();
            long sleepEndMillis = sleepEndDate.getTime();

            Calendar midnightCalendar = Calendar.getInstance();
            midnightCalendar.setTime(curDate);

            midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
            midnightCalendar.set(Calendar.MINUTE, 0);
            midnightCalendar.set(Calendar.SECOND, 0);
            midnightCalendar.set(Calendar.MILLISECOND, 0);

            long midnight = midnightCalendar.getTimeInMillis();
            boolean isMidnight = false;

            if (sleepEndDate.getTime() == midnight) {
                sleepEndMillis += oneDay;
                isMidnight = true;
            }
            if(sleepStartMillis <= sleepEndMillis) {
                edit_sleep.sleepStart = sleepStartMillis;
                edit_sleep.sleepEnd = sleepEndMillis;

                if (isMidnight) {
                    initSleep.sleepEnd += oneDay;
                }

                Intent scheduleIntent = new Intent(mainActivity, SplashActivity.class);

                //Send the information of the selected date
                Calendar calendar = Calendar.getInstance();
                int year = -1, month = -1, day = -1;
                calendar.setTime(curDate);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);

                Log.v(TAG, "Selected: " + year + "-" + (month + 1) + "-" + day);

                scheduleIntent.putExtra("Year", year);
                scheduleIntent.putExtra("Month", month);
                scheduleIntent.putExtra("Day", day);
                mainActivity.editSleep(initSleep, edit_sleep);

                startActivity(scheduleIntent);
            }else{
                if (languageSetting.equals("ko")) {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "오류",
                            "수면 시작 시각이 종료 시각보다 일러야 합니다.", "확인");
                } else {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "ERROR",
                            "Start of the sleep has to be before the end of the sleep", "OK");
                }
            }
        });

        return v;
    }

    public void setDateButtonText(String text, int isStartButton) {

    }

    public void setTimeButtonText(String text, int isStartButton) {
        if (isStartButton==1) {
            if (startTimeEditButton != null && endTimeEditButton != null) {
                startTimeEditButton.setText(text);

                Date startHourD = new Date();
                Date endHourD = new Date();
                try {
                    startHourD = sdfAMPM.parse(text);
                    endHourD = sdfAMPM.parse(endTimeEditButton.getText().toString());
                } catch (ParseException e) {
                    Log.e("EditIntervalFragment", e.toString());

                    // Display alert message
                    if (languageSetting.equals("ko")) {
                        showAlertDialogNoDim("시스템 에러",
                                "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
                    } else {
                        showAlertDialogNoDim("System Error",
                                "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
                    }
                }

                try {
                    intervalTextView.setText("수면 시간: "
                            + getInterval(sdfAMPM.format(startHourD),
                            sdfAMPM.format(endHourD)));
                } catch (ParseException e) {
                    Log.e("EditIntervalFragment", e.toString());

                    // Display alert message
                    if (languageSetting.equals("ko")) {
                        showAlertDialogNoDim("시스템 에러",
                                "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
                    } else {
                        showAlertDialogNoDim("System Error",
                                "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
                    }
                }
            }
        } else {
            if (startTimeEditButton != null && endTimeEditButton != null) {
                endTimeEditButton.setText(text);

                Date startHourD = new Date();
                Date endHourD = new Date();

                try {
                    startHourD = sdfAMPM.parse(startTimeEditButton.getText().toString());
                    endHourD = sdfAMPM.parse(text);
                } catch (ParseException e) {
                    Log.e("EditIntervalFragment", e.toString());

                    // Display alert message
                    if (languageSetting.equals("ko")) {
                        showAlertDialogNoDim("시스템 에러",
                                "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
                    } else {
                        showAlertDialogNoDim("System Error",
                                "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
                    }
                }

                try {
                    intervalTextView.setText("수면 시간: "
                            + getInterval(sdfAMPM.format(startHourD),
                            sdfAMPM.format(endHourD)));
                } catch (ParseException e) {
                    Log.e("EditIntervalFragment", e.toString());

                    // Display alert message
                    if (languageSetting.equals("ko")) {
                        showAlertDialogNoDim("시스템 에러",
                                "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
                    } else {
                        showAlertDialogNoDim("System Error",
                                "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
                    }
                }
            }
        }
    }

    public String getDateButtonText(int isStartButton) {return "";}

    //Convert Format of "aaa HH:mm" to "HH:mm"
    public String convertAToFormat(String time) throws ParseException {
        Date date = sdfAMPM.parse(time);
        return sdf24H.format(date);
    }

    //Calculate the interval between two time in format of "HH:mm"
    public String getInterval(String start, String end) throws ParseException {
        String languageSetting = Locale.getDefault().getLanguage();
        DateTimeFormatter formatter;
        if (languageSetting.equals("ko")) {
            formatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA);
        } else {
            formatter = DateTimeFormatter.ofPattern("h:mm a");
        }

        LocalTime startTime = LocalTime.parse(start, formatter);
        LocalTime endTime = LocalTime.parse(end, formatter);

        long differenceInMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (differenceInMinutes < 0) {
            differenceInMinutes = differenceInMinutes + 24*60;
        }

        long intervalHour = differenceInMinutes / 60;
        long intervalMinute = differenceInMinutes % 60;

        return intervalHour + "시간 " + intervalMinute + "분";
    }

    private void showAlertDialog(View dimBackground, ActionBar actionBar,
                                 BottomNavigationView bottomNavigationView, String title,
                                 String message, String buttonText) {
        // Save the original color
        // Change this part if someone tries to change the primary color
        int originalActionBarColor = getResources().getColor(R.color.white, null);
        Window window = getActivity().getWindow();

        // Dim effect
        dimBackground.setVisibility(View.VISIBLE);
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dim, null)));
        }
        if (bottomNavigationView != null) {
            bottomNavigationView.setItemBackground(new ColorDrawable(Color.parseColor("#80000000")));
        }
        window.setStatusBarColor(getResources().getColor(R.color.dim, null));

        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.layout_custom_dialog,
                (LinearLayout) getActivity().findViewById(R.id.DialogLayout));
        TextView dialogTitle = (TextView) viewDialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = (TextView) viewDialog.findViewById(R.id.dialogMessage);
        Button dialogButton = (Button) viewDialog.findViewById(R.id.dialogButton);
        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialogButton.setText(buttonText);

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogButton.setOnClickListener(dialogV -> {
            dialog.dismiss();
            dimBackground.setVisibility(View.GONE);
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(originalActionBarColor));
            }
            if (bottomNavigationView != null) {
                bottomNavigationView.setItemBackground(new ColorDrawable(Color.parseColor("#FFFFFF")));
            }
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
        });
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }

    private void showAlertDialogNoDim(String title, String message, String buttonText) {
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.layout_custom_dialog,
                (LinearLayout) getActivity().findViewById(R.id.DialogLayout));
        TextView dialogTitle = (TextView) viewDialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = (TextView) viewDialog.findViewById(R.id.dialogMessage);
        Button dialogButton = (Button) viewDialog.findViewById(R.id.dialogButton);
        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialogButton.setText(buttonText);

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogButton.setOnClickListener(dialogV -> {
            dialog.dismiss();
        });
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }
}