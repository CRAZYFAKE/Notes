package com.lguipeng.notes.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evernote.client.android.type.NoteRef;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Notebook;
import com.lguipeng.notes.R;
import com.lguipeng.notes.task.FindNotesTask;
import com.lguipeng.notes.utils.ToolbarUtils;
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
    RecyclerView mNotesView;
    @Bind(R.id.empty_notes)
    TextView emptyText;
    @Bind(R.id.progress_bar)
    ProgressWheel progressWheel;
    @Bind(R.id.refresh)
    SwipeRefreshLayout refreshLayout;

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
            mNotesView.setAdapter(mNoteListAdapter);
            refreshLayout.setColorSchemeColors(getColorPrimary());
            showProgressWheel(false);
        }

    }

    @Override
    public void initToolbar() {
        ToolbarUtils.initToolbar(toolbar, this);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener((view) -> finish());
        }
    }

    private void fixRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(NotesActivity.this);
        mNotesView.setLayoutManager(layoutManager);
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

    public interface ItemClickListener {
        public void onItemClick(View view);
    }
    public interface ItemLongClickListener {
        public void onItemLongClick(View view);
    }

    class NoteListAdapter<E> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener,View.OnLongClickListener {

        List<NoteRef> mList;
        Context mContext;

        private ItemClickListener mItemClickListener = null;
        private ItemLongClickListener mItemLongClickListener = null;

        public NoteListAdapter(List<NoteRef> list, Context context) {
            this.mList = list;
            this.mContext = context;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.position = position;
            NoteRef noteRef = mList.get(position);
            viewHolder.tv_title.setText(noteRef.getTitle());
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_item_layout, null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            return new MyViewHolder(view);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null){

            }
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

        public void setOnItemClickListener(ItemClickListener listener){
            this.mItemClickListener = listener;
        }
        public void setOnLongItemClickListener(ItemLongClickListener listener){
            this.mItemLongClickListener = listener;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            public TextView tv_title;
            public int position;

            public MyViewHolder(View rootView) {
                super(rootView);
                tv_title = (TextView) itemView.findViewById(R.id.note_title);
            }
        }
    }
}