package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity
public class V0 {
    @PrimaryKey(autoGenerate = true)
    public int v0_id;

    @ColumnInfo(name = "y_val")
    public Double y_val;

    @ColumnInfo(name = "x_val")
    public Double x_val;

    @ColumnInfo(name = "n_val")
    public Double n_val;

    @ColumnInfo(name = "H_val")
    public Double H_val;

    @ColumnInfo(name = "time")
    public Long time;

}
