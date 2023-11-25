package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity
public class SleepBackend {

    @ColumnInfo(name = "sleepStart")
    public long sleepStart;

    @ColumnInfo(name = "sleepEnd")
    public long sleepEnd;
}

