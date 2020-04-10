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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.enums.ProfileStatus;
import com.rozdoum.socialcomponents.enums.UploadImagePrefix;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.managers.listeners.OnProfileCreatedListener;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.PreferencesUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kristina on 10/28/16.
 */

public class ProfileManager extends FirebaseListenersManager {

    private static final String Attach = ProfileManager.class.getSimpleName();
    private static ProfileManager instance;

    private Context context;
    private DatabaseHelper databaseHelper;
    private Map<Context, List<ValueEventListener>> activeListeners = new HashMap<>();


    public static ProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager(context);
        }

        return instance;
    }

    private ProfileManager(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.to_DB_helper();
    }

    public Profile buildProfile(FirebaseUser firebaseUser, String Avatar_Url) {
        Profile profile = new Profile(firebaseUser.getUid());
        profile.setEmail(firebaseUser.getEmail());
        profile.setUsername(firebaseUser.getDisplayName());
        profile.setPhotoUrl(Avatar_Url != null ? Avatar_Url : firebaseUser.getPhotoUrl().toString());
        return profile;
    }

    public void isProfileExist(String id, final OnObjectExistListener<Profile> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child("profiles").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void createOrUpdateProfile(Profile profile, OnProfileCreatedListener onProfileCreatedListener) {
        createOrUpdateProfile(profile, null, onProfileCreatedListener);
    }

    public void createOrUpdateProfile(final Profile profile, Uri imageUri, final OnProfileCreatedListener onProfileCreatedListener) {
        if (imageUri == null) {
            databaseHelper.createOrUpdateProfile(profile, onProfileCreatedListener);
            return;
        }

        String Header_Img = ImageUtil.to_genTitleOf_Img(UploadImagePrefix.PROFILE, profile.getId());
        UploadTask uploadTask = databaseHelper.load_image(imageUri, Header_Img);

        if (uploadTask != null) {
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult().getDownloadUrl();
                        LogUtil.to_Log_debug(Attach, "successful upload image, image url: " + String.valueOf(downloadUrl));

                        profile.setPhotoUrl(downloadUrl.toString());
                        databaseHelper.createOrUpdateProfile(profile, onProfileCreatedListener);

                    } else {
                        onProfileCreatedListener.onProfileCreated(false);
                        LogUtil.to_Log_debug(Attach, "fail to upload image");
                    }

                }
            });
        } else {
            onProfileCreatedListener.onProfileCreated(false);
            LogUtil.to_Log_debug(Attach, "fail to upload image");
        }
    }

    public void retrieve_profileValue(Context activityContext, String id, final OnObjectChangedListener<Profile> listener) {
        ValueEventListener valueEventListener = databaseHelper.retrieve_profile(id, listener);
        to_addinglistener(activityContext, valueEventListener);
    }

    public void retrieve_profilevalue(String id, final OnObjectChangedListener<Profile> listener) {
        databaseHelper.retrieve_profilevalue(id, listener);
    }

    public ProfileStatus to_Profile_check() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return ProfileStatus.NOT_AUTHORIZED;
        } else if (!PreferencesUtil.Check_created_profile(context)){
            return ProfileStatus.NO_PROFILE;
        } else {
            return ProfileStatus.PROFILE_CREATED;
        }
    }
}
