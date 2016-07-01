package com.dhchoi.crowdsourcingapp.views;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.widget.ListView;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    private ListView mListView;

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwipeRefreshLayout setListView(ListView listView) {
        mListView = listView;
        return this;
    }

    @Override
    public boolean canChildScrollUp() {
        return mListView != null && mListView.canScrollVertically(-1);
    }
}
