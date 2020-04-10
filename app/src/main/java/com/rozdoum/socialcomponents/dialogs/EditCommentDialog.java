/*
 *
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
 *
 */

package com.rozdoum.socialcomponents.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.rozdoum.socialcomponents.R;

/**
 * Created by alexey on 12.05.17.
 */

public class EditCommentDialog extends DialogFragment {
    public static final String Attach = EditCommentDialog.class.getSimpleName();
    public static final String textkey = "EditCommentDialog.textkey";
    public static final String IdKey_comment = "EditCommentDialog.IdKey_comment";

    private Dialog_commentback callback;
    private String commentText;
    private String commentId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getActivity() instanceof Dialog_commentback) {
            callback = (Dialog_commentback) getActivity();
        } else {
            throw new RuntimeException(getActivity().getTitle() + " should implements Dialog_commentback");
        }

        commentText = (String) getArguments().get(textkey);
        commentId = (String) getArguments().get(IdKey_comment);

        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dialog_edit_comment, null);

        final EditText editCommentEditText = (EditText) view.findViewById(R.id.editCommentEditText);

        if (commentText != null) {
            editCommentEditText.setText(commentText);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.title_edit_comment)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_title_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newCommentText = editCommentEditText.getText().toString();

                        if (!newCommentText.equals(commentText) && callback != null) {
                            callback.onCommentChanged(newCommentText, commentId);
                        }

                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    public interface Dialog_commentback {
        void onCommentChanged(String newText, String commentId);
    }
}