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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnPostChangedListener;
import com.rozdoum.socialcomponents.model.Post;

public class EditPostActivity extends CreatePostActivity {
    private static final String Attach = EditPostActivity.class.getSimpleName();
    public static final String POST_EXTRA_KEY = "EditPostActivity.POST_EXTRA_KEY";
    public static final int EDIT_POST_REQUEST = 33;

    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        post = (Post) getIntent().getSerializableExtra(POST_EXTRA_KEY);
        progress_display();
        fill_fields();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Check_changedPost();
    }

    @Override
    protected void onStop() {
        super.onStop();
        postManager.to_endlisteners(this);
    }

    @Override
    public void saved_post(boolean success) {
        hide_progress();
        creatingPost = false;

        if (success) {
            setResult(RESULT_OK);
            finish();
        } else {
            display_snackBar(R.string.error_fail_update_post);
        }
    }

    @Override
    protected void savePost(final String title, final String description) {
        display_savingpost(title, description);
    }

    private void Check_changedPost() {
        PostManager.getInstance(this).receive_posts(this, post.getId(), new OnPostChangedListener() {
            @Override
            public void ChangeIn_Object(Post obj) {
                if (obj == null) {
                    Warning_dialog(getResources().getString(R.string.error_post_was_removed));
                } else {
                    Check_PostCounter(obj);
                }
            }

            @Override
            public void onError(String errorText) {
                Warning_dialog(errorText);
            }

            private void Warning_dialog(String message) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Activity_Main();
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        });
    }

    private void Check_PostCounter(Post updatedPost) {
        if (post.getLikesCount() != updatedPost.getLikesCount()) {
            post.setLikesCount(updatedPost.getLikesCount());
        }

        if (post.getCommentsCount() != updatedPost.getCommentsCount()) {
            post.setCommentsCount(updatedPost.getCommentsCount());
        }

        if (post.getWatchersCount() != updatedPost.getWatchersCount()) {
            post.setWatchersCount(updatedPost.getWatchersCount());
        }

        if (post.isHasComplain() != updatedPost.isHasComplain()) {
            post.setHasComplain(updatedPost.isHasComplain());
        }
    }

    private void Activity_Main() {
        Intent intent = new Intent(EditPostActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void display_savingpost(String title, String description) {
        progress_display(R.string.message_saving);
        post.setTitle(title);
        post.setDescription(description);

        if (imageUri != null) {
            postManager.createOrUpdatePostWithImage(imageUri, EditPostActivity.this, post);
        } else {
            postManager.createOrUpdatePost(post);
            saved_post(true);
        }
    }

    private void fill_fields() {
        edit_title.setText(post.getTitle());
        Edit_description.setText(post.getDescription());
        Image_details();
        hide_progress();
    }

    private void Image_details() {
        Glide.with(this)
                .load(post.getImagePath())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade()
                .centerCrop()
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
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
