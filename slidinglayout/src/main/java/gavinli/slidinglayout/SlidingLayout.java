package gavinli.slidinglayout;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by GavinLi
 * on 16-9-19.
 */
public class SlidingLayout extends ViewGroup {
    private final ViewDragHelper mViewDragHelper;
    private View mHeaderView;
    private View mDescView;

    private OnViewRemoveListener onViewRemoveListener;

    private static final int WAIT_MODE = 0;
    private static final int PULL_MODE = 1;
    private static final int CLEAR_MODE = 2;
    private int mDragMode = PULL_MODE;

    private int mTop;
    private int mVerticalDragRange;
    private float mVerticalDragOffset;

    private int mHorizontalDragRange;
    private float mHorizontalDragOffset;

    public SlidingLayout(Context context) {
        this(context, null);
    }

    public SlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewDragHelper = ViewDragHelper.create(this, 1f, new DragHelpCallBack());
    }

    public void setOnViewRemoveListener(OnViewRemoveListener listener) {
        this.onViewRemoveListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
        final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                getPaddingTop() + getPaddingBottom(), maxHeight - mHeaderView.getMeasuredHeight());
        measureChild(mDescView, widthMeasureSpec, childHeightMeasureSpec);

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mVerticalDragRange = getHeight() - mHeaderView.getMeasuredHeight();
        mHorizontalDragRange = getWidth();

        mHeaderView.layout(
                0,
                mTop,
                r,
                mTop + mHeaderView.getMeasuredHeight());

        mDescView.layout(
                0,
                mTop + mHeaderView.getMeasuredHeight(),
                r,
                mTop + b);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    private float mLastPostionX;
    private float mLastPostionY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isHeaderViewUnder = mViewDragHelper.isViewUnder(
                mHeaderView, (int)event.getX(), (int)event.getY());
        boolean isDescViewUnder = mViewDragHelper.isViewUnder(
                mDescView, (int)event.getX(), (int)event.getY());

        if(isHeaderViewUnder) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastPostionX = event.getX();
                    mLastPostionY = event.getY();
                    if (mTop == getHeight() - mHeaderView.getMeasuredHeight())
                        mDragMode = WAIT_MODE;
                case MotionEvent.ACTION_MOVE:
                    if (mDragMode == WAIT_MODE) {
                        if (Math.abs(mLastPostionX - event.getX()) > 5) {
                            mDragMode = CLEAR_MODE;
                        } else if (Math.abs(mLastPostionY - event.getY()) > 5) {
                            mDragMode = PULL_MODE;
                        }
                    }
                    break;
            }
        }
        mViewDragHelper.processTouchEvent(event);

        return mDragMode == CLEAR_MODE || isHeaderViewUnder || isDescViewUnder;
    }

    @Override
    public void computeScroll() {
        if(mViewDragHelper.continueSettling(true))
            ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderView = getChildAt(0);
        mDescView = getChildAt(1);
    }

    private class DragHelpCallBack extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mHeaderView;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mViewDragHelper.captureChildView(mHeaderView, pointerId);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if(mDragMode == CLEAR_MODE) {
                return getHeight() - child.getMeasuredHeight();
            } else {
                final int topBound = getPaddingTop();
                final int bottomBound = getHeight() - mHeaderView.getHeight();
                return Math.min(Math.max(top, topBound), bottomBound);
            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(mDragMode == PULL_MODE) {
                return 0;
            } else {
                return left;
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if(mDragMode == PULL_MODE) {
                mTop = top;
                mVerticalDragOffset = (float) top / mVerticalDragRange;
                mDescView.setAlpha(1 - mVerticalDragOffset);
                requestLayout();
            } else if(mDragMode == CLEAR_MODE) {
                mHorizontalDragOffset = Math.abs((float) left / mHorizontalDragRange);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if(mDragMode == PULL_MODE) {
                int top = getPaddingTop();
                if (yvel > 2000 || (yvel >= 0 && mVerticalDragOffset > 0.4f)) {
                    top += mVerticalDragRange;
                }
                mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
                invalidate();
            } else if(mDragMode == CLEAR_MODE) {
                int left = getPaddingLeft();
                if (Math.abs(xvel) > 2000 || mHorizontalDragOffset > 0.4f) {
                    left += mHorizontalDragRange;
                    mViewDragHelper.settleCapturedViewAt(left, releasedChild.getTop());
                    onViewRemoveListener.removeView();
                } else {
                    mViewDragHelper.settleCapturedViewAt(0, releasedChild.getTop());
                    invalidate();
                }
            } else {
                mViewDragHelper.settleCapturedViewAt(0, getHeight() - releasedChild.getMeasuredHeight());
                invalidate();
            }
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mVerticalDragRange;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mHorizontalDragRange;
        }
    }
}
