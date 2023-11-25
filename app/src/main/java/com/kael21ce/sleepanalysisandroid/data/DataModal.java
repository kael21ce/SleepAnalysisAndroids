package com.kael21ce.sleepanalysisandroid.data;

import java.util.ArrayList;
import java.util.List;

public class DataModal {

    // string variables for our name and job
    private String user;
    private List<SleepBackend> sleep;
    private List<V0Backend> v0;

    public DataModal(String username, List<Sleep> sleeps, List<V0> V0s) {
        this.user = username;
        sleep = new ArrayList<>();
        v0 = new ArrayList<>();
        for(Sleep sleep: sleeps){
            SleepBackend tempSleep = new SleepBackend();
            tempSleep.sleepStart = sleep.sleepStart;
            tempSleep.sleepEnd = sleep.sleepEnd;
            this.sleep.add(tempSleep);
        }
        for(V0 v0: V0s){
            V0Backend tempV0 = new V0Backend();
            tempV0.n = v0.n_val;
            tempV0.h = v0.H_val;
            tempV0.y = v0.y_val;
            tempV0.x = v0.x_val;
            tempV0.time = v0.time;
            this.v0.add(tempV0);
        }
    }

    public String getUsername() {
        return user;
    }

    public void setUsername(String username) {
        this.user = username;
    }

    public List<SleepBackend> getSleeps(){
        return sleep;
    }

    public void setSleeps(List<Sleep> sleeps){
        for(Sleep sleep: sleeps){
            SleepBackend tempSleep = new SleepBackend();
            tempSleep.sleepStart = sleep.sleepStart;
            tempSleep.sleepEnd = sleep.sleepEnd;
            this.sleep.add(tempSleep);
        }
    }

    public List<V0Backend> getV0s(){
        return v0;
    }

    public void setV0s(List<V0> V0s){
        for(V0 v0: V0s){
            V0Backend tempV0 = new V0Backend();
            tempV0.n = v0.n_val;
            tempV0.h = v0.H_val;
            tempV0.y = v0.y_val;
            tempV0.x = v0.x_val;
            tempV0.time = v0.time;
            this.v0.add(tempV0);
        }
    }

}