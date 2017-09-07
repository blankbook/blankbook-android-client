package com.example.jacob.blankbookandroidclient;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.managers.PublicGroupsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentDialogFragment extends DialogFragment {
    @BindView(R.id.content)
    EditText content;
    @BindView(R.id.accept_comment)
    Button accept;
    @BindView(R.id.cancel_comment)
    Button cancel;

    private PostActivity.OnCommentDialogResultListener onResult;

    public void setOnResult(PostActivity.OnCommentDialogResultListener onResult) {
        this.onResult = onResult;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.comment_dialog_fragment, container);
        ButterKnife.bind(this, view);

        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP | Gravity.LEFT);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        window.setAttributes(params);

        setupButtons();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        content.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void setupButtons() {
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onResult != null) { onResult.onAccept(content.getText().toString()); }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onResult != null) { onResult.onCancel(); }
            }
        });
    }
}
