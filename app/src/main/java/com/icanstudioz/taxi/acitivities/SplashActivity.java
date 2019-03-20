package com.icanstudioz.taxi.acitivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import com.icanstudioz.taxi.R;
import com.icanstudioz.taxi.session.SessionManager;
import com.thebrownarrow.permissionhelper.ActivityManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

/**
 * Created by android on 7/3/17.
 */

public class SplashActivity extends ActivityManagePermission {
    private final static int SPLASH_TIME_OUT = 2000;
    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

//Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.splash_activity);
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                Askpermission();
                // This method will be executed once the timer is over
                // Start your app main activity


                // close this activity

            }
        }, SPLASH_TIME_OUT);

    }

    public void Askpermission() {
        askCompactPermissions(permissionAsk, new PermissionResult() {
            @Override
            public void permissionGranted() {
                redirect();
            }

            @Override
            public void permissionDenied() {
                redirect();

            }

            @Override
            public void permissionForeverDenied() {
                redirect();
            }
        });
    }

    private void redirect() {
       // SessionManager.getInstance().setPref(getApplicationContext());

        if (SessionManager.isLoggedIn()) {
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        } else {
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
        }
        finish();

    }

}
