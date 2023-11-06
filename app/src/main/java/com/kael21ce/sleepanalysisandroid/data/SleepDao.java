package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SleepDao {
    @Query("SELECT * FROM sleep ORDER BY sleep_start ASC")
    List<Sleep> getAll();

    @Query("SELECT * FROM sleep WHERE sleep_id IN (:userIds)")
    List<Sleep> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM sleep WHERE sleep_start LIKE :first AND " +
            "sleep_end LIKE :last LIMIT 1")
    Sleep findByName(String first, String last);

    @Query("UPDATE sleep SET sleep_start = :sleepStart, sleep_end = :sleepEnd "+
            "WHERE sleep_id = :sleepId")
    void updateSleep(long sleepId, long sleepStart, long sleepEnd);

    @Insert
    void insertAll(List<Sleep> sleeps);

    @Delete
    void delete(Sleep sleep);
}
