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

    public long getSleepId(){
        return sleep_id;
    }

    public long getSleepStart(){
        return sleepStart;
    }

    public long getSleepEnd(){
        return sleepEnd;
    }

    @Override
    public boolean equals(Object anObject) {
        if (!(anObject instanceof Sleep)) {
            return false;
        }
        Sleep otherMember = (Sleep)anObject;
        return ((otherMember.getSleepStart() == getSleepStart()) && (otherMember.getSleepEnd() == getSleepEnd()));
    }
}

