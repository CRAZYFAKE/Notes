package com.lguipeng.notes.view;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * 自定义FloatingActionButton
 */
public class BetterFab extends FloatingActionButton {
    private boolean isShown = true;
    private int ANIM_DURATION = 200;
    private boolean mVisible = false;
    private boolean forceHide = false;

    public BetterFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BetterFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BetterFab(Context context) {
        super(context);
    }

    public boolean isForceHide() {
        return forceHide;
    }

    public void setForceHide(boolean forceHide) {
        this.forceHide = forceHide;
        if (!forceHide) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }
    }

    //if hide，disable animation
    public boolean canAnimation() {
        return !isForceHide();
    }

    public void show(boolean isVisiable) {
        mVisible = isVisiable;
        int translationX = isVisiable ? 0 : (getWidth() / 2) + getMarginRight();
        this.animate().translationX(translationX).setDuration(ANIM_DURATION).start();
    }

    public boolean isShown() {
        return isShown;
    }

    private int getMarginRight() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin;
        }
        return marginBottom;
    }

    public boolean getVisible() {
        return mVisible;
    }
}