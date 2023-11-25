package com.kael21ce.sleepanalysisandroid.data;

import java.util.List;

public class DataModal {

    // string variables for our name and job
    private String username;
    private List<Sleep> sleeps;
    private List<V0> V0s;

    public DataModal(String username, List<Sleep> sleeps, List<V0> V0s) {
        this.username = username;
        this.sleeps = sleeps;
        this.V0s = V0s;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Sleep> getSleeps(){
        return sleeps;
    }

    public void setSleeps(List<Sleep> sleeps){
        this.sleeps = sleeps;
    }

    public List<V0> getV0s(){
        return V0s;
    }

    public void setV0s(List<V0> V0s){
        this.V0s = V0s;
    }

}