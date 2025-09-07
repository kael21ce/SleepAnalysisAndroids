package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;

import androidx.room.Room;

public class AppDatabaseSingleton {

    // 1. 단 하나의 인스턴스를 저장할 변수를 만든다 (private static)
    private static AppDatabase instance;

    // 2. 다른 곳에서 new로 생성하지 못하도록 생성자를 막는다 (private)
    private AppDatabaseSingleton() {}

    // 3. 데이터베이스 인스턴스를 얻는 유일한 통로를 만든다 (public static)
    public static synchronized AppDatabase getInstance(Context context) {
        // 인스턴스가 아직 없으면 새로 생성
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "sleep_wake"
                    )
                    .allowMainThreadQueries()
                    .build();
        }
        // 이미 만들어진 인스턴스가 있으면 그것을 반환
        return instance;
    }
}
