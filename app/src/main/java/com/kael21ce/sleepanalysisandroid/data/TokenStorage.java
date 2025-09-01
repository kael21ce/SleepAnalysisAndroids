package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {
    private static final String PREF_NAME = "AuthTokenPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private static TokenStorage instance;
    private final SharedPreferences sharedPref;

    private TokenStorage(Context context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenStorage getInstance(Context context) {
        if (instance == null) {
            instance = new TokenStorage(context);
        }
        return instance;
    }

    public void saveTokens(TokenPair tokenPair) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_ACCESS_TOKEN, tokenPair.getAccessToken());
        editor.putString(KEY_REFRESH_TOKEN, tokenPair.getRefreshToken());
        editor.apply();
    }

    public void updateAccessToken(String accessToken) {
        sharedPref.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
    }

    public String getAccessToken() {
        return sharedPref.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPref.getString(KEY_REFRESH_TOKEN, null);
    }

    public void clearTokens() {
        sharedPref.edit().clear().apply();
    }
}
