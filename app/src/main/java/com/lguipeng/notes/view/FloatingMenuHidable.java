package com.lguipeng.notes.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import cc.trity.floatingactionbutton.FloatingActionsMenu;


/**
 * Created by Yao.YiXiang on 2016/3/15.
 */
public class FloatingMenuHidable extends FloatingActionsMenu {

    private boolean isShown = true;
    private int ANIM_DURATION = 200;
    private boolean mVisible = false;

    public FloatingMenuHidable(Context context) {
        super(context);

    }

    public FloatingMenuHidable(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public FloatingMenuHidable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }


    public void show(boolean isVisible) {
        mVisible = isVisible;
        int translationX = isVisible ? 0 : (getWidth() / 2) + getMarginRight();
        this.animate().translationX(translationX).setDuration(ANIM_DURATION).start();
    }

    public boolean isShown() {
        return isShown;
    }

    private int getMarginRight() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((MarginLayoutParams) layoutParams).rightMargin;
        }
        return marginBottom;
    }

    public boolean getVisible() {
        return mVisible;
    }
}
