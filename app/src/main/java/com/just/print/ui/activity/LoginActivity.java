package com.just.print.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.just.print.R;
import com.just.print.app.BaseActivity;
import com.just.print.ui.fragment.LoginFragment;

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
