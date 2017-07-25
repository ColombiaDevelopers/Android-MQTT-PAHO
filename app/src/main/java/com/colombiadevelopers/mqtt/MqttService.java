package com.colombiadevelopers.mqtt;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MqttService extends Service implements MqttCallback {
    private String TAG = "mqtt";

    public String data;

    private boolean isRunning  = false;

    MqttAsyncClient sampleClient;
    String broker;
    String clientId;
    int QoS;
    String topic;
    MqttConnectOptions connOpts;
    IMqttToken token;
    MemoryPersistence persistense;


    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate called");

        isRunning = true;

        broker = "ssl://mqtt.colombiadevelopers.com:8883";
        clientId = MqttAsyncClient.generateClientId();
        QoS = 1;
        topic = "test";
        persistense = new MemoryPersistence();

        try {

            sampleClient = new MqttAsyncClient(broker, clientId, persistense);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            connOpts.setUserName("blog_coldev");
            String password = "@MQTT2586";
            connOpts.setPassword(password.toCharArray());

            token = sampleClient.connect(connOpts);
            token.waitForCompletion(3500);
            sampleClient.setCallback(this);
            token = sampleClient.subscribe(topic, QoS);
            token.waitForCompletion(5000);
        }
        catch (MqttException e) {

            Log.i(TAG, "reason " + e.getReasonCode());
            Log.i(TAG, "msg " + e.getMessage());
            Log.i(TAG, "loc " + e.getLocalizedMessage());
            Log.i(TAG, "cause " + e.getCause());
            Log.i(TAG, "excep " + e);
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("my-event2"));
        sendMessage("servicio a toda");
        return super.onStartCommand(intent, flags, startId);//Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind done");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Log.i(TAG, "se callo");
        try {
            sampleClient = new MqttAsyncClient(broker, clientId, persistense);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName("blog_coldev");
            String password = "@MQTT2586";
            connOpts.setPassword(password.toCharArray());

            token = sampleClient.connect(connOpts);
            token.waitForCompletion(3500);
            sampleClient.setCallback(this);
            token = sampleClient.subscribe(topic, QoS);
            token.waitForCompletion(5000);
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressLint("NewApi")
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        this.handleMessage (s, new String(mqttMessage.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            sendMessage(message);
        }
    };

    // Methods used by the binding client components
    public void sendMessage(String msj) {

        MqttMessage message = new MqttMessage(msj.getBytes());
        message.setQos(QoS);

        try {

            token = sampleClient.publish(topic, message);
            token.waitForCompletion(1000);
        }
        catch (MqttException e) {

            Log.i(TAG, "reason " + e.getReasonCode());
            Log.i(TAG, "msg " + e.getMessage());
            Log.i(TAG, "loc " + e.getLocalizedMessage());
            Log.i(TAG, "cause " + e.getCause());
            Log.i(TAG, "excep " + e);
            e.printStackTrace();
        }
    }

    public void handleMessage(String topic, String message) {


        Intent intent = new Intent("my-event");
        intent.putExtra("topic", topic);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        ActivityManager.RunningAppProcessInfo myProcess = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(myProcess);
        boolean isInBackground = myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

        if(isInBackground){
            showNotification(message);
        }
        Log.i("app", "Estado = > " + String.valueOf(isInBackground));

    }

    private void showNotification(String msj) {

        int icono = R.mipmap.ic_launcher;

        NotificationCompat.Builder builder;
        builder =new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(icono)
                .setContentTitle(topic)
                .setContentText(msj)
                .setVibrate(new long[] {100, 250, 100, 500})
                .setAutoCancel(true);

        builder.setDefaults(Notification.DEFAULT_ALL);

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
