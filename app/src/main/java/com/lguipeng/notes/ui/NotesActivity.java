package com.lguipeng.notes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.evernote.client.android.type.NoteRef;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Notebook;
import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.NoteListAdapter;
import com.lguipeng.notes.adpater.base.BaseRecyclerViewAdapter;
import com.lguipeng.notes.task.FindNotesTask;
import com.lguipeng.notes.task.GetNoteHtmlTask;
import com.lguipeng.notes.utils.ToolbarUtils;
import com.lguipeng.notes.view.FixedRecyclerView;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.vrallev.android.task.TaskResult;

import java.util.List;

import butterknife.Bind;

/**
 * 查看云端笔记本里的笔记
 */
public class NotesActivity extends BaseActivity {

    public static final String KEY_NOTEBOOK = "KEY_NOTEBOOK";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.notes_view)
    FixedRecyclerView mNotesView;
    @Bind(R.id.empty_notes)
    TextView emptyText;
    @Bind(R.id.progress_bar)
    ProgressWheel progressWheel;
    @Bind(R.id.refresh)
    SwipeRefreshLayout refreshLayout;

    private NoteRef mCurNote;
    private String mQurry;
    private Notebook mNotebook;
    private LinkedNotebook mLinkedNotebook;
    private NoteListAdapter mNoteListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchWithNoAnim();
        getNotes();
        fixRecyclerView();
    }

    private void getNotes() {
        showProgressWheel(true);
        mNotebook = (Notebook) getIntent().getExtras().getSerializable(KEY_NOTEBOOK);
        new FindNotesTask(0, 20, mNotebook, mLinkedNotebook, mQurry).start(this);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_notes;
    }

    @TaskResult
    public void init(List<NoteRef> list) {
        toolbar.setTitle(mNotebook.getName());
        mNotesView.setHasFixedSize(true);
        if (list.size() == 0 || list == null) {
            showProgressWheel(false);
            showEmpty();
        } else {
            showNotesView();
            mNoteListAdapter = new NoteListAdapter(list, this);
            mNotesView.setHasFixedSize(true);
            mNoteListAdapter.setOnInViewClickListener(R.id.note_list_item_root,
                    new BaseRecyclerViewAdapter.onInternalClickListenerImpl<NoteRef>() {
                        @Override
                        public void OnClickListener(View parentV, View v, Integer position, NoteRef values) {
                            super.OnClickListener(parentV, v, position, values);
                            new GetNoteHtmlTask(list.get(position)).start(NotesActivity.this, "html");
                            mCurNote = list.get(position);
                        }
                    });
            mNoteListAdapter.setFirstOnly(false);
            mNoteListAdapter.setDuration(300);
            mNotesView.setAdapter(mNoteListAdapter);
            enableSwipeRefreshLayout(false);
            showProgressWheel(false);
        }
    }

    @Override
    public void initToolbar() {
        ToolbarUtils.initToolbar(toolbar, this);
    }

    private void fixRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(NotesActivity.this);
        mNotesView.setLayoutManager(layoutManager);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener((view) -> finish());
        }
    }

    @TaskResult(id = "html")
    public void gotoViewHtml(String html, GetNoteHtmlTask task) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ViewHTMLActivity.KEY_NOTE, mCurNote);
        bundle.putString(ViewHTMLActivity.KEY_HTML, html);
        intent.putExtras(bundle);
        intent.setClass(NotesActivity.this, ViewHTMLActivity.class);
        startActivity(intent);
    }

    public void showProgressWheel(boolean visible) {
        progressWheel.setBarColor(getColorPrimary());
        if (visible) {
            if (!progressWheel.isSpinning())
                progressWheel.spin();
        } else {
            progressWheel.postDelayed(() -> {
                if (progressWheel.isSpinning()) {
                    progressWheel.stopSpinning();
                }
            }, 300);
        }
    }

    public void enableSwipeRefreshLayout(boolean enable) {
        refreshLayout.setEnabled(enable);
    }

    /**
     * 显示"Empty notes"
     */
    private void showEmpty() {
        emptyText.setVisibility(View.VISIBLE);
        refreshLayout.setVisibility(View.GONE);
    }

    /**
     * 显示笔记本view
     */
    private void showNotesView() {
        emptyText.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
    }

}