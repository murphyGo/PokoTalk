package com.murphy.pokotalk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/** Extended ListView class with detecting scroll state feature */
public class ListViewDetectable extends ListView {
    protected OnScrollListenerDetectable onScrollListnerDetectable;
    protected boolean fullyAtBottom;
    protected boolean keepVerticalPosition;
    protected boolean scrollDownAtFirst;
    protected boolean isFirstLoop;

    public ListViewDetectable(Context context) {
        super(context);
        init();
    }

    public ListViewDetectable(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListViewDetectable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ListViewDetectable(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        onScrollListnerDetectable = new OnScrollListenerDetectable();
        super.setOnScrollListener(new OnScrollListenerDetectable());
        keepVerticalPosition = false;
        scrollDownAtFirst = false;
        isFirstLoop = true;
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        onScrollListnerDetectable.setOuterOnScrollListener(l);
    }

    public void setKeepVerticalPosition(boolean adjustVerticalPosition) {
        this.keepVerticalPosition = adjustVerticalPosition;
    }

    protected void setFullyAtBottom(boolean fullyAtBottom) {
        this.fullyAtBottom = fullyAtBottom;
    }

    public boolean isFullyAtBottom() {
        return fullyAtBottom;
    }

    public void postScrollToBottom() {
        post(new Runnable() {
            @Override
            public void run() {
                setSelection(getCount() - 1);
                setFullyAtBottom(true);
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (getCount() > 0 && h != oldh) {
            if (keepVerticalPosition) {
                int firstVisiblePosition = getFirstVisiblePosition();
                View firstVisibleItem = getChildAt(0);
                if (firstVisibleItem == null)
                    return;
                int firstVisibleItemTop = firstVisibleItem.getTop();
                int yDelta = h - oldh;
                smoothScrollToPositionFromTop(firstVisiblePosition, firstVisibleItemTop + yDelta, 0);
            }
        }
    }

    class OnScrollListenerDetectable implements ListView.OnScrollListener {
        OnScrollListener outerOnScrollListner;
        int currentVisibleItemCount;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            currentVisibleItemCount = visibleItemCount;

            if (outerOnScrollListner != null)
                outerOnScrollListner.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE && this.currentVisibleItemCount > 0) {
                ListViewDetectable listView = getListView();
                int lastItemIndex = listView.getCount() - 1;
                int lastVisibileIndex = listView.getLastVisiblePosition();
                int lastItemPosition = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
                View lastVisibleChild = listView.getChildAt(lastItemPosition);

                /* Check if ListView it at bottom */
                if (lastVisibleChild != null && lastVisibileIndex == lastItemIndex
                        && lastVisibleChild.getBottom() <= listView.getHeight()) {
                    setFullyAtBottom(true);
                } else {
                    setFullyAtBottom(false);
                }
            }

            if (outerOnScrollListner != null)
                outerOnScrollListner.onScrollStateChanged(view, scrollState);
        }

        public void setOuterOnScrollListener(OnScrollListener outerOnScrollListener) {
            this.outerOnScrollListner = outerOnScrollListener;
        }

        public ListViewDetectable getListView() {
            return ListViewDetectable.this;
        }
    }
}
