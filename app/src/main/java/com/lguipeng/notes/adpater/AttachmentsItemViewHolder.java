package com.lguipeng.notes.adpater;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lguipeng.notes.R;

/**
 * Created by Yao.YiXiang on 2016/5/17.
 */
public class AttachmentsItemViewHolder extends RecyclerView.ViewHolder {

    private final TextView mFileName;
    private final ImageView mFileSign;

    public AttachmentsItemViewHolder(View parent) {
        super(parent);
        mFileName = (TextView) parent.findViewById(R.id.attachment_name);
        mFileSign = (ImageView) parent.findViewById(R.id.attachment_sign);
    }

    public void setFileSign(int resId) {
        mFileSign.setImageResource(resId);
    }

    public void setFileName(CharSequence text) {
        setTextView(mFileName, text);
    }

    public void setFileName(int text) {
        setTextView(mFileName, text);
    }

    private void setTextView(TextView view, CharSequence text) {
        if (view == null)
            return;
        if (TextUtils.isEmpty(text))
            view.setVisibility(View.GONE);
        view.setText(text);
    }

    private void setTextView(TextView view, @StringRes int text) {
        if (view == null || text <= 0)
            return;
        view.setText(text);
    }
}