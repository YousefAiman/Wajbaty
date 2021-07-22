package com.developers.wajbaty.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.BadgeUtil;
import com.developers.wajbaty.Utils.CloudMessagingNotificationsSender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private String currentUID;
    private NotificationManager notificationManager;
    private int notificationNum;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){

            FirebaseFirestore.getInstance().collection("Users").document(
                    FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .update("cloudMessagingToken",s);

        }

    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("ttt","Firebase Messaging serivice created");

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("ttt","message reciceived");
        if(currentUID == null){
            currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        if (notificationManager == null) {
            notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
        }

        try {
            sendNotification(remoteMessage);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void sendNotification(RemoteMessage remoteMessage) throws ExecutionException, InterruptedException {

        final CloudMessagingNotificationsSender.Data data = new CloudMessagingNotificationsSender.Data(remoteMessage.getData());

        String type = String.valueOf(data.getType());
        createChannel(type);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,type)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(data.getTitle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(data.getBody())
                .setAutoCancel(true);


        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        if (data.getImageUrl()!=null) {
            builder.setLargeIcon(
                    Glide.with(this)
                            .asBitmap()
                            .apply(new RequestOptions().override(100, 100))
                            .centerCrop()
                            .load(data.getImageUrl())
                            .submit()
                            .get());
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            builder.setLargeIcon(bitmap);
        }


        builder.setGroup(type);


        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(notificationNum, builder.build());

        notificationNum++;

        if (Build.VERSION.SDK_INT < 26) {
            BadgeUtil.incrementBadgeNum(this);
        }


    }

    public void createChannel(String channelId) {
        if (Build.VERSION.SDK_INT >= 26) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                Log.d("ttt", "didn't find: " + channelId);
                Log.d("ttt", "creating notificaiton channel");
                NotificationChannel channel = new NotificationChannel(channelId, channelId + " channel", NotificationManager.IMPORTANCE_HIGH);
                channel.setShowBadge(true);
                channel.setDescription("notifications");
                channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
