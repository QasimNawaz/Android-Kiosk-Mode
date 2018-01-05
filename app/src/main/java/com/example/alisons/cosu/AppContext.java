package com.example.alisons.cosu;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.StrictMode;
import android.os.UserManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;


/**
 * Created by alisons on 12/7/2017.
 */

public class AppContext extends Application {

    private static AppContext mInstance;
    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;


    //TODO: later add this to check the state of the lock.
    private Boolean boolLockState;

    // add a variable to keep the component name in the application.
    private ComponentName mAdminComponentName;

    public static final String LOCK_ACTIVITY_KEY = "lock_activity";
    public static final int FROM_LOCK_ACTIVITY = 1;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Fresco.initialize(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startKioskService();  // add this
    }

    private void startKioskService() {
        startService(new Intent(this, CosuService.class));
    }

    public static synchronized AppContext getInstance() {
        return mInstance;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startDefaultPoliciy(CosuActivity cosuActivity) {
        //set default COSU policy
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolLockState = false;

        // Retrieve DeviceAdminReceiver ComponentName so we can make
        // device management api calls later

        //mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
        // Retrieve Package Manager so that we can enable and
        // disable LockedActivity
        mPackageManager = this.getPackageManager();
        // call the methods to implement the device policies.
        mAdminComponentName = com.example.alisons.cosu.DeviceAdminReceiver.getComponentName(this);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            setDefaultCosuPolicies(true);
        } else {
            Toast.makeText(getApplicationContext(), "Application not set to device owner", Toast.LENGTH_SHORT).show();
        }

        // check that the activity is starting the correct lockedActvity.
        Intent intent = cosuActivity.getIntent();

        if (intent.getIntExtra(CosuActivity.LOCK_ACTIVITY_KEY, 0) ==
                CosuActivity.FROM_LOCK_ACTIVITY) {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                    mAdminComponentName, getPackageName());
            mPackageManager.setComponentEnabledSetting(
                    new ComponentName(getApplicationContext(), CosuActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startCosuMode(CosuActivity cosuActivity) {
        if (mDevicePolicyManager.isDeviceOwnerApp(getApplicationContext().getPackageName())) {
            Intent intentLock = new Intent(getApplicationContext(), CosuActivity.class);
            mPackageManager.setComponentEnabledSetting(
                    new ComponentName(getApplicationContext(),
                            CosuActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            startActivity(intentLock);
        }

        //setup the locking of the device here.
        if (mDevicePolicyManager.isLockTaskPermitted(getApplicationContext().getPackageName())) {
            Intent intentLock = new Intent(getApplicationContext(), CosuActivity.class);
            startActivity(intentLock);
            cosuActivity.finish();
//
//            SplashScreen.this.startActivity(new Intent(SplashScreen.this, MainActivity.class));
//            SplashScreen.this.finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void stopCosuMode(CosuActivity cosuActivity) {
        // unlock the device here.
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_LOCKED) {
            cosuActivity.stopLockTask();
            PrefUtils.setCosuModeActive(false, getApplicationContext());
        }
        // set the policies to false and enable everything back.
        setDefaultCosuPolicies(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onStart(CosuActivity cosuActivity) {
        // start the locked mode if the activity is not already started.

        /*TODO: Setup a boolean at the time of locking and check it everytime to make sure that
        unlocked state is not given*/

        if (mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                // start the lock
                cosuActivity.startLockTask();
                PrefUtils.setCosuModeActive(true, getApplicationContext());
            }
        }
    }

    // the below methods allow us to take advantage of the latest device management apis
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setDefaultCosuPolicies(boolean active) {
        //set user-restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        //Disable keyguard and status bar
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
//        mDevicePolicyManager.setPermissionPolicy(mAdminComponentName, createKeyguardDisabledFlag());
        mDevicePolicyManager.setCameraDisabled(mAdminComponentName, false);
        Log.d("Boolean", "" + mDevicePolicyManager.getCameraDisabled(mAdminComponentName));

        //enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

        //set system update policy
        if (active) {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null);
        }

        //set this activity as a lock task package
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, active ? new String[]{getPackageName()} : new String[]{});

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            //set the cosu activity as home intent so that it is started on reboot
            mDevicePolicyManager.addPersistentPreferredActivity(mAdminComponentName, intentFilter, new ComponentName(getPackageName(), CosuActivity.class.getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, getPackageName());
        }

    }

    int createKeyguardDisabledFlag() {
        int flags = DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE;
        flags |= DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL;
//        flags |= DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA;
        return flags;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void enableStayOnWhilePluggedIn(boolean enabled) {
        if (enabled) {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
                            | BatteryManager.BATTERY_PLUGGED_USB
                            | BatteryManager.BATTERY_PLUGGED_WIRELESS));
        } else {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    "0"
            );
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
        }
    }

    public boolean isActiveAdmin() {
        if (mDevicePolicyManager.isAdminActive(mAdminComponentName)) {
            return true;
        } else {
            return false;
        }
    }
}
