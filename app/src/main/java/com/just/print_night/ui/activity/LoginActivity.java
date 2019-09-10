package com.just.print_night.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.just.print_night.R;
import com.just.print_night.app.BaseActivity;
import com.just.print_night.ui.fragment.LoginFragment;

public class LoginActivity extends BaseActivity {

    static final private String tag = "LOGIN";

    Fragment fragment = new LoginFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        beginTransaction().add(R.id.content, fragment, fragment.getClass().getName()).commit();
    }


}
