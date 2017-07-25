package com.colombiadevelopers.mqtt;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.chat);
        textView.setText("Inicio");

        if (!isMyServiceRunning()){
            startService(new Intent(this, MqttService.class));
            Log.d("mqtt", "Service started");
        } else {
            Log.d("mqtt", "Service already running");
        }

    }

    private boolean isMyServiceRunning() {
        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (MqttService.class.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String topic = intent.getStringExtra("topic");
            String message = intent.getStringExtra("message");
            Log.i("mqtt", "Got message: " + message);
            handleMessage(topic, message);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("my-event"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.msj);
        String message = editText.getText().toString();
        Intent intent2 = new Intent ("my-event2");
        intent2.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);
        editText.setText("");
    }

    public void handleMessage (String topic, String Message) {
        TextView textView = (TextView) findViewById(R.id.chat);
        textView.setText(textView.getText() + "\n" + Message);
    }
}
