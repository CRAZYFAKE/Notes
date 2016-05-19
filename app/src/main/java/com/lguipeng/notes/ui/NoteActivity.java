package com.lguipeng.notes.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lguipeng.notes.App;
import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.component.DaggerActivityComponent;
import com.lguipeng.notes.injector.module.ActivityModule;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.impl.NotePresenter;
import com.lguipeng.notes.mvp.views.impl.NoteView;
import com.lguipeng.notes.utils.DialogUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.Bind;

public class NoteActivity extends BaseActivity implements NoteView {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.label_edit_text)
    MaterialEditText labelEditText;
    @Bind(R.id.content_edit_text)
    MaterialEditText contentEditText;
    @Bind(R.id.opr_time_line_text)
    TextView oprTimeLineTextView;
    @Bind(R.id.frame_layout)
    FrameLayout mFramLayout;

    private MenuItem doneMenuItem;
    private int width;
    @Inject
    NotePresenter notePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresenter();
        getEditTextWidth();
        notePresenter.onCreate(savedInstanceState);
    }

    private void initializePresenter() {
        notePresenter.attachView(this);
        notePresenter.attachIntent(getIntent());
    }

    @Override
    protected void initializeDependencyInjector() {
        App app = (App) getApplication();
        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(app.getAppComponent())
                .build();
        mActivityComponent.inject(this);
    }

    @Override
    protected void onStop() {
        notePresenter.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        notePresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_note;
    }

    @Override
    protected void initToolbar() {
        super.initToolbar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        doneMenuItem = menu.getItem(0);
        notePresenter.onPrepareOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (notePresenter.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return notePresenter.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public void setToolbarTitle(int title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public void initViewOnEditMode(SNote note) {
        showKeyBoard();
        labelEditText.requestFocus();
        labelEditText.setText(note.getLabel());
        contentEditText.setText(note.getContent());
        labelEditText.setSelection(note.getLabel().length());
        contentEditText.setSelection(note.getContent().length());
        labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void initViewOnViewMode(SNote note) {
        hideKeyBoard();
        labelEditText.setText(note.getLabel());
        contentEditText.setText(note.getContent());
        labelEditText.setOnFocusChangeListener(notePresenter);
        contentEditText.setOnFocusChangeListener(notePresenter);
        labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void initViewOnCreateMode(SNote note) {
        labelEditText.requestFocus();
        //labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    public void showAttachImg(String content) {
        //匹配以"<img src='"开头，以"'/>"结尾的字符串
        Pattern p = Pattern.compile("<img src='(.*?)'/>");
        Matcher m = p.matcher(content);
        ArrayList<String> paths = new ArrayList<String>();
        while (m.find()) {
            paths.add(m.group(1));
        }
        if (paths.size() <= 0) {
            return;
        }
        //将所有匹配到的String保存到strs中
        for (String filepath : paths) {
            File file = new File(filepath);
            Bitmap pic = null;
            if (file.exists()) {
                pic = BitmapFactory.decodeFile(filepath);
                float scaleWidth = ((float) width) / pic.getWidth();
                Matrix mx = new Matrix();
                mx.setScale(scaleWidth, scaleWidth);
                pic = Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), mx, true);
                SpannableString ss = new SpannableString(filepath);
                ImageSpan imgSpan = new ImageSpan(this, pic);
                ss.setSpan(imgSpan, 0, filepath.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                Editable editText = contentEditText.getEditableText();
//                editText.replace();
            } else {
                return;
            }
        }
    }

    @Override
    public void setOperateTimeLineTextView(String text) {
        oprTimeLineTextView.setText(text);
    }

    @Override
    public void setDoneMenuItemVisible(boolean visible) {
        if (doneMenuItem != null) {
            doneMenuItem.setVisible(visible);
        }
    }

    @Override
    public boolean isDoneMenuItemVisible() {
        return doneMenuItem != null && doneMenuItem.isVisible();
    }

    @Override
    public boolean isDoneMenuItemNull() {
        return doneMenuItem == null;
    }

    @Override
    public String getLabelText() {
        return labelEditText.getText().toString();
    }

    @Override
    public String getContentText() {
        return contentEditText.getText().toString();
    }

    @Override
    public void showNotSaveNoteDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilder(this);
        builder.setTitle(R.string.not_save_note_leave_tip);
        builder.setPositiveButton(R.string.sure, notePresenter);
        builder.setNegativeButton(R.string.cancel, notePresenter);
        builder.show();
    }

    @Override
    public void hideKeyBoard() {
        hideKeyBoard(labelEditText);
    }

    @Override
    public void showKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyBoard(EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        }
    }

    /**
     * 通过路径获取系统图片
     */
    private Bitmap getBitmap(Uri uri) {
        Bitmap pic = null;
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        Display display = getWindowManager().getDefaultDisplay();
        int dw = display.getWidth();
        int dh = display.getHeight();
        try {
            pic = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri), null, op);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int wRatio = (int) Math.ceil(op.outWidth / (float) dw);
        int hRatio = (int) Math.ceil(op.outHeight / (float) dh);
        if (wRatio > 1 && hRatio > 1) {
            op.inSampleSize = wRatio + hRatio;
        }
        op.inJustDecodeBounds = false;
        try {
            pic = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri), null, op);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return pic;
    }

    private void getEditTextWidth() {
        ViewTreeObserver vto = contentEditText.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                contentEditText.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);
                width = contentEditText.getWidth();
            }
        });
    }
}