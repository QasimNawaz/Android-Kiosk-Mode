package com.example.alisons.cosu;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

/**
 * Created by alisons on 12/7/2017.
 */

public class CosuService extends Service {

    private static final String TAG = CosuService.class.getSimpleName();
    //    private Thread t = null;
    private Context ctx = null;
    private boolean running = false;

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service 'KioskService'");
        running = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service 'KioskService'");
        running = true;
        ctx = this;
        // start a thread that periodically checks if your app is in the foreground
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    handleKioskMode();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Thread interrupted: 'KioskService'");
                    }
                } while (running);
                stopSelf();
            }
        });

        t.start();
        return Service.START_NOT_STICKY;
    }

    private void handleKioskMode() {
        // is Kiosk Mode active?
        if (PrefUtils.isCosuModeActive(ctx)) {
            // is App in background?
            if (isInBackground()) {
                restoreApp(); // restore!
            }
        }
    }

    private void restoreApp() {
        // Restart activity
        Intent i = new Intent(ctx, CosuActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    private boolean isInBackground() {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return (!ctx.getApplicationContext().getPackageName().equals(componentInfo.getPackageName()));
    }
}
