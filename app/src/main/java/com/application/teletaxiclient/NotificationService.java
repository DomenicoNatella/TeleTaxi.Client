package com.application.teletaxiclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.restlet.data.Protocol;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;

import model.Prenotazione;

public class NotificationService extends Service {

    private static final int NOTIFICATION_ID = 5 ;
    private int wakeUpTime = 35000;
    private Timer mTimer;
    private int timeNotifica, tempoDiAttesa;
    private TimerTask timerTask;
    private Worker worker;
    private Handler mHandler = new Handler();
    private long currentTime;
    private String clientePreferences = "clientePref";


    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences(clientePreferences, Context.MODE_PRIVATE);
        timeNotifica = sharedPreferences.getInt("preferenzeNotifica", 5);
        tempoDiAttesa = Double.valueOf(intent.getDoubleExtra("tempoDiAttesa", 0)).intValue();
        currentTime = System.currentTimeMillis();
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(timerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post( worker = new Worker());
                mHandler.removeCallbacksAndMessages(worker);
            }
        }, 0, wakeUpTime);
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        if(mTimer != null) {
            mHandler.removeCallbacks(worker);
            timerTask.cancel();
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    class Worker implements Runnable{
        @Override
        public void run(){
            Log.e("error", ""+(currentTime/60000)+","+tempoDiAttesa+","+timeNotifica+","+(System.currentTimeMillis()/60000));
            if(((currentTime/60000) + tempoDiAttesa - timeNotifica) <= (System.currentTimeMillis()/60000) ){
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                builder.setContentTitle("Taxi in arrivo!")
                        .setAutoCancel(true)
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setContentText("Il taxi prenotato e' in arrivo alla posizione desiderata")
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setLights(Color.RED, 3000, 3000)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setSmallIcon(R.drawable.ic_local_taxi_black_48dp)
                        .setPriority(Notification.PRIORITY_HIGH);
                Intent intentNotify = new Intent(getApplicationContext(), ClientMainActivity.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                        NOTIFICATION_ID,
                        intentNotify,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                Notification notification = builder.build();
                notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
    }




}
