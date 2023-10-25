package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Sleep.class, V0.class, Awareness.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SleepDao sleepDao();
    public abstract V0Dao v0Dao();

    public abstract AwarenessDao awarenessDao();

}