/*
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
 */

package com.rozdoum.socialcomponents.managers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.enums.UploadImagePrefix;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.managers.listeners.OnPostChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnPostListChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnTaskCompleteListener;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.utils.LogUtil;

/**
 * Created by Kristina on 10/28/16.
 */

public class PostManager extends FirebaseListenersManager {

    private static final String Attach = PostManager.class.getSimpleName();
    private static PostManager instance;
    private int Count_newpost = 0;
    private to_havewatcher_forpost to_trackPost;

    private Context context;

    public static PostManager getInstance(Context context) {
        if (instance == null) {
            instance = new PostManager(context);
        }

        return instance;
    }

    private PostManager(Context context) {
        this.context = context;
    }

    public void createOrUpdatePost(Post post) {
        try {
            ApplicationHelper.to_DB_helper().createOrUpdatePost(post);
        } catch (Exception e) {
            Log.e(Attach, e.getMessage());
        }
    }

    public void receive_postssList(OnPostListChangedListener<Post> onDataChangedListener, long date) {
        ApplicationHelper.to_DB_helper().ListOfPosts(onDataChangedListener, date);
    }

    public void receive_postssListByUser(OnDataChangedListener<Post> onDataChangedListener, String userId) {
        ApplicationHelper.to_DB_helper().ListOfPostsByUser(onDataChangedListener, userId);
    }

    public void receive_posts(Context context, String postId, OnPostChangedListener onPostChangedListener) {
        ValueEventListener valueEventListener = ApplicationHelper.to_DB_helper().receive_posts(postId, onPostChangedListener);
        to_addinglistener(context, valueEventListener);
    }

    public void retrieve_single_postValue(String postId, OnPostChangedListener onPostChangedListener) {
        ApplicationHelper.to_DB_helper().retrieve_single_post(postId, onPostChangedListener);
    }

    public void createOrUpdatePostWithImage(Uri imageUri, final OnPostCreatedListener onPostCreatedListener, final Post post) {
        // Register observers to listen for when the download is done or if it fails
        DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();
        if (post.getId() == null) {
            post.setId(databaseHelper.for_postidgeneration());
        }

        final String Header_Img = ImageUtil.to_genTitleOf_Img(UploadImagePrefix.POST, post.getId());
        UploadTask uploadTask = databaseHelper.load_image(imageUri, Header_Img);

        if (uploadTask != null) {
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    onPostCreatedListener.saved_post(false);

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    LogUtil.to_Log_debug(Attach, "successful upload image, image url: " + String.valueOf(downloadUrl));

                    post.setImagePath(String.valueOf(downloadUrl));
                    post.setImageTitle(Header_Img);
                    createOrUpdatePost(post);

                    onPostCreatedListener.saved_post(true);
                }
            });
        }
    }

    public Task<Void> to_imageRemoval(String Header_Img) {
        final DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();
        return databaseHelper.to_imageRemoval(Header_Img);
    }

    public void Post_removing(final Post post, final OnTaskCompleteListener onTaskCompleteListener) {
        final DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();
        Task<Void> to_imageRemovalTask = to_imageRemoval(post.getImageTitle());

        to_imageRemovalTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                databaseHelper.Post_removing(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        onTaskCompleteListener.onTaskComplete(task.isSuccessful());
                        databaseHelper.PostRemoval_updateprofile(post);
                        LogUtil.to_Log_debug(Attach, "Post_removing(), is success: " + task.isSuccessful());
                    }
                });
                LogUtil.to_Log_debug(Attach, "to_imageRemoval(): success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                LogUtil.to_Log_Error(Attach, "to_imageRemoval()", exception);
                onTaskCompleteListener.onTaskComplete(false);
            }
        });
    }

    public void canComplain(Post post) {
        DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();
        databaseHelper.ableto_complain(post);
    }

    public void User_currentLike(Context activityContext, String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();
        ValueEventListener valueEventListener = databaseHelper.User_currentLike(postId, userId, onObjectExistListener);
        to_addinglistener(activityContext, valueEventListener);
    }

    public void User_currentLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();
        databaseHelper.User_currentLikeSingleValue(postId, userId, onObjectExistListener);
    }

    public void having_exitingpostvalue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();
        databaseHelper.having_exitingpostvalue(postId, onObjectExistListener);
    }

    public void inc_count_forWatchers(String postId) {
        DatabaseHelper databaseHelper = ApplicationHelper.to_DB_helper();
        databaseHelper.inc_count_forWatchers(postId);
    }

    public void to_inccounterfor_Post() {
        Count_newpost++;
        to_declarecounterfor_post();
    }

    public void to_clearcountfor_post() {
        Count_newpost = 0;
        to_declarecounterfor_post();
    }

    public int to_getcounterfor_post() {
        return Count_newpost;
    }

    public void to_setcounterfor_post(to_havewatcher_forpost to_trackPost) {
        this.to_trackPost = to_trackPost;
    }

    private void to_declarecounterfor_post() {
        if (to_trackPost != null) {
            to_trackPost.onPostCounterChanged(Count_newpost);
        }
    }

    public interface to_havewatcher_forpost {
        void onPostCounterChanged(int newValue);
    }
}
