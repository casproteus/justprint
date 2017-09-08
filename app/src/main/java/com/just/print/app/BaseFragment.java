package com.just.print.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.just.print.db.bean.Mark;
import com.just.print.db.expand.DaoExpand;
import com.just.print.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void showMarksDialog(List<Mark> choiceItem, final onChoiceMarks choiceMarks) {

        if (choiceItem == null) {
            choiceItem = new ArrayList<Mark>();
        }

        final List<Mark> allMarks = DaoExpand.queryNotDeletedAll(Applic.app.getDaoMaster().newSession().getMarkDao());

        String[] markNames = new String[allMarks.size()];
        boolean[] selectionStatus = new boolean[allMarks.size()];
        final Map<String, Integer> sa = new HashMap<String, Integer>();
        for (int i = 0, s = allMarks.size(); i < s; i++) {
            markNames[i] = allMarks.get(i).getName();
            selectionStatus[i] = choiceItem.contains(allMarks.get(i));
            if(selectionStatus[i]) {
                sa.put(Integer.toString(i), i);
            }
        }

        new AlertDialog.Builder(this.getActivity()).setMultiChoiceItems(markNames, selectionStatus, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked)
                    sa.put(Integer.toString(which), which);
                else
                    sa.remove(Integer.toString(which));
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Mark> result = new ArrayList<Mark>();
                for (Integer i : sa.values()) {
                    result.add(allMarks.get(i));
                }
                choiceMarks.onChoiceMarks(result);

            }
        }).setNegativeButton("Cancel", null).show();
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
