/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.rozdoum.socialcomponents.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.activities.MainActivity;
import com.rozdoum.socialcomponents.activities.PostDetailsActivity;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.utils.LogUtil;

/**
 * Created by alexey on 13.04.17.
 */


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String Attach = MyFirebaseMessagingService.class.getSimpleName();

    private static int have_IdFor_Notification = 0;

    private static final String KeyOf_PostID = "postId";
    private static final String KeyOf_AuthorID = "authorId";
    private static final String KeyOf_Action = "actionType";
    private static final String KeyOf_Title = "title";
    private static final String KeyOf_Body = "body";
    private static final String KeyFor_Icon = "icon";
    private static final String New_actionLike = "new_like";
    private static final String New_CommentAction = "new_comment";
    private static final String New_postAction = "new_post";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null && remoteMessage.getData().get(KeyOf_Action) != null) {
            to_handle_messages(remoteMessage);
        } else {
            LogUtil.to_Log_Error(Attach, "onMessageReceived()", new RuntimeException("FCM remoteMessage doesn't contains Action Type"));
        }
    }

    private void to_handle_messages(RemoteMessage remoteMessage) {
        String receivedActionType = remoteMessage.getData().get(KeyOf_Action);
        LogUtil.to_Log_debug(Attach, "Message Notification Action Type: " + receivedActionType);

        switch (receivedActionType) {
            case New_actionLike:
                to_mergeComment_OrLikes(remoteMessage);
                break;
            case New_CommentAction:
                to_mergeComment_OrLikes(remoteMessage);
                break;
            case New_postAction:
                to_createNewPost(remoteMessage);
                break;
        }
    }

    private void to_createNewPost(RemoteMessage remoteMessage) {
        String postAuthorId = remoteMessage.getData().get(KeyOf_AuthorID);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Send notification for each users except author of post.
        if (firebaseUser != null && !firebaseUser.getUid().equals(postAuthorId)) {
            PostManager.getInstance(this.getApplicationContext()).to_inccounterfor_Post();
        }
    }

    private void to_mergeComment_OrLikes(RemoteMessage remoteMessage) {
        String notificationTitle = remoteMessage.getData().get(KeyOf_Title);
        String notificationBody = remoteMessage.getData().get(KeyOf_Body);
        String notificationImageUrl = remoteMessage.getData().get(KeyFor_Icon);
        String postId = remoteMessage.getData().get(KeyOf_PostID);

        Intent backIntent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.PostId_Key, postId);

        Bitmap bitmap = to_receiveBitMap_fromURL(notificationImageUrl);

        to_forwardNotifcations(notificationTitle, notificationBody, bitmap, intent, backIntent);

        LogUtil.to_Log_debug(Attach, "Message Notification Body: " + remoteMessage.getData().get(KeyOf_Body));
    }

    public Bitmap to_receiveBitMap_fromURL(String imageUrl) {
        try {
            return Glide.with(this)
                    .load(imageUrl)
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(Constants.PushNotification.SizeOfLargestIcon, Constants.PushNotification.SizeOfLargestIcon)
                    .get();

        } catch (Exception e) {
            LogUtil.to_Log_Error(Attach, "getBitmapfromUrl", e);
            return null;
        }
    }

    private void to_forwardNotifcations(String notificationTitle, String notificationBody, Bitmap bitmap, Intent intent) {
        to_forwardNotifcations(notificationTitle, notificationBody, bitmap, intent, null);
    }

    private void to_forwardNotifcations(String notificationTitle, String notificationBody, Bitmap bitmap, Intent intent, Intent backIntent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent;

        if(backIntent != null) {
            backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent[] intents = new Intent[] {backIntent, intent};
            pendingIntent = PendingIntent.getActivities(this, have_IdFor_Notification++, intents, PendingIntent.FLAG_ONE_SHOT);
        } else {
            pendingIntent = PendingIntent.getActivity(this, have_IdFor_Notification++, intent, PendingIntent.FLAG_ONE_SHOT);
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setAutoCancel(true)   //Automatically delete the notification
                .setSmallIcon(R.drawable.ic_push_notification_small) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setLargeIcon(bitmap)
                .setSound(defaultSoundUri);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(have_IdFor_Notification++ /* ID of notification */, notificationBuilder.build());
    }
}
