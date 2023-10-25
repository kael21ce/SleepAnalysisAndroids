package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity
public class Awareness {

    @PrimaryKey
    public long awarenessDay;

    @ColumnInfo(name = "good_duration")
    public long goodDuration;

    @ColumnInfo(name = "bad_duration")
    public long badDuration;
}

