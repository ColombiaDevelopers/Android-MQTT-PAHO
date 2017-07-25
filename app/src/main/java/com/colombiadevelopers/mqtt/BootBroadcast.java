package com.colombiadevelopers.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Julio on 23/06/2017.
 */

public class BootBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent serviceIntent = new Intent("com.neverdesk.mqtt.MqttService");
            context.startService(serviceIntent);
        }
    }

}
