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

package com.rozdoum.socialcomponents.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.managers.DatabaseHelper;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.GoogleApiHelper;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.LogoutHelper;
import com.rozdoum.socialcomponents.utils.PreferencesUtil;

import java.util.Arrays;

public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String Attach = LoginActivity.class.getSimpleName();
    private static final int Google_signin = 9001;

    private FirebaseAuth Authorization;
    private FirebaseAuth.AuthStateListener AuthorizationListener;
    private GoogleApiClient GoogleApi;

    private CallbackManager Call_back_manager;
    private String ProfilePicture_Url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Configure Google Sign In
        GoogleApi = GoogleApiHelper.Google_clientcreation(this);
        findViewById(R.id.googleSignInButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignIn();
            }
        });

        // Configure firebase auth
        Authorization = FirebaseAuth.getInstance();

        if (Authorization.getCurrentUser() != null) {
            LogoutHelper.signOut(GoogleApi, this);
        }

        AuthorizationListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Profile is signed in
                    LogUtil.to_Log_debug(Attach, "onAuthStateChanged:signed_in:" + user.getUid());
                    Check_Profile_Existence(user.getUid());
                } else {
                    // Profile is signed out
                    LogUtil.to_Log_debug(Attach, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Configure Facebook  Sign In
        Call_back_manager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(Call_back_manager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                LogUtil.to_Log_debug(Attach, "facebook:onSuccess:" + loginResult);
                ProfilePicture_Url = String.format(getString(R.string.facebook_large_image_url_pattern),
                        loginResult.getAccessToken().getUserId());
                facebookToken_handling(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                LogUtil.to_Log_debug(Attach, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                LogUtil.to_Log_Error(Attach, "facebook:onError", error);
                display_snackBar(error.getMessage());
            }
        });

        findViewById(R.id.facebookSignInButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FacebookSignIn();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Authorization.addAuthStateListener(AuthorizationListener);

        if (GoogleApi != null) {
            GoogleApi.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (AuthorizationListener != null) {
            Authorization.removeAuthStateListener(AuthorizationListener);
        }

        if (GoogleApi != null && GoogleApi.isConnected()) {
            GoogleApi.stopAutoManage(this);
            GoogleApi.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Call_back_manager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Google_signin) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                progress_display();
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                ProfilePicture_Url = String.format(getString(R.string.google_large_image_url_pattern),
                        account.getPhotoUrl(), Constants.Profile.SizeOfMaxAvatar);
                Google_firebaseAuth(account);
            } else {
                LogUtil.to_Log_debug(Attach, "Google_signin failed :" + result);
                // Google Sign In failed, update UI appropriately
                hide_progress();
            }
        }
    }

    private void Check_Profile_Existence(final String userId) {
        ProfileManager.getInstance(this).isProfileExist(userId, new OnObjectExistListener<Profile>() {
            @Override
            public void onDataChanged(boolean exist) {
                if (!exist) {
                    Profile_Creation();
                } else {
                    PreferencesUtil.to_setprofile(LoginActivity.this, true);
                    DatabaseHelper.getInstance(LoginActivity.this.getApplicationContext())
                            .Inc_registration_token(FirebaseInstanceId.getInstance().getToken(), userId);
                }
                hide_progress();
                finish();
            }
        });
    }

    private void Profile_Creation() {
        Intent intent = new Intent(LoginActivity.this, CreateProfileActivity.class);
        intent.putExtra(CreateProfileActivity.Image_key, ProfilePicture_Url);
        startActivity(intent);
    }

    private void facebookToken_handling(AccessToken token) {
        LogUtil.to_Log_debug(Attach, "facebookToken_handling:" + token);
        progress_display();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Authorization.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        LogUtil.to_Log_debug(Attach, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            AuthorizationError_handling(task);
                        }
                    }
                });
    }

    private void AuthorizationError_handling(Task<AuthResult> task) {
        Exception exception = task.getException();
        LogUtil.to_Log_Error(Attach, "signInWithCredential", exception);

        if (exception != null) {
            Warning_dialog(exception.getMessage());
        } else {
            display_snackBar(R.string.error_authentication);
        }

        hide_progress();
    }

    private void Google_firebaseAuth(GoogleSignInAccount acct) {
        LogUtil.to_Log_debug(Attach, "Google_firebaseAuth:" + acct.getId());
        progress_display();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Authorization.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        LogUtil.to_Log_debug(Attach, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            AuthorizationError_handling(task);
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        LogUtil.to_Log_debug(Attach, "onConnectionFailed:" + connectionResult);
        display_snackBar(R.string.error_google_play_services);
        hide_progress();
    }

    private void GoogleSignIn() {
        if (has_connection()) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(GoogleApi);
            startActivityForResult(signInIntent, Google_signin);
        } else {
            display_snackBar(R.string.internet_connection_failed);
        }
    }

    private void FacebookSignIn() {
        if (has_connection()) {
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
        } else {
            display_snackBar(R.string.internet_connection_failed);
        }
    }
}

