package com.kael21ce.sleepanalysisandroid.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface V0Dao {
    @Query("SELECT * FROM V0")
    List<V0> getAll();

    @Insert
    void insertAll(List<V0> v0s);

    @Delete
    void delete(V0 v0);
}
