package com.kael21ce.sleepanalysisandroid;

//For update text of button
public interface ButtonTextUpdater {
    //In AddIntervalFragment
    void setDateButtonText(String text, int isStartButton);
    void setTimeButtonText(String text, int isStartButton);
    String getDateButtonText(int isStartButton);
}
