package com.lguipeng.notes.adpater;

import android.content.Context;

import com.lguipeng.notes.R;

import java.util.List;

public class DrawerListAdapter extends SimpleListAdapter{

    public DrawerListAdapter(Context mContext, List<String> list) {
        super(mContext, list);
    }

    @Override
    protected int getLayout() {
        return R.layout.item_drawer_list_layout;
    }
}
