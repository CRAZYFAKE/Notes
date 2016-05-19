package com.lguipeng.notes.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.EditText;

import com.lguipeng.notes.injector.ContextLifeCycle;

import java.io.File;

import javax.inject.Inject;

/**
 * Created by Yao.YiXiang on 2016/4/21.
 */
public class AttachmentUtils {

    private Context mContext;

    @Inject
    public AttachmentUtils(@ContextLifeCycle("App") Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 将图片拼成的Span加入到EditText
     */
    public void attachImage(EditText editText, int width, Bitmap pic, Intent data) {
        String path = "";
        String fileName = "";
        String mimeType = "";
        final String[] QUERY_COLUMNS = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DISPLAY_NAME
        };
        Uri selectedImage = data.getData();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(selectedImage, QUERY_COLUMNS, null, null, null);
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[1]));
                fileName = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[3]));
                mimeType = cursor.getString(cursor.getColumnIndex(QUERY_COLUMNS[2]));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        /* 图片转成SpannableString加到EditText中 */
        File out = new File(Environment.getExternalStorageDirectory()
                .getPath(), path);
        Uri uri = Uri.fromFile(out);
        float scaleWidth = ((float) width) / pic.getWidth();
        Matrix mx = new Matrix();
        mx.setScale(scaleWidth, scaleWidth);
        pic = Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), mx, true);
        String tempUrl = "<img src='" + path + "'/>";
        SpannableString ss = new SpannableString(tempUrl);
        ImageSpan imgSpan = new ImageSpan(mContext, pic);
        ss.setSpan(imgSpan, 0, tempUrl.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        /* 图片转成SpannableString加到EditText中 */
        int index = editText.getSelectionStart();
        Editable edit_text = editText.getEditableText();
        if (index < 0 || index >= edit_text.length()) {
            edit_text.append(ss);
        } else {
            edit_text.insert(index, ss);
        }
        edit_text.insert(index + ss.length(), "\n");//光标设置在下一行
    }
}