package com.example.jacob.blankbookandroidclient;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentDialogFragment extends DialogFragment {
    @BindView(R.id.content)
    EditText content;
    @BindView(R.id.accept_comment)
    Button accept;
    @BindView(R.id.cancel_comment)
    Button cancel;

    private OnCommentDialogResultListener onResult;

    public void setOnResult(OnCommentDialogResultListener onResult) {
        this.onResult = onResult;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.create_comment_dialog, container);
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

    public interface OnCommentDialogResultListener {
        void onAccept(String content);
        void onCancel();
    }
}
