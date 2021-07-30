package com.murphy.pokotalk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
    protected Runnable reachTopCallback;
    protected boolean reachedTop;
    protected boolean blockLayingOutChildren;
    protected int pivotVisiblePositionMark;
    protected int pivotVisibleItemTopMark;

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

    @Override
    protected void layoutChildren() {
        if (!blockLayingOutChildren) {
            super.layoutChildren();
        }
    }

    private void init() {
        onScrollListnerDetectable = new OnScrollListenerDetectable();
        super.setOnScrollListener(onScrollListnerDetectable);
        keepVerticalPosition = false;
        scrollDownAtFirst = false;
        isFirstLoop = true;
        reachTopCallback = null;
        reachedTop = false;
        blockLayingOutChildren = false;
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
                setSelectionFromTop(firstVisiblePosition, firstVisibleItemTop + yDelta);
            }
        }
    }

    /** Mark current scroll position with respect to visible item position. */
    public void markScrollPosition(int visiblePosition) {
        View pivotVisibleItem = getChildAt(visiblePosition);
        if (pivotVisibleItem == null) {
            pivotVisiblePositionMark = -1;
            Log.v("POKO", "MARK FIRST POSITION -1");
            return;
        }
        pivotVisiblePositionMark = getFirstVisiblePosition() + visiblePosition;
        pivotVisibleItemTopMark = pivotVisibleItem.getTop();
        blockLayingOutChildren = true;
        Log.v("POKO", "MARK PIVOT POSITION " + pivotVisiblePositionMark);
    }

    /** Move to marked scroll position.
     * If the ListView has no entry when mark, it scrolls to the top. */
    public void scrollToMark(int positionDelta) {
        blockLayingOutChildren = false;
        if (pivotVisiblePositionMark < 0) {
            setSelectionAfterHeaderView();
        } else {
            int totalItems = getCount();
            int finalPosition = pivotVisiblePositionMark + positionDelta;
            if (finalPosition < 0) {
                finalPosition = 0;
            } else if (finalPosition >= totalItems) {
                finalPosition = totalItems - 1;
            }
            setSelectionFromTop(finalPosition, pivotVisibleItemTopMark);
        }
    }

    public Runnable getReachTopCallback() {
        return reachTopCallback;
    }

    public void setReachTopCallback(Runnable reachTopCallback) {
        this.reachTopCallback = reachTopCallback;
    }

    class OnScrollListenerDetectable implements ListView.OnScrollListener {
        OnScrollListener outerOnScrollListener;
        int currentVisibleItemCount;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            currentVisibleItemCount = visibleItemCount;

            if (outerOnScrollListener != null)
                outerOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

            if (visibleItemCount == 0 || (firstVisibleItem == 0
                    && getListView().getChildAt(0).getTop() == 0)) {
                if (!reachedTop && reachTopCallback != null) {
                    reachTopCallback.run();
                    reachedTop = true;
                    Log.v("POKO", "Reached top");
                }
            } else {
                reachedTop = false;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE && this.currentVisibleItemCount > 0) {
                ListViewDetectable listView = getListView();
                int lastItemIndex = listView.getCount() - 1;
                int lastVisibleIndex = listView.getLastVisiblePosition();
                int lastItemPosition = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
                View lastVisibleChild = listView.getChildAt(lastItemPosition);

                /* Check if ListView it at bottom */
                if (lastVisibleChild != null && lastVisibleIndex == lastItemIndex
                        && lastVisibleChild.getBottom() <= listView.getHeight()) {
                    setFullyAtBottom(true);
                } else {
                    setFullyAtBottom(false);
                }
            }

            if (outerOnScrollListener != null)
                outerOnScrollListener.onScrollStateChanged(view, scrollState);
        }

        public void setOuterOnScrollListener(OnScrollListener outerOnScrollListener) {
            this.outerOnScrollListener = outerOnScrollListener;
        }

        public ListViewDetectable getListView() {
            return ListViewDetectable.this;
        }
    }
}
