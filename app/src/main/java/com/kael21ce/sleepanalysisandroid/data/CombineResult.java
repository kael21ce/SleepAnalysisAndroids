package com.kael21ce.sleepanalysisandroid.data;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public // List<V0>, ArrayList<BarEntry>를 감싸는 결과 클래스: 매 함수마다 전달
// 추가로 다른 list들도 함께 전달 가능
class CombineResult {
    private final List<Sleep> sleeps;
    private final List<V0> v0s;
    private final ArrayList<BarEntry> barEntries;
    private final List<Awareness> awarenesses, sleepAwarenesses;

    public CombineResult(List<Sleep> sleeps, List<V0> v0s, ArrayList<BarEntry> barEntries,
                         List<Awareness> awarenesses, List<Awareness> sleepAwarenesses) {
        this.sleeps = sleeps;
        this.v0s = v0s;
        this.barEntries = barEntries;
        this.awarenesses = awarenesses;
        this.sleepAwarenesses = sleepAwarenesses;
    }

    // Getter
    public List<Sleep> getSleeps() {
        return sleeps;
    }
    public List<V0> getV0s() {
        return v0s;
    }

    public ArrayList<BarEntry> getBarEntries() {
        return barEntries;
    }
    public List<Awareness> getAwarenesses() {
        return awarenesses;
    }
    public List<Awareness> getSleepAwarenesses() {
        return sleepAwarenesses;
    }
}

