package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AwarenessDao {
    @Query("SELECT * FROM awareness")
    List<Awareness> getAll();

    @Query("SELECT * FROM awareness WHERE awarenessDay IN (:userIds)")
    List<Awareness> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM awareness WHERE awarenessDay = :awarenessDay")
    Awareness findByDay(Long awarenessDay);

    @Query("UPDATE awareness SET good_duration = :goodDuration AND bad_duration = :badDuration "+
            "WHERE awarenessDay = :awarenessDay")
    void updateAwareness(long awarenessDay, long goodDuration, long badDuration);

    @Insert
    void insertAll(List<Awareness> awarenesses);

    @Delete
    void delete(Awareness awareness);
}
