package com.infinitestack.shotanywhere;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * NotifyUtil.java
 * Description :
 * <p>
 * Created by MixtureDD on 2017/6/21 15:52.
 * Copyright © 2017 MixtureDD. All rights reserved.
 */

public class NotifyUtil {

    public static void notifyShot(Context context, Intent intent, int id, Notification.Action... actions) {
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        ActionNotifyBuilder builder =  new ActionNotifyBuilder(context, resultPendingIntent)
                .createBuilder();
        for (Notification.Action action : actions){
            builder.addAction(action);
        }
        builder.show(id);
    }

}
