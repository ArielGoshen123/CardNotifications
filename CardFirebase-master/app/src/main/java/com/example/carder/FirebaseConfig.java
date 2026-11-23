package com.example.carder;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseConfig {

    // ↓↓↓ מעתיקים את שלושת הערכים מ-FirebaseConfig.java של פרויקט פרק 11 ↓↓↓
    public static final String API_KEY = "AIzaSyDSO4_LC6b5eedQKXUGl4E4zotSBG1u-LU";
    public static final String APPLICATION_ID = "1:574680545077:android:b6734accbc4dd8ca33be7c";
    public static final String DATABASE_URL = "https://card-444bc-default-rtdb.europe-west1.firebasedatabase.app/";
    // ↑↑↑ בלי רווחים, ובתוך המרכאות ↑↑↑

    // האם מילאו את שלושת הערכים?
    public static boolean isConfigured() {
        return !API_KEY.isEmpty() && !APPLICATION_ID.isEmpty() && !DATABASE_URL.isEmpty();
    }

    // מחבר את האפליקציה ל-Firebase. מספיק פעם אחת: בפעם השנייה יוצאים מיד
    public static void ensureInitialized(Context context) {
        if (!FirebaseApp.getApps(context).isEmpty()) {
            return;                                     // כבר מחובר
        }
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey(API_KEY)
                .setApplicationId(APPLICATION_ID)
                .setDatabaseUrl(DATABASE_URL)
                .build();
        FirebaseApp.initializeApp(context.getApplicationContext(), options);
    }
}