package com.just.print.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.just.print.db.bean.Mark;
import com.just.print.util.ToastUtil;

import java.util.List;

/**
 * Created by wangx on 2016/11/1.
 */
abstract public class BaseFragment extends Fragment {
    protected abstract int getLayoutId();

    @Override
    public Context getContext() {
        return getActivity().getBaseContext();
    }

    View mRoot;

    EventBus mEventBus;

    public interface onChoiceMarks {
        void onChoiceMarks(List<Mark> list);
    }

    public EventBus getEventBus() {
        if (mEventBus == null)
            if (getActivity() instanceof EventBus) {
                mEventBus = (EventBus) getActivity();
            } else {
                mEventBus = new EventBus.EventBusImpl();
            }
        return mEventBus;
    }


    protected View findViewById(int id) {
        return mRoot.findViewById(id);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (mRoot == null)
            mRoot = inflater.inflate(getLayoutId(), container, false);
        else {
            if (mRoot != null && mRoot.getParent() != null) {
                ViewGroup v = (ViewGroup) mRoot.getParent();
                v.removeView(mRoot);
            }
            //container.removeView(mRoot);
        }
        return mRoot;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreated(savedInstanceState);
    }

    public void showToast(String text) {
        ToastUtil.showToast(getContext(), text);
    }

    public abstract void onCreated(Bundle savedInstanceState);
}
