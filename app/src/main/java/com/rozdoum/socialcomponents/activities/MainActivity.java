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

package com.rozdoum.socialcomponents.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.PostsAdapter;
import com.rozdoum.socialcomponents.enums.PostStatus;
import com.rozdoum.socialcomponents.enums.ProfileStatus;
import com.rozdoum.socialcomponents.managers.DatabaseHelper;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.AnimationUtils;

public class MainActivity extends BaseActivity {
    private static final String Attach = MainActivity.class.getSimpleName();

    private PostsAdapter postsAdapter;
    private RecyclerView ViewRecycler;
    private FloatingActionButton floatingActionButton;

    private ProfileManager ManageProfile;
    private PostManager postManager;
    private int counter;
    private TextView New_post_counter;
    private PostManager.to_havewatcher_forpost to_trackPost;
    private boolean Animation_counter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ManageProfile = ProfileManager.getInstance(this);
        postManager = PostManager.getInstance(this);
        View_content();

        to_trackPost = new PostManager.to_havewatcher_forpost() {
            @Override
            public void onPostCounterChanged(int newValue) {
                post_countUpdate();
            }
        };

        postManager.to_setcounterfor_post(to_trackPost);

//        Add_likes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        post_countUpdate();
    }

    private void Add_likes() {
        DatabaseHelper.getInstance(this).Add_newLike(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                counter++;
                display_snackBar("You have " + counter + " new likes");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProfileActivity.Make_Profile:
                    post_list_refresher();
                    break;
                case CreatePostActivity.New_Post:
                    post_list_refresher();
                    display_flaotbutton(R.string.message_post_was_created);
                    break;

                case PostDetailsActivity.Post_update:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.PostStatusKey);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            postsAdapter.removeSelectedPost();
                            display_flaotbutton(R.string.message_post_was_removed);
                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            postsAdapter.UpdatePost_selected();
                        }
                    }
                    break;
            }
        }
    }

    private void post_list_refresher() {
        postsAdapter.showPage_one();
        if (postsAdapter.getItemCount() > 0) {
            ViewRecycler.scrollToPosition(0);
        }
    }

    private void View_content() {
        if (ViewRecycler == null) {
            floatingActionButton = (FloatingActionButton) findViewById(R.id.addNewPostFab);

            if (floatingActionButton != null) {
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (has_connection()) {
                            Post_clickAddition();
                        } else {
                            display_flaotbutton(R.string.internet_connection_failed);
                        }
                    }
                });
            }

            New_post_counter = (TextView) findViewById(R.id.New_post_counter);
            New_post_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    post_list_refresher();
                }
            });

            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            SwipeRefreshLayout Swipe_refresher = (SwipeRefreshLayout) findViewById(R.id.Swipe_refresher);
            ViewRecycler = (RecyclerView) findViewById(R.id.recycler_view);
            postsAdapter = new PostsAdapter(this, Swipe_refresher);
            postsAdapter.setCallback(new PostsAdapter.Callback() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    PostManager.getInstance(MainActivity.this).having_exitingpostvalue(post.getId(), new OnObjectExistListener<Post>() {
                        @Override
                        public void onDataChanged(boolean exist) {
                            if (exist) {
                                show_postActivity(post, view);
                            } else {
                                display_flaotbutton(R.string.error_post_was_removed);
                            }
                        }
                    });
                }

                @Override
                public void onListLoadingFinished() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onAuthorClick(String authorId, View view) {
                    display_profileActivity(authorId, view);
                }

                @Override
                public void has_cancelled(String message) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });

            ViewRecycler.setLayoutManager(new LinearLayoutManager(this));
            ((SimpleItemAnimator) ViewRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
            ViewRecycler.setAdapter(postsAdapter);
            postsAdapter.showPage_one();
            post_countUpdate();

            ViewRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView ViewRecycler, int dx, int dy) {
                    toHide_counter();
                    super.onScrolled(ViewRecycler, dx, dy);
                }
            });
        }
    }

    private void toHide_counter() {
        if (!Animation_counter && New_post_counter.getVisibility() == View.VISIBLE) {
            Animation_counter = true;
            AlphaAnimation alphaAnimation = AnimationUtils.to_notshowAlpha_view(New_post_counter);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Animation_counter = false;
                    New_post_counter.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            alphaAnimation.start();
        }
    }

    private void todisplay_counter() {
        AnimationUtils.to_ViewScaleAndVisibility(New_post_counter);
    }

    private void show_postActivity(Post post, View v) {
        Intent intent = new Intent(MainActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.PostId_Key, post.getId());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View ViewImage = v.findViewById(R.id.ImagePostLabel);
            View AuthImgLabel = v.findViewById(R.id.AuthImgLabel);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(MainActivity.this,
                            new android.util.Pair<>(ViewImage, getString(R.string.post_image_transition_name)),
                            new android.util.Pair<>(AuthImgLabel, getString(R.string.post_author_image_transition_name))
                    );
            startActivityForResult(intent, PostDetailsActivity.Post_update, options.toBundle());
        } else {
            startActivityForResult(intent, PostDetailsActivity.Post_update);
        }
    }

    public void display_flaotbutton(int messageId) {
        display_snackBar(floatingActionButton, messageId);
    }

    private void Post_clickAddition() {
        ProfileStatus profileStatus = ManageProfile.to_Profile_check();

        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            Post_activityCreation();
        } else {
            Authorize(profileStatus);
        }
    }

    private void Post_activityCreation() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.New_Post);
    }

    private void display_profileActivity(String userId) {
        display_profileActivity(userId, null);
    }

    private void display_profileActivity(String userId, View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.user_key, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            View AuthImgLabel = view.findViewById(R.id.AuthImgLabel);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(MainActivity.this,
                            new android.util.Pair<>(AuthImgLabel, getString(R.string.post_author_image_transition_name)));
            startActivityForResult(intent, ProfileActivity.Make_Profile, options.toBundle());
        } else {
            startActivityForResult(intent, ProfileActivity.Make_Profile);
        }
    }

    private void post_countUpdate() {
        Handler mainHandler = new Handler(this.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                int newPostsQuantity = postManager.to_getcounterfor_post();

                if (New_post_counter != null) {
                    if (newPostsQuantity > 0) {
                        todisplay_counter();

                        String counterFormat = getResources().getQuantityString(R.plurals.new_posts_counter_format, newPostsQuantity, newPostsQuantity);
                        New_post_counter.setText(String.format(counterFormat, newPostsQuantity));
                    } else {
                        toHide_counter();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.profile:
                ProfileStatus profileStatus = ManageProfile.to_Profile_check();

                if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    display_profileActivity(userId);
                } else {
                    Authorize(profileStatus);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
