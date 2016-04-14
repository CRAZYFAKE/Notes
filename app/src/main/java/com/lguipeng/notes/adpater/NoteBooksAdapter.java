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

import com.evernote.edam.type.Notebook;
import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.base.BaseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yao.YiXiang on 2016/4/13.
 */
public class NoteBooksAdapter extends BaseRecyclerViewAdapter<Notebook> implements Filterable {

    private final List<Notebook> originalList;
    private Context mContext;
    public NoteBooksAdapter(List<Notebook> list) {
        super(list);
        originalList = new ArrayList<>(list);
    }

    public NoteBooksAdapter(List<Notebook> list, Context context) {
        super(list, context);
        originalList = new ArrayList<>(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        final View view = LayoutInflater.from(mContext).inflate(R.layout.notebooks_item_layout, parent, false);
        return new NotesItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        NotesItemViewHolder holder = (NotesItemViewHolder) viewHolder;
        Notebook notebook = list.get(position);
        if (notebook == null)
            return;
        String label = "";
        if (mContext != null) {
            boolean b  = TextUtils.equals(mContext.getString(R.string.default_label), notebook.getName());
            label = b? "": notebook.getName();
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
        if (view.getMeasuredHeight() <=0){
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
    public void setList(List<Notebook> list) {
        super.setList(list);
        this.originalList.clear();
        originalList.addAll(list);
    }

    private static class NoteFilter extends Filter{

        private final NoteBooksAdapter adapter;

        private final List<Notebook> originalList;

        private final List<Notebook> filteredList;

        private NoteFilter(NoteBooksAdapter adapter, List<Notebook> originalList) {
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
                for ( Notebook note : originalList) {
                    if ( note.getName().contains(constraint)) {
                        filteredList.add(note);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.list.clear();
            adapter.list.addAll((ArrayList<Notebook>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}
