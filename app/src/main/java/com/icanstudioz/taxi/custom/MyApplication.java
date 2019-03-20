package com.icanstudioz.taxi.custom;

import android.os.Build;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.session.SessionManager;
import com.mapbox.mapboxsdk.Mapbox;

import io.fabric.sdk.android.Fabric;

/**
 * Created by android on 15/3/17.
 */

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapboxkey));

        SessionManager.initialize(getApplicationContext());

    }

   public static boolean isM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return true;
        return false;
    }

    public static boolean isO() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return true;
        return false;
    }
}
