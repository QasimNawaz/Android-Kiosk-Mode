package com.example.alisons.cosu;

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.UserManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CosuActivity extends AppCompatActivity implements View.OnClickListener {

    private Button start, stop, startActivityOne, startActivityTwo;
    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;


    //TODO: later add this to check the state of the lock.
    private Boolean boolLockState;

    // add a variable to keep the component name in the application.
    private ComponentName mAdminComponentName;

    public static final String LOCK_ACTIVITY_KEY = "lock_activity";
    public static final int FROM_LOCK_ACTIVITY = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cosu);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        startActivityOne = findViewById(R.id.start_activity_one);
        startActivityTwo = findViewById(R.id.start_activity_two);
        AppContext.getInstance().startDefaultPoliciy(CosuActivity.this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        startActivityOne.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
//                AppContext.getInstance().startCosuMode(CosuActivity.this);
                break;
            case R.id.stop:
                AppContext.getInstance().stopCosuMode(CosuActivity.this);
                break;
            case R.id.start_activity_one:
                CosuActivity.this.startActivity(new Intent(CosuActivity.this, SampleActivity_1.class));
                CosuActivity.this.finish();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        AppContext.getInstance().onStart(CosuActivity.this);
    }





//    // the below methods allow us to take advantage of the latest device management apis
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void setDefaultCosuPolicies(boolean active) {
//        //set user-restrictions
//        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
//        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
//        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
//        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
//        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);
//
//        //Disable keyguard and status bar
//        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
//        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
//
//        //enable STAY_ON_WHILE_PLUGGED_IN
//        enableStayOnWhilePluggedIn(active);
//
//        //set system update policy
//        if (active) {
//            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
//        } else {
//            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null);
//        }
//
//        //set this activity as a lock task package
//        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, active ? new String[]{getPackageName()} : new String[]{});
//
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
//        intentFilter.addCategory(Intent.CATEGORY_HOME);
//        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
//
//        if (active) {
//            //set the cosu activity as home intent so that it is started on reboot
//            mDevicePolicyManager.addPersistentPreferredActivity(mAdminComponentName, intentFilter, new ComponentName(getPackageName(), CosuActivity.class.getName()));
//        } else {
//            mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, getPackageName());
//        }
//
//    }

//    private void enableStayOnWhilePluggedIn(boolean enabled) {
//        if (enabled) {
//            mDevicePolicyManager.setGlobalSetting(
//                    mAdminComponentName,
//                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
//                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
//                            | BatteryManager.BATTERY_PLUGGED_USB
//                            | BatteryManager.BATTERY_PLUGGED_WIRELESS));
//        } else {
//            mDevicePolicyManager.setGlobalSetting(
//                    mAdminComponentName,
//                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
//                    "0"
//            );
//        }
//    }
//
//    private void setUserRestriction(String restriction, boolean disallow) {
//        if (disallow) {
//            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
//        } else {
//            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
//        }
//    }
}
