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

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.PostsByUserAdapter;
import com.rozdoum.socialcomponents.enums.PostStatus;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.LogoutHelper;

public class ProfileActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String Attach = ProfileActivity.class.getSimpleName();
    public static final int Make_Profile = 22;
    public static final String user_key = "ProfileActivity.user_key";

    // UI references.
    private TextView NameLabel;
    private ImageView ViewImage;
    private RecyclerView ViewRecycler;
    private ProgressBar progressBar;
    private TextView CountPosts;
    private TextView Posts_textView;
    private ProgressBar Progress_ofPosts;

    private FirebaseAuth Authorization;
    private GoogleApiClient GoogleApi;
    private String currentUserId;
    private String UserID;

    private PostsByUserAdapter postsAdapter;
    private SwipeRefreshLayout Swipe_refresher;
    private TextView LikesLabel_counter;
    private ProfileManager ManageProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        UserID = getIntent().getStringExtra(user_key);

        Authorization = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        // Set up the login form.
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ViewImage = (ImageView) findViewById(R.id.ViewImage);
        NameLabel = (TextView) findViewById(R.id.NameLabel);
        CountPosts = (TextView) findViewById(R.id.CountPosts);
        LikesLabel_counter = (TextView) findViewById(R.id.LikesLabel_counter);
        Posts_textView = (TextView) findViewById(R.id.Posts_textView);
        Progress_ofPosts = (ProgressBar) findViewById(R.id.Progress_ofPosts);

        Swipe_refresher = (SwipeRefreshLayout) findViewById(R.id.Swipe_refresher);
        Swipe_refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doActionRefresh();
            }
        });

        Posts_loader();
        supportPostponeEnterTransition();
    }

    @Override
    public void onStart() {
        super.onStart();
        ProfileLoader();

        if (GoogleApi != null) {
            GoogleApi.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ManageProfile.to_endlisteners(this);

        if (GoogleApi != null && GoogleApi.isConnected()) {
            GoogleApi.stopAutoManage(this);
            GoogleApi.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CreatePostActivity.New_Post:
                    postsAdapter.loadPosts();
                    display_snackBar(R.string.message_post_was_created);
                    setResult(RESULT_OK);
                    break;

                case PostDetailsActivity.Post_update:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.PostStatusKey);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            postsAdapter.removeSelectedPost();

                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            postsAdapter.UpdatePost_selected();
                        }
                    }
                    break;
            }
        }
    }

    private void doActionRefresh() {
        postsAdapter.loadPosts();
    }

    private void Posts_loader() {
        if (ViewRecycler == null) {

            ViewRecycler = (RecyclerView) findViewById(R.id.recycler_view);
            postsAdapter = new PostsByUserAdapter(this, UserID);
            postsAdapter.setCallBack(new PostsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    PostManager.getInstance(ProfileActivity.this).having_exitingpostvalue(post.getId(), new OnObjectExistListener<Post>() {
                        @Override
                        public void onDataChanged(boolean exist) {
                            if (exist) {
                                show_postActivity(post, view);
                            } else {
                                display_snackBar(R.string.error_post_was_removed);
                            }
                        }
                    });
                }

                @Override
                public void onPostsListChanged(int postsCount) {
                    String postsLabel = getResources().getQuantityString(R.plurals.posts_counter_format, postsCount, postsCount);
                    CountPosts.setText(buildCounterSpannable(postsLabel, postsCount));

                    LikesLabel_counter.setVisibility(View.VISIBLE);
                    CountPosts.setVisibility(View.VISIBLE);

                    if (postsCount > 0) {
                        Posts_textView.setVisibility(View.VISIBLE);
                    }

                    Swipe_refresher.setRefreshing(false);
                    hidePostsLoader();
                }

                @Override
                public void onPostLoadingCanceled() {
                    Swipe_refresher.setRefreshing(false);
                    hidePostsLoader();
                }
            });

            ViewRecycler.setLayoutManager(new LinearLayoutManager(this));
            ((SimpleItemAnimator) ViewRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
            ViewRecycler.setAdapter(postsAdapter);
            postsAdapter.loadPosts();
        }
    }

    private Spannable buildCounterSpannable(String label, int value) {
        SpannableStringBuilder contentString = new SpannableStringBuilder();
        contentString.append(String.valueOf(value));
        contentString.append("\n");
        int start = contentString.length();
        contentString.append(label);
        contentString.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Second_Light), start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    private void show_postActivity(Post post, View v) {
        Intent intent = new Intent(ProfileActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.PostId_Key, post.getId());
        intent.putExtra(PostDetailsActivity.AnimationAuthorKey, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View ViewImage = v.findViewById(R.id.ImagePostLabel);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(ProfileActivity.this,
                            new android.util.Pair<>(ViewImage, getString(R.string.post_image_transition_name))
                    );
            startActivityForResult(intent, PostDetailsActivity.Post_update, options.toBundle());
        } else {
            startActivityForResult(intent, PostDetailsActivity.Post_update);
        }
    }

    private void ProfileLoader() {
        ManageProfile = ProfileManager.getInstance(this);
        ManageProfile.retrieve_profileValue(ProfileActivity.this, UserID, createOnProfileChangedListener());
    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void ChangeIn_Object(Profile obj) {
                fill_fields(obj);
            }
        };
    }

    //FILL USER PROFILE INFO
    private void fill_fields(Profile profile) {
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
                                Postponed_transition(ViewImage);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                Postponed_transition(ViewImage);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(ViewImage);
            } else {
                progressBar.setVisibility(View.GONE);
                ViewImage.setImageResource(R.drawable.ic_stub);
            }

            int likesCount = (int) profile.getLikesCount();
            String likesLabel = getResources().getQuantityString(R.plurals.likes_counter_format, likesCount, likesCount);
            LikesLabel_counter.setText(buildCounterSpannable(likesLabel, likesCount));
        }
    }

    private void hidePostsLoader() {
        if (Progress_ofPosts.getVisibility() != View.GONE) {
            Progress_ofPosts.setVisibility(View.GONE);
        }
    }

    private void Postponed_transition(final ImageView ViewImage) {
        ViewImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ViewImage.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    private void Activity_main() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void beginProfileEditing() {
        if (has_connection()) {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        } else {
            display_snackBar(R.string.internet_connection_failed);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtil.to_Log_debug(Attach, "onConnectionFailed:" + connectionResult);
    }

    private void Post_activityCreation() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.New_Post);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (UserID.equals(currentUserId)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.profile_menu, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.editProfile:
                beginProfileEditing();
                return true;
            case R.id.signOut:
                LogoutHelper.signOut(GoogleApi, this);
                Activity_main();
                return true;
            case R.id.createPost:
                if (has_connection()) {
                    Post_activityCreation();
                } else {
                    display_snackBar(R.string.internet_connection_failed);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
