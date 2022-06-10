package com.unirc.tesi.marcoventura.contacttracing.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.unirc.tesi.marcoventura.contacttracing.MainActivity;
import com.unirc.tesi.marcoventura.contacttracing.R;
import com.unirc.tesi.marcoventura.contacttracing.service.ForegroundServiceHelper;

import static com.unirc.tesi.marcoventura.contacttracing.service.ForegroundServiceHelper.CHANNEL_ID;
import static com.unirc.tesi.marcoventura.contacttracing.service.ForegroundServiceHelper.EXTRA_NOTIFICATION;

public class NotificationHelper {

    public static Notification getNotification(Context context){
        Intent intent = new Intent(context, ForegroundServiceHelper.class);
        intent.putExtra(EXTRA_NOTIFICATION, true);

        PendingIntent servicePendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),0);

        String text_notification = "Service Active";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Contact Tracing")
                .setContentText(text_notification)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setTicker(text_notification)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(CHANNEL_ID);

        return builder.build();
    }

}
