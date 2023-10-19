package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity
public class Sleep {
    @PrimaryKey(autoGenerate = true)
    public int sleep_id;

    @ColumnInfo(name = "sleep_start")
    public long sleepStart;

    @ColumnInfo(name = "sleep_end")
    public long sleepEnd;
}

