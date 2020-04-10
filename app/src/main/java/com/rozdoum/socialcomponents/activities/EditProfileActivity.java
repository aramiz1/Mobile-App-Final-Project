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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnProfileCreatedListener;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.ValidationUtil;

public class EditProfileActivity extends PickImageActivity implements OnProfileCreatedListener {
    private static final String Attach = EditProfileActivity.class.getSimpleName();

    // UI references.
    private EditText NameLabel;
    private ImageView ViewImage;
    private ProgressBar progressBar;

    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up the login form.
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ViewImage = (ImageView) findViewById(R.id.ViewImage);
        NameLabel = (EditText) findViewById(R.id.NameLabel);

        progress_display();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ProfileManager.getInstance(this).retrieve_profilevalue(firebaseUser.getUid(), createOnProfileChangedListener());

        ViewImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(v);
            }
        });
    }

    @Override
    public ProgressBar progress_view() {
        return progressBar;
    }

    @Override
    public ImageView image_view() {
        return ViewImage;
    }

    @Override
    public void loadImage() {
        Image_crop();
    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void ChangeIn_Object(Profile obj) {
                profile = obj;
                fill_fields();
            }
        };
    }

    private void fill_fields() {
        if (profile != null) {
            NameLabel.setText(profile.getUsername());

            if (profile.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(profile.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .error(R.drawable.ic_stub)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(ViewImage);
            }
        }
        hide_progress();
        NameLabel.requestFocus();
    }

    private void Profile_creation() {

        // Reset errors.
        NameLabel.setError(null);

        // Store values at the time of the login attempt.
        String name = NameLabel.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            NameLabel.setError(getString(R.string.error_field_required));
            focusView = NameLabel;
            cancel = true;
        } else if (!ValidationUtil.check_Name(name)) {
            NameLabel.setError(getString(R.string.error_profile_name_length));
            focusView = NameLabel;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            progress_display();
            profile.setUsername(name);
            ProfileManager.getInstance(this).createOrUpdateProfile(profile, imageUri, this);
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        Image_crop_handling(requestCode, resultCode, data);
    }



    @Override
    public void onProfileCreated(boolean success) {
        hide_progress();

        if (success) {
            finish();
        } else {
            display_snackBar(R.string.error_fail_update_profile);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
                if (has_connection()) {
                    Profile_creation();
                } else {
                    display_snackBar(R.string.internet_connection_failed);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

