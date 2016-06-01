package com.lguipeng.notes.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.AttachmentsAdapter;
import com.lguipeng.notes.adpater.base.BaseRecyclerViewAdapter;
import com.lguipeng.notes.model.Attachment;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.impl.NotePresenter;
import com.lguipeng.notes.utils.FileUtils;
import com.lguipeng.notes.utils.ToolbarUtils;
import com.lguipeng.notes.view.FixedRecyclerView;
import com.lguipeng.notes.view.FloatingMenuHidable;

import net.tsz.afinal.FinalDb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cc.trity.floatingactionbutton.FloatingActionButton;

public class AttachmentActivity extends BaseActivity {

    public final static int SELECT_IMAGE = 100;
    public final static int SELECT_AUDIO = 101;
    public final static int SELECT_FILE = 102;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.attachments_view)
    FixedRecyclerView mAttachmentsView;
    @Bind(R.id.empty_attachments)
    TextView emptyText;
    @Bind(R.id.refresher)
    SwipeRefreshLayout refreshLayout;

    //添加附件按钮
    @Bind(R.id.attach_content)
    FloatingMenuHidable attachContent;
    @Bind(R.id.fb_attach_image)
    FloatingActionButton attachImage;
    @Bind(R.id.fb_attach_audio)
    FloatingActionButton attachAudio;
    @Bind(R.id.fb_attach_file)
    FloatingActionButton attachFile;

    private AttachmentsAdapter mAttachmentsAdapter;
    private List<Attachment> attachmentList;
    private FinalDb mFinalDb;
    private SNote mCurrentNote;
    private int mNoteId;
    private Context mContext;
    private Attachment attachment;
    private int mIsChanged;//0-》未改变，1-》改变了


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchWithNoAnim();
        fixRecyclerView();
        enableSwipeRefreshLayout(false);
        initData();
        initList();

    }

    private void initData() {
        mFinalDb = FinalDb.create(this);
        mCurrentNote = (SNote) getIntent().getExtras().getSerializable(NotePresenter.CURRENT_NOTE);
        mNoteId = mCurrentNote.getId();
        mIsChanged = 0;
        attachmentList = new ArrayList<Attachment>();
        this.mContext = this;
    }

    private void initList() {
        attachmentList = mFinalDb.findAllByWhere(Attachment.class, "noteId=\"" + mNoteId + "\"");
        initRecyclerView(attachmentList);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void initRecyclerView(List<Attachment> attachments) {
        mAttachmentsAdapter = new AttachmentsAdapter(attachments, this);
        mAttachmentsView.setHasFixedSize(true);
        mAttachmentsAdapter.setOnInViewClickListener(R.id.attachment_more,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<Attachment>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Attachment attachment) {
                        super.OnClickListener(parentV, v, position, attachment);
                        PopupMenu popup = new PopupMenu(mContext, findViewById(R.id.attachment_more));
                        popup.getMenuInflater()
                                .inflate(R.menu.menu_attachment, popup.getMenu());
                        popup.setOnMenuItemClickListener((item -> onPopupMenuClick(item.getItemId(), attachment)));
                        popup.show();
                    }
                });
        mAttachmentsAdapter.setFirstOnly(false);
        mAttachmentsAdapter.setDuration(300);
        mAttachmentsView.setAdapter(mAttachmentsAdapter);
        refreshLayout.setColorSchemeColors(getColorPrimary());
    }

    /**
     * 点击附件列表的item的处理事件
     *
     * @param id         1->查看，2->删除
     * @param attachment 附件对象
     */
    public boolean onPopupMenuClick(int id, Attachment attachment) {
        switch (id) {
            case R.id.view_attachment:
                openFile(attachment);
                break;
            case R.id.delete_attachment:
                mFinalDb.delete(attachment);
                mAttachmentsAdapter.remove(attachment);
                mIsChanged = 1;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        toolbar.setTitle(getResources().getString(R.string.attach));
        super.onResume();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_attachment;
    }

    @Override
    public void initToolbar() {
        ToolbarUtils.initToolbar(toolbar, this);
    }

    private void fixRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AttachmentActivity.this);
        mAttachmentsView.setLayoutManager(layoutManager);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener((view) -> setResult());
        }
    }

    @OnClick(R.id.fb_attach_image)
    public void attachImage() {
        attachImage(this);
        attachContent.collapse();//折叠列表
    }

    @OnClick(R.id.fb_attach_audio)
    public void attachAudio() {
        attachAudio(this);
        attachContent.collapse();
    }

    @OnClick(R.id.fb_attach_file)
    public void attachFile() {
        attachFile(this);
        attachContent.collapse();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case SELECT_IMAGE:
                    saveImage(data);
                    break;
                case SELECT_AUDIO:
                    saveAudio(data);
                    break;
                case SELECT_FILE:
                    saveFile(data);
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        } else {
            return;
        }
    }

    /**
     * 选择文件后，
     *
     * @param data
     */
    public void saveAudio(Intent data) {
        attachment = new Attachment();
        String path = "";
        String fileName = "";
        String mimeType = "";
        final String[] QUERY_COLUMNS = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DISPLAY_NAME
        };
        Uri attach = data.getData();
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(attach, QUERY_COLUMNS, null, null, null);
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[1]));
                mimeType = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[2]));
                fileName = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[3]));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (fileName == "" || path == "" || mimeType == "") {
            return;
        }
        attachment.setFileName(fileName);
        attachment.setMimeType(mimeType);
        attachment.setPath(path);
        attachment.setNoteId(mNoteId);
        mFinalDb.saveBindId(attachment);
        mAttachmentsAdapter.add(attachment);
        mCurrentNote.setStatus(SNote.Status.NEED_PUSH.getValue());
        mFinalDb.update(mCurrentNote);
        mIsChanged = 1;
    }

    public void saveImage(Intent data) {
        attachment = new Attachment();
        String path = "";
        String fileName = "";
        String mimeType = "";
        final String[] QUERY_COLUMNS = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DISPLAY_NAME
        };
        Uri attach = data.getData();
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(attach, QUERY_COLUMNS, null, null, null);
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[1]));
                mimeType = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[2]));
                fileName = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[3]));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (fileName == "" || path == "" || mimeType == "") {
            return;
        }
        attachment.setFileName(fileName);
        attachment.setMimeType(mimeType);
        attachment.setPath(path);
        attachment.setNoteId(mNoteId);
        mAttachmentsAdapter.add(attachment);
        mFinalDb.saveBindId(attachment);
        mCurrentNote.setStatus(SNote.Status.NEED_PUSH.getValue());
        mFinalDb.update(mCurrentNote);
        mIsChanged = 1;
    }

    /**
     * 选择文件之后进行保存
     *
     * @param data 文件数据
     */
    public void saveFile(Intent data) {
        attachment = new Attachment();
        Uri attach = data.getData();
        String path = attach.getPath();
        String fileName = FileUtils.getFileName(path);
        String mimeType = FileUtils.getMimeType(new File(path));
        if (fileName == "" || path == "" || mimeType == "") {
            return;
        }
        attachment.setFileName(fileName);
        attachment.setMimeType(mimeType);
        attachment.setPath(path);
        attachment.setNoteId(mNoteId);
        mAttachmentsAdapter.add(attachment);
        mFinalDb.saveBindId(attachment);
        mCurrentNote.setStatus(SNote.Status.NEED_PUSH.getValue());
        mFinalDb.update(mCurrentNote);
        mIsChanged = 1;
    }

    /**
     * 查看附件，根据附件类型提示使用不同的应用查看附件
     *
     * @param attachment
     */
    private void openFile(Attachment attachment) {
        File file = new File(attachment.getPath());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = FileUtils.getMimeType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        //跳转
        try {
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.open_file)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "安装文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 跳转到选择图片界面
     *
     * @param activity
     */
    public void attachImage(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)),
                SELECT_IMAGE);
    }

    /**
     * 跳转到选择音频界面
     *
     * @param activity
     */
    public void attachAudio(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_audio)),
                SELECT_AUDIO);
    }

    /**
     * 跳转转到选择文件界面
     *
     * @param activity
     */
    public void attachFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_file)),
                SELECT_FILE);
    }

    public void enableSwipeRefreshLayout(boolean enable) {
        refreshLayout.setEnabled(enable);
    }


    /**
     * 点击返回按钮，执行setResult()函数
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult();
        }
        return false;
    }

    /**
     * 将是否更改附件mIsChanged，回传给NoteActiviy。
     */
    public void setResult() {
        Intent intent = new Intent();
        intent.putExtra(NotePresenter.IS_ATTACHMENT_CHANGED, mIsChanged);
        setResult(RESULT_OK, intent);
        finish();
    }
}