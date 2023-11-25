package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity
public class V0Backend {

    @ColumnInfo(name = "y")
    public double y;

    @ColumnInfo(name = "x")
    public double x;

    @ColumnInfo(name = "n")
    public double n;

    @ColumnInfo(name = "h")
    public double h;

    @ColumnInfo(name = "time")
    public long time;

}
