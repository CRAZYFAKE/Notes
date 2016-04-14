package com.lguipeng.notes.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lguipeng.notes.R;
import com.lguipeng.notes.ui.MainActivity;
import com.lguipeng.notes.ui.SettingActivity;
import com.lguipeng.notes.view.BetterFab;
import com.pnikosis.materialishprogress.ProgressWheel;

import butterknife.Bind;

/**
 * 笔记列表 Fragment
 * Created by Yao.YiXiang on 2016/4/13.
 */
public class NotesFragment extends PreferenceFragment {

    @Bind(R.id.refresher)
    SwipeRefreshLayout refreshLayout;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.fab)
    BetterFab fab;
    @Bind(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;
    private MainActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() != null && getActivity() instanceof SettingActivity){
            this.activity = (MainActivity)getActivity();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}