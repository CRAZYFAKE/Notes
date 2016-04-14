package com.lguipeng.notes.mvp.views.impl;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;

import com.evernote.edam.type.Notebook;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.views.View;

import java.util.List;

public interface MainView extends View {
    void initToolbar();
    void initDrawerView(List<String> list);
    void setToolbarTitle(String title);
    void showProgressWheel(boolean visible);
    void switchNoteTypePage(List<SNote> notes);
    void addNote(SNote note);
    void updateNote(SNote note);
    void removeNote(SNote note);
    void scrollRecyclerViewToTop();
    void setDrawerItemChecked(int position);
    boolean isDrawerOpen();
    void closeDrawer();
    void openOrCloseDrawer();
    void setMenuGravity(int gravity);
    void showFab(boolean visible);
    void stopRefresh();
    void startRefresh();
    boolean isRefreshing();
    void enableSwipeRefreshLayout(boolean enable);
    void setLayoutManager(RecyclerView.LayoutManager manager);
    void initRecyclerView(List<SNote> notes);//初始化界面，并且加载笔记
    void showTrashPopupMenu(android.view.View view, SNote note);//显示“回收站”界面笔记的PopupMenu
    void showNormalPopupMenu(android.view.View view, SNote note);//显示“笔记”界面笔记的PopupMenu
    void showDeleteForeverDialog(SNote note);//提示用户是否永久删除笔记
    void showSnackbar(@StringRes int message);//顯示SnackBar
    void showGoBindEverNoteSnackbar(@StringRes int message, @StringRes int action);
    void initNoteBookList(List<Notebook> list);
    void getNoteBookList();
    void moveTaskToBack();
    void reCreate();
}
