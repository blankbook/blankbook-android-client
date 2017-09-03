package com.example.jacob.blankbookandroidclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jacob.blankbookandroidclient.animations.HeightAnimation;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.managers.LocalGroupsManger;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupCreationActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.name_error)
    TextView nameError;
    @BindView(R.id.password_protected)
    Switch passwordProtected;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.password_error)
    TextView passwordError;
    @BindView(R.id.password_confirmation_error)
    TextView passwordConfirmationError;
    @BindView(R.id.password_wrapper)
    LinearLayout passwordWrapper;
    @BindView(R.id.password_confirmation)
    EditText passwordConfirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);
        ButterKnife.bind(this);
        setupToolbar();
        setupPasswordAnimation();
        setupErrorDetection();
        name.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                save();
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.creation, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finishAndAnimate();
        return false;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.new_group));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
    }

    private void setupPasswordAnimation() {
        passwordWrapper.post(new Runnable() {
            @Override
            public void run() {
                final int initialHeight = passwordWrapper.getHeight();
                ViewGroup.LayoutParams params = passwordWrapper.getLayoutParams();
                params.height = 0;
                passwordWrapper.setLayoutParams(params);
                passwordProtected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            expandPasswordContainer(initialHeight);
                        } else {
                            collapsePasswordContainer();
                        }
                    }
                });
            }
        });
    }

    private void setupErrorDetection() {
        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                errorCheckName();
            }
        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                errorCheckPasswords();
            }
        });
        passwordConfirmation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                errorCheckPasswords();
            }
        });
    }

    private void errorCheckPasswords() {
        passwordConfirmationError.setText("");
        if (passwordProtected.isChecked()) {
            if (!password.getText().toString().equals(passwordConfirmation.getText().toString()) &&
                    !"".equals(passwordConfirmation.getText().toString())) {
                passwordConfirmationError.setText(getResources().getString(R.string.passwords_no_match_error));
            }
        }
    }

    private void errorCheckName() {
        nameError.setText("");
        if ("".equals(name.getText().toString())) {
            return;
        }
        String nameText = name.getText().toString();
        RetrofitClient.getInstance().getBlankBookAPI().getGroup(nameText)
                .enqueue(new Callback<Group>() {
                    @Override
                    public void onResponse(Call<Group> call, Response<Group> response) {
                        if (response.body() != null) {
                            nameError.setText(getResources().getString(R.string.name_taken_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<Group> call, Throwable t) {
                    }
                });
    }

    private void expandPasswordContainer(int targetHeight) {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        Animation expand = new HeightAnimation(passwordWrapper, targetHeight);
        expand.setDuration(shortAnimTime);
        passwordWrapper.startAnimation(expand);
    }

    private void collapsePasswordContainer() {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        Animation expand = new HeightAnimation(passwordWrapper, 0);
        expand.setDuration(shortAnimTime);
        passwordWrapper.startAnimation(expand);
    }

    private void save() {
        if (!password.getText().toString().equals(passwordConfirmation.getText().toString())) {
            passwordConfirmationError.setText(getResources().getString(R.string.passwords_no_match_error));
            return;
        }
        RetrofitClient.getInstance().getBlankBookAPI()
                .postGroup(new Group(name.getText().toString(), passwordProtected.isChecked()))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.code() == 200) {
                            onGroupCreation();
                        } else {
                            showGroupCreationError();
                            errorCheckName();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        showGroupCreationError();
                        errorCheckName();
                    }
                });
    }

    private void showGroupCreationError() {
        Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.error_creating_group),
                Toast.LENGTH_SHORT).show();
    }

    private void onGroupCreation() {
        String groupName = name.getText().toString();
        LocalGroupsManger.getInstance().addGroup(groupName);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.NEW_GROUP_NAME_TAG, groupName);
        setResult(Activity.RESULT_OK, resultIntent);
        finishAndAnimate();
    }

    private void finishAndAnimate() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            // give time for everything to redraw after the keyboard collapses
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    overridePendingTransition(R.anim.none, R.anim.fade_out);
                }
            }, 50L);
        } else {
            finish();
            overridePendingTransition(R.anim.none, R.anim.fade_out);
        }
    }
}
