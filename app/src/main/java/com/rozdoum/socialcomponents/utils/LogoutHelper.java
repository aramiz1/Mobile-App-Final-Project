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

package com.rozdoum.socialcomponents.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rozdoum.socialcomponents.managers.DatabaseHelper;

public class LogoutHelper {

    private static final String Attach = LogoutHelper.class.getSimpleName();
    private static ClearImageCacheAsyncTask to_clearcacheTask;

    public static void signOut(GoogleApiClient GoogleApi, FragmentActivity fragmentActivity) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseHelper.getInstance(fragmentActivity.getApplicationContext())
                    .rem_registration_token(FirebaseInstanceId.getInstance().getToken(), user.getUid());

            for (UserInfo profile : user.getProviderData()) {
                String providerId = profile.getProviderId();
                to_logout(providerId, GoogleApi, fragmentActivity);
            }
            to_logoutOf_Firebase(fragmentActivity.getApplicationContext());
        }

        if (to_clearcacheTask == null) {
            to_clearcacheTask = new ClearImageCacheAsyncTask(fragmentActivity.getApplicationContext());
            to_clearcacheTask.execute();
        }
    }

    private static void to_logout(String providerId, GoogleApiClient GoogleApi, FragmentActivity fragmentActivity) {
        switch (providerId) {
            case GoogleAuthProvider.PROVIDER_ID:
                to_logoutOf_Google(GoogleApi, fragmentActivity);
                break;

            case FacebookAuthProvider.PROVIDER_ID:
                to_logoutOf_Facebook(fragmentActivity.getApplicationContext());
                break;
        }
    }

    private static void to_logoutOf_Firebase(Context context) {
        FirebaseAuth.getInstance().signOut();
        PreferencesUtil.to_setprofile(context, false);
    }

    private static void to_logoutOf_Facebook(Context context) {
        FacebookSdk.sdkInitialize(context);
        LoginManager.getInstance().logOut();
    }

    private static void to_logoutOf_Google(GoogleApiClient GoogleApi, FragmentActivity fragmentActivity) {
        if (GoogleApi == null) {
            GoogleApi = GoogleApiHelper.Google_clientcreation(fragmentActivity);
        }

        if (!GoogleApi.isConnected()) {
            GoogleApi.connect();
        }

        final GoogleApiClient finalMGoogleApiClient = GoogleApi;
        GoogleApi.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (finalMGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(finalMGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                LogUtil.to_Log_debug(Attach, "User Logged out from Google");
                            } else {
                                LogUtil.to_Log_debug(Attach, "Error Logged out from Google");
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                LogUtil.to_Log_debug(Attach, "Google API Client Connection Suspended");
            }
        });
    }

    private static class ClearImageCacheAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context context;

        public ClearImageCacheAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Glide.get(context.getApplicationContext()).clearDiskCache();
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            super.onPostExecute(o);
            to_clearcacheTask = null;
            Glide.get(context.getApplicationContext()).clearMemory();
        }
    }
}
