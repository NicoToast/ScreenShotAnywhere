package com.infinitestack.shotanywhere;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * ActionNotifyBuilder.java
 * Description :
 * <p>
 * Created by MixtureDD on 2017/6/21 20:31.
 * Copyright © 2017 MixtureDD. All rights reserved.
 */

public class ActionNotifyBuilder extends Notification.Builder {

    private NotificationManager mManager;
    private PendingIntent contentPending;
    private int iconRes;
    private String contentTitle;
    private String contentText;
    private String ticker;

    public ActionNotifyBuilder(Context context, int iconRes, PendingIntent contentPending, String contentTitle, String contentText, String ticker) {
        super(context);
        this.iconRes = iconRes;
        this.contentTitle = contentTitle;
        this.contentText = contentText;
        this.ticker = ticker;
        this.contentPending = contentPending;
    }

    public ActionNotifyBuilder(Context context, PendingIntent contentPending) {
        super(context);
        mManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        this.iconRes = R.mipmap.ic_launcher_round;
        this.contentTitle = context.getString(R.string.notify_title_shot);
        this.contentText = context.getString(R.string.notify_text_shot);
        this.ticker = context.getString(R.string.notify_ticker_shot);
        this.contentPending = contentPending;
    }

    public ActionNotifyBuilder createBuilder() {
        this.setSmallIcon(iconRes)
                .setContentTitle(contentTitle)
                .setTicker(ticker)
                .setContentText(contentText)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setContentIntent(contentPending)
                .setFullScreenIntent(contentPending, false);
        return this;
    }

    public ActionNotifyBuilder addActionEvent(Notification.Action action) {
        this.addAction(action);
        return this;
    }

    public void show(int id) {
        Notification baseNF = this.build();
        //发出状态栏通知
        mManager.notify(id, baseNF);
    }
}
