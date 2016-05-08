package com.lguipeng.notes.mvp.presenters.impl;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.EditText;

import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.ContextLifeCycle;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.Presenter;
import com.lguipeng.notes.mvp.views.View;
import com.lguipeng.notes.mvp.views.impl.NoteView;
import com.lguipeng.notes.ui.NoteActivity;
import com.lguipeng.notes.utils.AttachmentUtils;
import com.lguipeng.notes.utils.TimeUtils;

import net.tsz.afinal.FinalDb;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class NotePresenter implements Presenter, android.view.View.OnFocusChangeListener,
        DialogInterface.OnClickListener, TextWatcher {
    private NoteView view;
    private final Context mContext;
    private FinalDb mFinalDb;
    private SNote note;
    private int operateMode = 0;
    private AttachmentUtils mAttachmentUtils;
    private MainPresenter.NotifyEvent<SNote> event;
    private SNote.NoteType mCurrentNoteTypePage = SNote.NoteType.getDefault();
    public final static String OPERATE_NOTE_TYPE_KEY = "OPERATE_NOTE_TYPE_KEY";
    public final static int VIEW_NOTE_MODE = 0x00;
    public final static int EDIT_NOTE_MODE = 0x01;
    public final static int CREATE_NOTE_MODE = 0x02;


    public final static int REQ_SELECT_IMAGE = 100;
    public final static int REQ_SELECT_AUDIO = 101;
    public final static int REQ_SELECT_FILE = 102;

    @Inject
    public NotePresenter(@ContextLifeCycle("Activity") Context mContext, FinalDb mFinalDb, AttachmentUtils mAttachmentUtils) {
        this.mContext = mContext;
        this.mFinalDb = mFinalDb;
        this.mAttachmentUtils = mAttachmentUtils;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onResume() {

    }

    public void onPrepareOptionsMenu() {
        view.setDoneMenuItemVisible(false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                saveNote();
                return true;
            case android.R.id.home:
                view.hideKeyBoard();
                if (view.isDoneMenuItemVisible()) {
                    view.showNotSaveNoteDialog();
                    return true;
                }
                view.finishView();
            default:
                return false;
        }
    }

    public void attachItem() {
        SNote note = new SNote();
        note.setType(mCurrentNoteTypePage);
        startNoteActivity(NotePresenter.CREATE_NOTE_MODE, note);
    }

    /**
     * 添加图片
     *
     * @param activity
     */
    public void attachImage(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, REQ_SELECT_IMAGE);
    }

    /**
     * 添加音频
     *
     * @param activity
     */
    public void attachAudio(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        //intent.setType(“audio/*”); //选择音频
        //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
        //intent.setType(“video/*;image/*”);//同时选择视频和图片
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, REQ_SELECT_FILE);
    }

    /**
     * 添加文件
     *
     * @param activity
     */
    public void attachFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, REQ_SELECT_FILE);
    }

    private void startNoteActivity(int type, SNote value) {
        Intent intent = new Intent(mContext, NoteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(NotePresenter.OPERATE_NOTE_TYPE_KEY, type);
        EventBus.getDefault().postSticky(value);
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {
        view.hideKeyBoard();

    }

    @Override
    public void onDestroy() {
        if (event != null) {
            EventBus.getDefault().post(event);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void attachView(View v) {
        this.view = (NoteView) v;
    }

    public void attachIntent(Intent intent) {
        parseIntent(intent);
    }

    public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            view.hideKeyBoard();
            if (view.isDoneMenuItemVisible()) {
                view.showNotSaveNoteDialog();
                return true;
            }
        }
        return false;
    }

    private void parseIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            operateMode = intent.getExtras().getInt(OPERATE_NOTE_TYPE_KEY, 0);
        }
    }

    public void onEventMainThread(SNote note) {
        this.note = note;
        initToolbar();
        initEditText();
        initTextView();
    }

    private void initToolbar() {
        view.setToolbarTitle(R.string.view_note);
        switch (operateMode) {
            case CREATE_NOTE_MODE:
                view.setToolbarTitle(R.string.new_note);
                break;
            case EDIT_NOTE_MODE:
                view.setToolbarTitle(R.string.edit_note);
                break;
            case VIEW_NOTE_MODE:
                view.setToolbarTitle(R.string.view_note);
                break;
            default:
                break;
        }
    }

    private void initEditText() {
        switch (operateMode) {
            case EDIT_NOTE_MODE:
                view.initViewOnEditMode(note);
                break;
            case VIEW_NOTE_MODE:
                view.initViewOnViewMode(note);
                break;
            default:
                view.initViewOnCreateMode(note);
                break;
        }
    }

    private void initTextView() {
        view.setOperateTimeLineTextView(getOprTimeLineText(note));
    }

    public void attachImage(EditText editText, int width, Bitmap pic, Intent data) {
        mAttachmentUtils.attachImage(editText, width, pic, data);
    }

    public void attachImage(EditText editText, int width, String filepath) {
        File file = new File(filepath);
        Bitmap pic = null;
        if (file.exists()) {
            pic = BitmapFactory.decodeFile(filepath);
//            mAttachmentUtils.attachImage(editText, width, pic, filepath);
        } else {
            return;
        }
    }

    public void showAttachImg(EditText editText, int width, String content) {
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
                ImageSpan imgSpan = new ImageSpan(mContext, pic);
                ss.setSpan(imgSpan, 0, filepath.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                int index = editText.getSelectionStart();
//                Editable edit_text = editText.getEditableText();
//                if (index < 0 || index >= edit_text.length()) {
//                    edit_text.append(ss);
//                } else {
//                    edit_text.insert(index, ss);
//                }
//                edit_text.insert(index + ss.length(), "\n");//光标设置在下一行
                Editable edit_text = editText.getEditableText();
                edit_text.insert(0, ss);
            } else {
                return;
            }
        }
    }

    /**
     * 保存笔记
     */
    private void saveNote() {
        view.hideKeyBoard();
        if (TextUtils.isEmpty(view.getLabelText())) {
            note.setLabel(mContext.getString(R.string.default_label));
        } else {
            note.setLabel(view.getLabelText());
        }
        note.setContent(view.getContentText());
        note.setLastOprTime(TimeUtils.getCurrentTimeInLong());
        note.setStatus(SNote.Status.NEED_PUSH.getValue());
        event = new MainPresenter.NotifyEvent<>();
        switch (operateMode) {
            case CREATE_NOTE_MODE:
                note.setCreateTime(TimeUtils.getCurrentTimeInLong());
                event.setType(MainPresenter.NotifyEvent.CREATE_NOTE);
                mFinalDb.saveBindId(note);
                break;
            default:
                event.setType(MainPresenter.NotifyEvent.UPDATE_NOTE);
                mFinalDb.update(note);
                break;
        }
        event.setData(note);
        view.finishView();
    }

    private String getOprTimeLineText(SNote note) {
        if (note == null || note.getLastOprTime() == 0)
            return "";
        String create = mContext.getString(R.string.create);
        String edit = mContext.getString(R.string.last_update);
        StringBuilder sb = new StringBuilder();
        if (note.getLastOprTime() <= note.getCreateTime() || note.getCreateTime() == 0) {
            sb.append(mContext.getString(R.string.note_log_text, create, TimeUtils.getTime(note.getLastOprTime())));
            return sb.toString();
        }
        sb.append(mContext.getString(R.string.note_log_text, edit, TimeUtils.getTime(note.getLastOprTime())));
        sb.append("\n");
        sb.append(mContext.getString(R.string.note_log_text, create, TimeUtils.getTime(note.getCreateTime())));
        return sb.toString();
    }


    @Override
    public void onFocusChange(android.view.View v, boolean hasFocus) {
        if (hasFocus) {
            view.setToolbarTitle(R.string.edit_note);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (view.isDoneMenuItemNull())
            return;
        String labelSrc = view.getLabelText();
        String contentSrc = view.getContentText();
        String content = contentSrc.replaceAll("\\s*|\t|\r|\n", "");
        if (!TextUtils.isEmpty(content)) {
            if (TextUtils.equals(labelSrc, note.getLabel()) && TextUtils.equals(contentSrc, note.getContent())) {
                view.setDoneMenuItemVisible(false);
                return;
            }
            view.setDoneMenuItemVisible(true);
        } else {
            view.setDoneMenuItemVisible(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                saveNote();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                view.finishView();
                break;
            default:
                break;
        }
    }

}