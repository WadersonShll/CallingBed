package com.wdkl.callingbed.widget.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.wdkl.callingbed.R;

/**
 * 类名称：ProgressView <br>
 * 类描述：显示进度条 <br>
 * 创建人：Waderson <br>
 * 创建时间 ：2017-12-13  <br>
 */
public class ProgressView extends View {

    private Paint mPaint;
    private TextPaint mTextPaint;

    private int mRadius;
    private int mBorderColor;
    private int mProgressColor;
    private int mProgressDescColor;
    private int mMax;
    private int mProgress;
    private int mBorderWidth;
    private boolean mIsShowDesc;
    private boolean mHaveChangeColor;

    private int DEFAULT_MAX = 10;
    private int DEFAULT_PROGRESS = 0;
    public String[] changeColor = {"#CDFDCB", "#65FDCC", "#65FDCC", "#55CA7C", "#55CA7C", "#52CD53", "#52CD53", "#52CD53", "#3D9A5C", "#3D9A5C"};

    private int DEFAULT_RADIUS = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
    private int DEFAULT_BORDER_COLOR = Color.parseColor("#FD0005");
    private int DEFAULT_PROGRESS_COLOR = Color.parseColor("#77D178");
    private int DEFAULT_PROGRESS_DESC_COLOR = Color.parseColor("#000000");
    private int DEFAULT_BORDER_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
    private boolean DEFAULT_ISSHOWDESC = true;

    private int mWidth;
    private int mHeight;
    private Rect mTextBounds;
    private String mProgressDesc = "";

    private OnFinishedListener mOnFinishedListener;
    private OnAnimationEndListener mOnAnimationEndListener;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        mMax = a.getInt(R.styleable.ProgressView_max, DEFAULT_MAX);
        mProgress = a.getInt(R.styleable.ProgressView_progress, DEFAULT_PROGRESS);
        mRadius = (int) a.getDimension(R.styleable.ProgressView_progressRadius, DEFAULT_RADIUS);
        mBorderColor = a.getColor(R.styleable.ProgressView_borderColor, DEFAULT_BORDER_COLOR);
        mProgressColor = a.getColor(R.styleable.ProgressView_progressColor, DEFAULT_PROGRESS_COLOR);
        mProgressDescColor = a.getColor(R.styleable.ProgressView_progressDescColor, DEFAULT_PROGRESS_DESC_COLOR);
        mBorderWidth = (int) a.getDimension(R.styleable.ProgressView_borderWidth, DEFAULT_BORDER_WIDTH);
        mProgressDesc = a.getString(R.styleable.ProgressView_progressDesc);
        mIsShowDesc = a.getBoolean(R.styleable.ProgressView_isShowDesc, DEFAULT_ISSHOWDESC);
        mHaveChangeColor = a.getBoolean(R.styleable.ProgressView_haveChangeColor, true);
        a.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextBounds = new Rect();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mTextPaint.setColor(mProgressDescColor);
    }

    /**
     * 设置结束监听
     *
     * @param onFinishedListener
     */
    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        mOnFinishedListener = onFinishedListener;
    }

    /**
     * 设置进度停止监听
     *
     * @param onAnimationEndListener
     */
    public void setOnAnimationEndListener(OnAnimationEndListener onAnimationEndListener) {
        mOnAnimationEndListener = onAnimationEndListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBorder(canvas);
        drawProgress(canvas);
        if (mIsShowDesc) drawProgressDesc(canvas);
    }

    private void drawBorder(Canvas canvas) {
        mPaint.reset();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBorderColor);
        mPaint.setStrokeWidth(mBorderWidth);

        int left = mBorderWidth / 2;
        int top = mBorderWidth / 2;
        int right = mWidth - mBorderWidth / 2;
        int bottom = mHeight - mBorderWidth / 2;

        Path path = new Path();
        path.moveTo(left + mRadius, top);
        path.lineTo(right - mRadius, top);
        path.arcTo(new RectF(right - 2 * mRadius, top, right, top + 2 * mRadius), -90, 90);
        path.lineTo(right, bottom - mRadius);
        path.arcTo(new RectF(right - 2 * mRadius, bottom - 2 * mRadius, right, bottom), 0, 90);
        path.lineTo(left + mRadius, bottom);
        path.arcTo(new RectF(left, bottom - 2 * mRadius, left + 2 * mRadius, bottom), 90, 90);
        path.lineTo(left, top + mRadius);
        path.arcTo(new RectF(left, top, left + 2 * mRadius, top + 2 * mRadius), 180, 90);
        path.close();
        canvas.drawPath(path, mPaint);
    }

    private void drawProgress(Canvas canvas) {

        mPaint.reset();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mProgressColor);
        mPaint.setStrokeWidth(mBorderWidth);

        float left = mBorderWidth * .5f;
        float top = mBorderWidth * .5f;
        float right = mWidth - mBorderWidth * .5f;
        float bottom = mHeight - mBorderWidth * .5f;

        Path path = new Path();
        path.moveTo(left, top + mRadius);
        float scale = (mProgress * 1.f / mMax) / (mRadius * 1.f / (right - left));
        float scale2 = (mProgress * 1.f / mMax) / ((right - mRadius) * 1.f / (right - left));
        if (scale <= 1) {
            float a = scale * mRadius;
            double angle = Math.acos((mRadius - a) / mRadius);
            path.arcTo(new RectF(left, top, left + 2 * mRadius, top + 2 * mRadius), 180, (float) (angle * 180 / Math.PI));
            float y = (float) (Math.pow(Math.pow(mRadius, 2) - Math.pow((a - mRadius), 2), 0.5) + bottom - mRadius);
            path.lineTo(left + a, y);
            path.arcTo(new RectF(left, bottom - 2 * mRadius, left + 2 * mRadius, bottom), 180 - (float) (angle * 180 / Math.PI),
                    (float) (angle * 180 / Math.PI));
            path.close();
            canvas.drawPath(path, mPaint);
        } else if (scale2 <= 1) {
            path.arcTo(new RectF(left, top, left + 2 * mRadius, top + 2 * mRadius), 180, 90);
            path.lineTo(left + (mProgress * 1.f / mMax) * (right - left), top);
            path.lineTo(left + (mProgress * 1.f / mMax) * (right - left), bottom);
            path.lineTo(left + mRadius, bottom);
            path.arcTo(new RectF(left, bottom - 2 * mRadius, left + 2 * mRadius, bottom), 90, 90);
            path.close();
            canvas.drawPath(path, mPaint);
        } else {
            float a = (mProgress * 1.f / mMax) * (right - left) - (right - mRadius);
            double angle = Math.asin(a / mRadius);
            path.arcTo(new RectF(left, top, left + 2 * mRadius, top + 2 * mRadius), 180, 90);
            path.lineTo(right - mRadius, top);
            path.arcTo(new RectF(right - 2 * mRadius, top, right, top + 2
                    * mRadius), -90, (float) (angle * 180 / Math.PI));
            double y = Math.pow((Math.pow(mRadius, 2) - Math.pow(a, 2)), .5) + top + mRadius;

            path.lineTo(right - mRadius + a, (float) y);
            path.arcTo(new RectF(right - 2 * mRadius, bottom - 2 * mRadius,
                            right, bottom), (float) (90 - (angle * 180 / Math.PI)),
                    (float) (angle * 180 / Math.PI));
            path.lineTo(left + mRadius, bottom);
            path.arcTo(new RectF(left, bottom - 2 * mRadius,
                    left + 2 * mRadius, bottom), 90, 90);
            path.close();

            canvas.drawPath(path, mPaint);
        }
    }

    private void drawProgressDesc(Canvas canvas) {
        String finalProgressDesc = "";
        if (100 == mMax) {
            finalProgressDesc = mProgressDesc + "" + mProgress + " %";
        } else {
            finalProgressDesc = mProgressDesc + "" + mProgress + "/" + mMax;
        }
        mTextPaint.getTextBounds(finalProgressDesc, 0, finalProgressDesc.length(), mTextBounds);
        canvas.drawText(finalProgressDesc, (int) (mWidth / 2.0 - mTextBounds.width() / 2.0), (int) (mHeight / 2.0 - (mTextPaint.ascent() + mTextPaint.descent()) / 2.0f), mTextPaint);
    }

    public void setMaxProgress(int max) {

        mMax = max < 0 ? 0 : max;
        invalidateView();
    }

    private void setProgress(int progress) {

        mProgress = progress > mMax ? mMax : progress;
        invalidateView();

        if (mProgress >= mMax && mOnFinishedListener != null) {
            mOnFinishedListener.onFinish();
        }

    }

    /**
     * 得到ProgressBar的最大进度
     *
     * @return
     */
    public int getMax() {
        return mMax;
    }

    /**
     * 获取当前ProgressBar的进度
     *
     * @return
     */
    public final int getProgress() {
        return mProgress;
    }

    public void setProgressDesc(String desc) {
        mProgressDesc = desc;
        invalidateView();
    }

    /**
     * 设置当前进度条的进度(默认动画时间1.5s)
     *
     * @param progress
     */
    public void setCurProgress(final int progress) {
        if (mHaveChangeColor) {
            setProgressColor(getProgressChangeColor((float) progress / (float) mMax));
        } else {
            setProgressColor(DEFAULT_PROGRESS_COLOR);
        }
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "progress", progress).setDuration(0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mOnAnimationEndListener != null) {
                    mOnAnimationEndListener.onAnimationEnd();
                }
            }
        });
        animator.start();
    }

    /**
     * 设置当前进度条的进度
     *
     * @param progress 目标进度
     * @param duration 动画时长
     */
    public void setCurProgress(final int progress, long duration) {
        if (mHaveChangeColor) {
            setProgressColor(getProgressChangeColor((float) progress / (float) mMax));
        } else {
            setProgressColor(DEFAULT_PROGRESS_COLOR);
        }
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "progress", progress).setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mOnAnimationEndListener != null) {
                    mOnAnimationEndListener.onAnimationEnd();
                }
            }
        });
        animator.start();
    }

    private int getProgressChangeColor(float percentage) {
        if (0 < percentage && percentage <= 0.1) {
            return Color.parseColor(changeColor[0]);
        } else if (0.1 < percentage && percentage <= 0.2) {
            return Color.parseColor(changeColor[1]);
        } else if (0.2 < percentage && percentage <= 0.3) {
            return Color.parseColor(changeColor[2]);
        } else if (0.3 < percentage && percentage <= 0.4) {
            return Color.parseColor(changeColor[3]);
        } else if (0.4 < percentage && percentage <= 0.5) {
            return Color.parseColor(changeColor[4]);
        } else if (0.5 < percentage && percentage <= 0.6) {
            return Color.parseColor(changeColor[5]);
        } else if (0.6 < percentage && percentage <= 0.7) {
            return Color.parseColor(changeColor[6]);
        } else if (0.7 < percentage && percentage <= 0.8) {
            return Color.parseColor(changeColor[7]);
        } else if (0.8 < percentage && percentage <= 0.9) {
            return Color.parseColor(changeColor[8]);
        } else if (0.9 < percentage && percentage <= 1.0) {
            return Color.parseColor(changeColor[9]);
        }
        return DEFAULT_PROGRESS_COLOR;
    }

    /**
     * 设置ProgressBar的颜色
     *
     * @param color
     */
    public void setProgressColor(int color) {
        mProgressColor = color;
        invalidateView();
    }

    /**
     * 设置是否显示当前进度
     *
     * @param isShowDesc true:显示
     */
    public void setIsShowDesc(boolean isShowDesc) {
        mIsShowDesc = isShowDesc;
        invalidateView();
    }

    /**
     * 设置是否需要颜色渐变
     */
    public void setHaveChangeColor(boolean haveChangeColor) {
        mHaveChangeColor = haveChangeColor;
        invalidateView();
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public interface OnFinishedListener {
        void onFinish();
    }

    public interface OnAnimationEndListener {
        void onAnimationEnd();
    }
}
