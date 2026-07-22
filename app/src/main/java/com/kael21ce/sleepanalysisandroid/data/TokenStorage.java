package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;
import android.content.SharedPreferences;

// iOS의 TokenStorage(Keychain)에 대응. 나머지 앱 전체가 쓰는 "SleepWake" 프리퍼런스에
// 같이 저장해서, 로그아웃 시 기존 editor.clear() 호출로 토큰도 함께 지워지도록 한다.
public final class TokenStorage {
    private static final String PREFS = "SleepWake";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";

    private TokenStorage() {}

    public static void save(Context context, String access, String refresh) {
        prefs(context).edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .apply();
    }

    public static void saveAccess(Context context, String access) {
        prefs(context).edit().putString(KEY_ACCESS, access).apply();
    }

    public static String getAccess(Context context) {
        return prefs(context).getString(KEY_ACCESS, null);
    }

    public static String getRefresh(Context context) {
        return prefs(context).getString(KEY_REFRESH, null);
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
