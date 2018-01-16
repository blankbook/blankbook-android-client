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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostDialogFragment extends DialogFragment {
    @BindView(R.id.title)
    EditText title;
    @BindView(R.id.body)
    EditText body;
    @BindView(R.id.accept_comment)
    Button accept;
    @BindView(R.id.cancel_comment)
    Button cancel;
    @BindView(R.id.group_selector)
    Spinner groupSelector;

    public static String GROUPS_TAG = "Groups";

    private OnPostDialogResultListener onResult;
    private List<String> possibleMemberGroups;

    public void setOnResult(OnPostDialogResultListener onResult) {
        this.onResult = onResult;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.create_post_dialog, container);
        ButterKnife.bind(this, view);

        possibleMemberGroups = getArguments().getStringArrayList(GROUPS_TAG);
        if (possibleMemberGroups == null || possibleMemberGroups.size() == 0) {
            dismiss();
            return view;
        }
        ArrayAdapter adapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_spinner_item, possibleMemberGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSelector.setAdapter(adapter);

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
        title.requestFocus();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void setupButtons() {
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onResult != null) {
                    onResult.onAccept(title.getText().toString(), body.getText().toString(), (String) groupSelector.getSelectedItem());
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onResult != null) { onResult.onCancel(); }
            }
        });
    }

    public interface OnPostDialogResultListener {
        void onAccept(String title, String body, String groupName);
        void onCancel();
    }
}
