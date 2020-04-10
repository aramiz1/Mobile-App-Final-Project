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

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.CommentsAdapter;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.dialogs.EditCommentDialog;
import com.rozdoum.socialcomponents.enums.PostStatus;
import com.rozdoum.socialcomponents.enums.ProfileStatus;
import com.rozdoum.socialcomponents.listeners.CustomTransitionListener;
import com.rozdoum.socialcomponents.managers.CommentManager;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.managers.listeners.OnPostChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnTaskCompleteListener;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.FormatterUtil;
import com.rozdoum.socialcomponents.utils.Utils;

import java.util.List;

public class PostDetailsActivity extends BaseActivity implements EditCommentDialog.Dialog_commentback {

    public static final String PostId_Key = "PostDetailsActivity.PostId_Key";
    public static final String AnimationAuthorKey = "PostDetailsActivity.AnimationAuthorKey";
    private static final int LoadingCommentTime = 30000;
    public static final int Post_update = 1;
    public static final String PostStatusKey = "PostDetailsActivity.PostStatusKey";

    private EditText editComment;
    @Nullable
    private Post post;
    private ScrollView scrollView;
    private ViewGroup likesContainer;
    private ImageView likesImageView;
    private TextView Commenttag;
    private TextView CountLikes;
    private TextView Countcomment;
    private TextView CounterView;
    private TextView AuthorLabel;
    private TextView DateLabel;
    private ImageView AuthImgLabel;
    private ProgressBar progressBar;
    private ImageView ImagePostLabel;
    private TextView TitleLabel;
    private TextView Edit_description;
    private ProgressBar commentsProgressBar;
    private RecyclerView commentsRecyclerView;
    private TextView CommentWarningLabel;

    private boolean TryLoadingComments = false;

    private MenuItem canComplain;
    private MenuItem canEdit;
    private MenuItem canDelete;

    private String postId;

    private PostManager postManager;
    private CommentManager ManageComment;
    private ProfileManager ManageProfile;
    private LikeController ControlLikes;
    private boolean RemovePost = false;
    private boolean ExistingPost;
    private boolean ProgressofAuthAnimation = false;

    private boolean AnimationRequired;
    private CommentsAdapter commentsAdapter;
    private ActionMode mActionMode;
    private boolean didTransitionfinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ManageProfile = ProfileManager.getInstance(this);
        postManager = PostManager.getInstance(this);
        ManageComment = CommentManager.getInstance(this);

        AnimationRequired = getIntent().getBooleanExtra(AnimationAuthorKey, false);
        postId = getIntent().getStringExtra(PostId_Key);

        inc_count_forWatchers();

        TitleLabel = (TextView) findViewById(R.id.TitleLabel);
        Edit_description = (TextView) findViewById(R.id.Edit_description);
        ImagePostLabel = (ImageView) findViewById(R.id.ImagePostLabel);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        commentsRecyclerView = (RecyclerView) findViewById(R.id.commentsRecyclerView);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        Commenttag = (TextView) findViewById(R.id.Commenttag);
        editComment = (EditText) findViewById(R.id.editComment);
        likesContainer = (ViewGroup) findViewById(R.id.likesContainer);
        likesImageView = (ImageView) findViewById(R.id.likesImageView);
        AuthImgLabel = (ImageView) findViewById(R.id.AuthImgLabel);
        AuthorLabel = (TextView) findViewById(R.id.AuthorLabel);
        CountLikes = (TextView) findViewById(R.id.CountLikes);
        Countcomment = (TextView) findViewById(R.id.Countcomment);
        CounterView = (TextView) findViewById(R.id.CounterView);
        DateLabel = (TextView) findViewById(R.id.DateLabel);
        commentsProgressBar = (ProgressBar) findViewById(R.id.commentsProgressBar);
        CommentWarningLabel = (TextView) findViewById(R.id.CommentWarningLabel);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && AnimationRequired) {
            AuthImgLabel.setScaleX(0);
            AuthImgLabel.setScaleY(0);

            // Add a listener to get noticed when the transition ends to animate the fab button
            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    //disable execution for exit transition
                    if (!didTransitionfinish) {
                        didTransitionfinish = true;
                        com.rozdoum.socialcomponents.utils.AnimationUtils.to_ViewScale(AuthImgLabel)
                                .setListener(authorAnimatorListener)
                                .start();
                    }
                }
            });
        }

        final Button sendButton = (Button) findViewById(R.id.sendButton);

        View_recycler();

        postManager.receive_posts(this, postId, createOnPostChangeListener());


        ImagePostLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Image_screen();
            }
        });

        editComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (has_connection()) {
                    ProfileStatus profileStatus = ProfileManager.getInstance(PostDetailsActivity.this).to_Profile_check();

                    if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                        Comment_send();
                    } else {
                        Authorize(profileStatus);
                    }
                } else {
                    display_snackBar(R.string.internet_connection_failed);
                }
            }
        });

        Countcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveto_firstcomment();
            }
        });

        View.OnClickListener onAuthorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post != null) {
                    display_profileActivity(post.getAuthorId(), v);
                }
            }
        };

        AuthImgLabel.setOnClickListener(onAuthorClickListener);

        AuthorLabel.setOnClickListener(onAuthorClickListener);

        supportPostponeEnterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postManager.to_endlisteners(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyBoard();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && AnimationRequired) {
            if (!ProgressofAuthAnimation) {
                ViewPropertyAnimator hideAuthorAnimator = com.rozdoum.socialcomponents.utils.AnimationUtils.to_hideScale(AuthImgLabel);
                hideAuthorAnimator.setListener(authorAnimatorListener);
                hideAuthorAnimator.withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        PostDetailsActivity.super.onBackPressed();
                    }
                });
            }

        } else {
            super.onBackPressed();
        }
    }

    private void View_recycler() {
        commentsAdapter = new CommentsAdapter();
        commentsAdapter.setCallback(new CommentsAdapter.Callback() {
            @Override
            public void onLongItemClick(View view, int position) {
                Comment selectedComment = commentsAdapter.findIndex(position);
                beginaction_mode(selectedComment);
            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                display_profileActivity(authorId, view);
            }
        });
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.addItemDecoration(new DividerItemDecoration(commentsRecyclerView.getContext(),
                ((LinearLayoutManager) commentsRecyclerView.getLayoutManager()).getOrientation()));

        ManageComment.Receive_comment(this, postId, createOnCommentsChangedDataListener());
    }

    private void beginaction_mode(Comment selectedComment) {
        if (mActionMode != null) {
            return;
        }

        //check access to modify or remove post
        if (canEditComment(selectedComment.getAuthorId()) || canModify()) {
            mActionMode = startSupportActionMode(new ActionModeCallback(selectedComment));
        }
    }

    private OnPostChangedListener createOnPostChangeListener() {
        return new OnPostChangedListener() {
            @Override
            public void ChangeIn_Object(Post obj) {
                if (obj != null) {
                    post = obj;
                    loading_post();
                } else if (!RemovePost) {
                    ExistingPost = false;
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent.putExtra(PostStatusKey, PostStatus.REMOVED));
                    display_removedPost();
                }
            }

            @Override
            public void onError(String errorText) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailsActivity.this);
                builder.setMessage(errorText);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        };
    }

    private void loading_post() {
        ExistingPost = true;
        initLikes();
        FillFields();
        inc_counter();
        Like_buttonStatus();
        canUpdateMenu();
    }

    private void inc_count_forWatchers() {
        postManager.inc_count_forWatchers(postId);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent.putExtra(PostStatusKey, PostStatus.UPDATED));
    }

    private void display_removedPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailsActivity.this);
        builder.setMessage(R.string.error_post_was_removed);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void moveto_firstcomment() {
        if (post != null && post.getCommentsCount() > 0) {
            scrollView.smoothScrollTo(0, Commenttag.getTop());
        }
    }

    private void FillFields() {
        if (post != null) {
            TitleLabel.setText(post.getTitle());
            Edit_description.setText(post.getDescription());

            Image_details();
            AuthImg_loading();
        }
    }

    private void Image_details() {
        if (post == null) {
            return;
        }

        String imageUrl = post.getImagePath();
        int width = Utils.to_width_display(this);
        int height = (int) getResources().getDimension(R.dimen.post_detail_image_height);
        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Postponed_transition(ImagePostLabel);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Postponed_transition(ImagePostLabel);
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .crossFade()
                .into(ImagePostLabel);
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

    private void AuthImg_loading() {
        if (post != null && post.getAuthorId() != null) {
            ManageProfile.retrieve_profilevalue(post.getAuthorId(), createProfileChangeListener());
        }
    }

    private void inc_counter() {
        if (post == null) {
            return;
        }

        long Count_comment = post.getCommentsCount();
        Countcomment.setText(String.valueOf(Count_comment));
        Commenttag.setText(String.format(getString(R.string.label_comments), Count_comment));
        CountLikes.setText(String.valueOf(post.getLikesCount()));
        ControlLikes.Likeupdate_counter(false);

        CounterView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(this, post.getCreatedDate());
        DateLabel.setText(date);

        if (Count_comment == 0) {
            Commenttag.setVisibility(View.GONE);
            commentsProgressBar.setVisibility(View.GONE);
        } else if (Commenttag.getVisibility() != View.VISIBLE) {
            Commenttag.setVisibility(View.VISIBLE);
        }
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void ChangeIn_Object(Profile obj) {
                if (obj.getPhotoUrl() != null) {
                    Glide.with(PostDetailsActivity.this)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .crossFade()
                            .into(AuthImgLabel);
                }

                AuthorLabel.setText(obj.getUsername());
            }
        };
    }

    private OnDataChangedListener<Comment> createOnCommentsChangedDataListener() {
        TryLoadingComments = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (TryLoadingComments) {
                    commentsProgressBar.setVisibility(View.GONE);
                    CommentWarningLabel.setVisibility(View.VISIBLE);
                }
            }
        }, LoadingCommentTime);


        return new OnDataChangedListener<Comment>() {
            @Override
            public void ChangedIn_List(List<Comment> list) {
                TryLoadingComments = false;
                commentsProgressBar.setVisibility(View.GONE);
                commentsRecyclerView.setVisibility(View.VISIBLE);
                CommentWarningLabel.setVisibility(View.GONE);
                commentsAdapter.setList(list);
            }
        };
    }

    private void Image_screen() {
        if (post != null) {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra(ImageDetailActivity.Img_key, post.getImagePath());
            startActivity(intent);
        }
    }

    private void display_profileActivity(String userId, View view) {
        Intent intent = new Intent(PostDetailsActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.user_key, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(PostDetailsActivity.this,
                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                ControlLikes.initLike(exist);
            }
        };
    }

    private void Like_buttonStatus() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && post != null) {
            postManager.User_currentLike(this, post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private void initLikes() {
        ControlLikes = new LikeController(this, post, CountLikes, likesImageView, false);

        likesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ExistingPost) {
                    ControlLikes.handleLikeClickAction(PostDetailsActivity.this, post);
                }
            }
        });

        //long click for changing animation
        likesContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (ControlLikes.getLikeAnimationType() == LikeController.AnimationType.Animation_bounce) {
                    ControlLikes.setLikeAnimationType(LikeController.AnimationType.Animation_color);
                } else {
                    ControlLikes.setLikeAnimationType(LikeController.AnimationType.Animation_bounce);
                }

                Snackbar snackbar = Snackbar
                        .make(likesContainer, "Animation was changed", Snackbar.LENGTH_LONG);

                snackbar.show();
                return true;
            }
        });
    }

    private void Comment_send() {
        if (post == null) {
            return;
        }

        String commentText = editComment.getText().toString();

        if (commentText.length() > 0 && ExistingPost) {
            ManageComment.Comment_update(commentText, post.getId(), new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete(boolean success) {
                    if (success) {
                        moveto_firstcomment();
                    }
                }
            });
            editComment.setText(null);
            editComment.clearFocus();
            hideKeyBoard();
        }
    }

    private void hideKeyBoard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean canEditComment(String commentAuthorId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && commentAuthorId.equals(currentUser.getUid());
    }

    private boolean canModify() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && post != null && post.getAuthorId().equals(currentUser.getUid());
    }

    private void canUpdateMenu() {
        if (canEdit != null && canDelete != null && canModify()) {
            canEdit.setVisible(true);
            canDelete.setVisible(true);
        }

        if (canComplain != null && post != null && !post.isHasComplain()) {
            canComplain.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_details_menu, menu);
        canComplain = menu.findItem(R.id.complain_action);
        canEdit = menu.findItem(R.id.edit_post_action);
        canDelete = menu.findItem(R.id.delete_post_action);

        if (post != null) {
            canUpdateMenu();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!ExistingPost) {
            return super.onOptionsItemSelected(item);
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.complain_action:
                doComplainAction();
                return true;

            case R.id.edit_post_action:
                if (canModify()) {
                    canOpenActivity_Post();
                }
                return true;

            case R.id.delete_post_action:
                if (canModify()) {
                    isPostRemovable();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doComplainAction() {
        ProfileStatus profileStatus = ManageProfile.to_Profile_check();

        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            canOpenComplain();
        } else {
            Authorize(profileStatus);
        }
    }

    private void isPostRemovable() {
        if (has_connection()) {
            if (!RemovePost) {
                canOpenDialog();
            }
        } else {
            display_snackBar(R.string.internet_connection_failed);
        }
    }

    private void Post_removing() {
        postManager.Post_removing(post, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                if (success) {
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent.putExtra(PostStatusKey, PostStatus.REMOVED));
                    finish();
                } else {
                    RemovePost = false;
                    display_snackBar(R.string.error_fail_remove_post);
                }

                hide_progress();
            }
        });

        progress_display(R.string.removing);
        RemovePost = true;
    }

    private void canOpenActivity_Post() {
        if (has_connection()) {
            Intent intent = new Intent(PostDetailsActivity.this, EditPostActivity.class);
            intent.putExtra(EditPostActivity.POST_EXTRA_KEY, post);
            startActivityForResult(intent, EditPostActivity.EDIT_POST_REQUEST);
        } else {
            display_snackBar(R.string.internet_connection_failed);
        }
    }

    private void canOpenDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_deletion_post)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Post_removing();
                    }
                });

        builder.create().show();
    }

    private void canOpenComplain() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_complain)
                .setMessage(R.string.complain_text)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.add_complain, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        canComplain();
                    }
                });

        builder.create().show();
    }

    private void canComplain() {
        postManager.canComplain(post);
        canComplain.setVisible(false);
        display_snackBar(R.string.complain_sent);
    }

    private void canRemComment(String commentId, final ActionMode mode, final int position) {
        progress_display();
        ManageComment.canRemComment(commentId, postId, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                hide_progress();
                mode.finish(); // Action picked, so close the CAB
                display_snackBar(R.string.message_comment_was_removed);
            }
        });
    }

    private void openEditCommentDialog(Comment comment) {
        EditCommentDialog editCommentDialog = new EditCommentDialog();
        Bundle args = new Bundle();
        args.putString(EditCommentDialog.textkey, comment.getText());
        args.putString(EditCommentDialog.IdKey_comment, comment.getId());
        editCommentDialog.setArguments(args);
        editCommentDialog.show(getFragmentManager(), EditCommentDialog.Attach);
    }

    private void canUpdtComment(String newText, String commentId) {
        progress_display();
        ManageComment.canUpdtComment(commentId, newText, postId, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                hide_progress();
                display_snackBar(R.string.message_comment_was_edited);
            }
        });
    }

    @Override
    public void onCommentChanged(String newText, String commentId) {
        canUpdtComment(newText, commentId);
    }

    private class ActionModeCallback implements ActionMode.Callback {
        Comment selectedComment;
        int position;

        ActionModeCallback(Comment selectedComment) {
            this.selectedComment = selectedComment;
        }

        // Called when the action mode is created; beginaction_mode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.comment_context_menu, menu);

            menu.findItem(R.id.editMenuItem).setVisible(canEditComment(selectedComment.getAuthorId()));

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.editMenuItem:
                    openEditCommentDialog(selectedComment);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.deleteMenuItem:
                    canRemComment(selectedComment.getId(), mode, position);
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }
    Animator.AnimatorListener authorAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            ProgressofAuthAnimation = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            ProgressofAuthAnimation = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            ProgressofAuthAnimation = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

}
