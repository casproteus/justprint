package com.just.print.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.just.print.R;
import com.just.print.app.BaseActivity;
import com.just.print.app.EventBus;
import com.just.print.ui.fragment.ConfigMainFragment;

import java.util.HashMap;
import java.util.Map;

public class ConfigActivity extends BaseActivity implements EventBus.EventHandler {
    public static final String CHANGE_PAGE = "CHANGE_PAGE";
    Map<Class<? extends Fragment>, Fragment> mFragment = new HashMap<Class<? extends Fragment>, Fragment>();

    {
        mFragment.put(ConfigMainFragment.class, new ConfigMainFragment());
    }

    Fragment getFragment(Class<? extends Fragment> clz) {
        Fragment f = mFragment.get(clz);
        if (f == null) {
            try {
                f = clz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (f != null)
                mFragment.put(clz, f);
        }
        return f;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sys_config_activity);
        register(CHANGE_PAGE, this);
        getSupportFragmentManager().beginTransaction().add(R.id.content, getFragment(ConfigMainFragment.class), ConfigMainFragment.class.getName())
                .commit();
    }

    @Override
    public void handleEvent(String eventName, Object... argument) {
        if (CHANGE_PAGE.equals(eventName) && argument.length > 0) {
            Class<? extends Fragment> clz = (Class<? extends Fragment>) argument[0];
            getSupportFragmentManager().beginTransaction().replace(R.id.content, getFragment(clz), clz.getName())
                    .addToBackStack(clz.getName()).commit();
        }
    }

}
