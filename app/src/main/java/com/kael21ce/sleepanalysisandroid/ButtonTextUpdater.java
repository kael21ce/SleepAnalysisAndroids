package com.kael21ce.sleepanalysisandroid;

//For update text of button
public interface ButtonTextUpdater {
    //In AddIntervalFragment
    void setDateButtonText(String text, Boolean isStartButton);
    void setTimeButtonText(String text, Boolean isStartButton);
}
