/*
 *
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.rozdoum.socialcomponents.managers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ValueEventListener;
import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnTaskCompleteListener;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.utils.LogUtil;

public class CommentManager extends FirebaseListenersManager {

    private static final String Attach = CommentManager.class.getSimpleName();
    private static CommentManager instance;

    private Context context;

    public static CommentManager getInstance(Context context) {
        if (instance == null) {
            instance = new CommentManager(context);
        }

        return instance;
    }

    private CommentManager(Context context) {
        this.context = context;
    }

    public void Comment_update(String commentText, String postId, OnTaskCompleteListener onTaskCompleteListener) {
        ApplicationHelper.to_DB_helper().make_comment(commentText, postId, onTaskCompleteListener);
    }

    public void counter_comment_dec(String postId, OnTaskCompleteListener onTaskCompleteListener) {
        ApplicationHelper.to_DB_helper().counter_comment_dec(postId, onTaskCompleteListener);
    }

    public void Receive_comment(Context activityContext, String postId, OnDataChangedListener<Comment> onDataChangedListener) {
        ValueEventListener valueEventListener = ApplicationHelper.to_DB_helper().Receive_comment(postId, onDataChangedListener);
        to_addinglistener(activityContext, valueEventListener);
    }

    public void canRemComment(String commentId, final String postId, final OnTaskCompleteListener onTaskCompleteListener) {
        final DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();

        databaseHelper.canRemComment(commentId, postId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                counter_comment_dec(postId, onTaskCompleteListener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
                LogUtil.to_Log_Error(Attach, "canRemComment()", e);
            }
        });
    }

    public void canUpdtComment(String commentId, String commentText, String postId, OnTaskCompleteListener onTaskCompleteListener) {
        ApplicationHelper.to_DB_helper().canUpdtComment(commentId, commentText, postId, onTaskCompleteListener);
    }
}
