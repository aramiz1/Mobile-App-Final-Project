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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.ValidationUtil;

public class CreatePostActivity extends PickImageActivity implements OnPostCreatedListener {
    private static final String Attach = CreatePostActivity.class.getSimpleName();
    public static final int New_Post = 11;

    protected ImageView ViewImage;
    protected ProgressBar progressBar;
    protected EditText edit_title;
    protected EditText Edit_description;

    protected PostManager postManager;
    protected boolean creatingPost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post_activity);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        postManager = PostManager.getInstance(CreatePostActivity.this);

        edit_title = (EditText) findViewById(R.id.edit_title);
        Edit_description = (EditText) findViewById(R.id.Edit_description);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ViewImage = (ImageView) findViewById(R.id.ViewImage);

        ViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(v);
            }
        });

        edit_title.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (edit_title.hasFocus() && edit_title.getError() != null) {
                    edit_title.setError(null);
                    return true;
                }
                return false;
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
        Send_ImagetoView();
    }

    protected void Post_create() {
        // Reset errors.
        edit_title.setError(null);
        Edit_description.setError(null);

        String title = edit_title.getText().toString().trim();
        String description = Edit_description.getText().toString().trim();

        View focusView = null;
        boolean cancel = false;

        if (TextUtils.isEmpty(description)) {
            Edit_description.setError(getString(R.string.warning_empty_description));
            focusView = Edit_description;
            cancel = true;
        }

        if (TextUtils.isEmpty(title)) {
            edit_title.setError(getString(R.string.warning_empty_title));
            focusView = edit_title;
            cancel = true;
        } else if (!ValidationUtil.check_TitleofPost(title)) {
            edit_title.setError(getString(R.string.error_post_title_length));
            focusView = edit_title;
            cancel = true;
        }

        if (!(this instanceof EditPostActivity) && imageUri == null) {
            Warning_dialog(R.string.warning_empty_image);
            focusView = ViewImage;
            cancel = true;
        }

        if (!cancel) {
            creatingPost = true;
            hideKeyboard();
            savePost(title, description);
        } else if (focusView != null) {
            focusView.requestFocus();
        }
    }

    protected void savePost(String title, String description) {
        progress_display(R.string.message_creating_post);
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        postManager.createOrUpdatePostWithImage(imageUri, CreatePostActivity.this, post);
    }

    @Override
    public void saved_post(boolean success) {
        hide_progress();

        if (success) {
            setResult(RESULT_OK);
            CreatePostActivity.this.finish();
            LogUtil.to_Log_debug(Attach, "Post was created");
        } else {
            creatingPost = false;
            display_snackBar(R.string.error_fail_create_post);
            LogUtil.to_Log_debug(Attach, "Failed to create a post");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.post:
                if (!creatingPost) {
                    if (has_connection()) {
                        Post_create();
                    } else {
                        display_snackBar(R.string.internet_connection_failed);
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
