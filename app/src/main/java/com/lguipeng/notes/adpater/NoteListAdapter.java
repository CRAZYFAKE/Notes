package com.lguipeng.notes.adpater;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.evernote.client.android.type.NoteRef;
import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.base.BaseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yao.YiXiang on 2016/4/13.
 */
public class NoteListAdapter extends BaseRecyclerViewAdapter<NoteRef> implements Filterable {

    private final List<NoteRef> originalList;
    private Context mContext;

    public NoteListAdapter(List<NoteRef> list) {
        super(list);
        originalList = new ArrayList<>(list);
    }

    public NoteListAdapter(List<NoteRef> list, Context context) {
        super(list, context);
        originalList = new ArrayList<>(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        final View view = LayoutInflater.from(mContext).inflate(R.layout.note_list_item_layout, parent, false);
        return new NotesItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        NotesItemViewHolder holder = (NotesItemViewHolder) viewHolder;
        NoteRef note = list.get(position);
        if (note == null)
            return;
        String label = "";
        if (mContext != null) {
            boolean b = TextUtils.equals(mContext.getString(R.string.default_label), note.getTitle());
            label = b ? "" : note.getTitle();
        }
        holder.setLabelText(label);
        animate(viewHolder, position);
    }

    @Override
    public Filter getFilter() {
        return new NoteFilter(this, originalList);
    }

    @Override
    protected Animator[] getAnimators(View view) {
        if (view.getMeasuredHeight() <= 0) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1.0f);
            return new ObjectAnimator[]{scaleX, scaleY};
        }
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1.0f),
                ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1.0f),
        };
    }

    @Override
    public void setList(List<NoteRef> list) {
        super.setList(list);
        this.originalList.clear();
        originalList.addAll(list);
    }

    private static class NoteFilter extends Filter {

        private final NoteListAdapter adapter;

        private final List<NoteRef> originalList;

        private final List<NoteRef> filteredList;

        private NoteFilter(NoteListAdapter adapter, List<NoteRef> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();
            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.list.clear();
            adapter.list.addAll((ArrayList<NoteRef>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}
