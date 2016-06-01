package com.lguipeng.notes.adpater;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.base.BaseRecyclerViewAdapter;
import com.lguipeng.notes.model.Attachment;
import com.lguipeng.notes.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yao.YiXiang on 2016/5/17.
 */
public class AttachmentsAdapter extends BaseRecyclerViewAdapter<Attachment> {
    private final List<Attachment> originalList;
    private Context mContext;

    public AttachmentsAdapter(List<Attachment> list) {
        super(list);
        originalList = new ArrayList<>(list);
    }

    public AttachmentsAdapter(List<Attachment> list, Context context) {
        super(list, context);
        originalList = new ArrayList<>(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item_attachments_layout, parent, false);
        return new AttachmentsItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        AttachmentsItemViewHolder holder = (AttachmentsItemViewHolder) viewHolder;
        Attachment attachment = list.get(position);
        if (attachment == null)
            return;
        holder.setFileName(attachment.getFileName());
        holder.setFileSign(findCurFileSign(FileUtils.getFileSuffixName(attachment.getFileName())));
        animate(viewHolder, position);
    }

    /***
     * 根据文件后缀名来选择文件的标识图
     *
     * @param suffix
     * @return
     */
    public int findCurFileSign(String suffix) {
        int resID = R.drawable.ic_attach_file;
        if (("mpga").equals(suffix) || ("png").equals(suffix) || ("bmp").equals(suffix) ||
                ("gif").equals(suffix) || ("jpeg").equals(suffix) || ("jpg").equals(suffix)) {
            resID = R.drawable.ic_attach_image;
        } else if (("wmv").equals(suffix) || ("wma").equals(suffix) || ("wav").equals(suffix) ||
                ("rmvb").equals(suffix) || ("mpg4").equals(suffix) || ("mpg").equals(suffix) ||
                ("mpeg").equals(suffix) || ("mpe").equals(suffix) || ("avi").equals(suffix) ||
                ("3gp").equals(suffix) || ("mp4").equals(suffix) || ("mp3").equals(suffix) ||
                ("mp2").equals(suffix) || ("mov").equals(suffix)) {
            resID = R.drawable.ic_attach_audio;
        }
        return resID;
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
    public void setList(List<Attachment> list) {
        super.setList(list);
        this.originalList.clear();
        originalList.addAll(list);
    }

    /**
     * 文件后缀名与资源列表匹配表
     */
    private final String[][] RESID_MapTable = {
            //{后缀名，    drawable资源ID}
            {".mpga", "ic_attach_image"},
            {".png", "ic_attach_image"},
            {".bmp", "ic_attach_image"},
            {".gif", "ic_attach_image"},
            {".jpeg", "ic_attach_image"},
            {".jpg", "ic_attach_image"},
            {".mov", "ic_attach_audio"},
            {".mp2", "ic_attach_audio"},
            {".mp3", "ic_attach_audio"},
            {".mp4", "ic_attach_audio"},
            {".3gp", "ic_attach_audio"},
            {".avi", "ic_attach_audio"},
            {".mpe", "ic_attach_audio"},
            {".mpeg", "ic_attach_audio"},
            {".mpg", "ic_attach_audio"},
            {".mpg4", "ic_attach_audio"},
            {".rmvb", "ic_attach_audio"},
            {".wav", "ic_attach_audio"},
            {".wma", "ic_attach_audio"},
            {".wmv", "ic_attach_audio"},
    };
}