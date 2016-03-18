package com.dhchoi.crowdsourcingapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Fixed-height ListView that can be put into ScrollViews.
 *
 * Reference: http://stackoverflow.com/a/29708371
 */
public class CustomListView extends ListView {
    public CustomListView(Context context) {
        super(context);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 4, MeasureSpec.AT_MOST));
    }
}
