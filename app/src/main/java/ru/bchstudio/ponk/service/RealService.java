package ru.bchstudio.ponk.service;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Calendar;

import ru.bchstudio.ponk.MainActivity;
import ru.bchstudio.ponk.OnWebAsyncTaskCompleted;
import ru.bchstudio.ponk.R;
import ru.bchstudio.ponk.WebAsyncTask;

public class RealService extends Service  implements OnWebAsyncTaskCompleted {
    private Thread mainThread;
    public static Intent serviceIntent = null;
    private static final String myURL = "http://89.169.90.244:5000/test";

    public RealService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        serviceIntent = intent;


        if (intent == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(1010, prepareNotification(R.mipmap.ic_launcher));
        newShowToast(getApplication(), "Start Foreground Service");

        final OnWebAsyncTaskCompleted lstnr = this;

        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {

                boolean run = true;
                while (run) {
                    try {


                        new WebAsyncTask(myURL,lstnr).execute();
                        Thread.sleep(1000 * 5);


                    } catch (InterruptedException e) {
                        run = false;
                        e.printStackTrace();
                    }
                }
            }
        });
        mainThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        serviceIntent = null;
        setAlarmTimer();
        Thread.currentThread().interrupt();

        if (mainThread != null) {
            mainThread.interrupt();
            mainThread = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    //Пример работы с интерфейсом из потока
    public void newShowToast(final Application application, final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(application, msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    //Пример работы с интерфейсом из потока
    public void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }


    private Notification prepareNotification(int icon) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Channel ID");
        builder.setSmallIcon(icon);
        builder.setContentTitle(null);
        builder.setContentText(null);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("Channel ID", "Name channel", NotificationManager.IMPORTANCE_DEFAULT));
        }

        return builder.build();

    }

    @Override
    public void onWebAsyncTaskCompleted(String result) {
        newShowToast(getApplication(),result);
    }
}