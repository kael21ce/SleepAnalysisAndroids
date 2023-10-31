package com.kael21ce.sleepanalysisandroid;

public class Bar {
    String date;
    Integer positiveWeight;
    Integer negativeWeight;

    public Bar(String date, Integer positiveWeight, Integer negativeWeight) {
        this.date = date;
        this.positiveWeight = positiveWeight;
        this.negativeWeight = negativeWeight;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getPositiveWeight() {
        return positiveWeight;
    }

    public void setPositiveWeight(Integer positiveWeight) {
        this.positiveWeight = positiveWeight;
    }

    public Integer getNegativeWeight() {
        return negativeWeight;
    }

    public void setNegativeWeight(Integer negativeWeight) {
        this.negativeWeight = negativeWeight;
    }
}
