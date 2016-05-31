package com.lguipeng.notes.mvp.views.impl;

import android.support.annotation.StringRes;

import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.views.View;

public interface NoteView extends View {
    void finishView();
    void setToolbarTitle(String title);
    void setToolbarTitle(@StringRes int title);
    void initViewOnEditMode(SNote note);
    void initViewOnViewMode(SNote note);
    void initViewOnCreateMode(SNote note);
    void setOperateTimeLineTextView(String text);
    void setDoneMenuItemVisible(boolean visible);
    boolean isDoneMenuItemVisible();
    boolean isDoneMenuItemNull();
    void startAttachmentActivityForResult(SNote note);
    String getLabelText();
    String getContentText();
    boolean showKnifeTools();
    void hideKeyBoard();
    void showKeyBoard();
    void showNotSaveNoteDialog();
}