package com.silladus.stepindicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by silladus on 2017/8/24/0024.
 * GitHub: https://github.com/silladus
 * Description:
 */

public abstract class StepIndicatorView extends View {
    private int mOrientation;
    private float mCompletedLineWidth;
    private float mCircleRadius;
    private float mCenterAxis;//圆心轴
    private List<Float> mCircleCenterPointPositionList;

    private float mLinePositionFromBorder;// the position from top, if HORIZONTAL, it is y, else is x.
    private float mLinePositionToBorder;// the position to bottom, same to mLinePositionFromBorder.

    private List<Step> mSteps;
    private int mStepNum = 0;
    private float mLineLength;//流程连线的长度  definition the spacing between the two circles
    private float defaultLineLength = 80f;

    private Path mPath;
    private Paint mUnCompletedPaint;
    private Paint mCompletedPaint;
    private int mUnCompletedLineColor = Color.parseColor("#A3E0D9");
    private int mCompletedLineColor = Color.WHITE;
    protected int mCompletingPosition;

    private float firstPointToStart, lastPointToEnd;
    //--------------------------------------------图片-------------------------------------------------
    private Drawable mCompleteIcon;
    private Drawable mAttentionIcon;
    private Drawable mDefaultIcon;

    // 在图标上画步数
    private TextPaint mIconTextPaint;
    private boolean isDrawIconStepText;

    public StepIndicatorView(Context context) {
        this(context, null);
    }

    public StepIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepIndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mSteps = new ArrayList<>();
        mCircleCenterPointPositionList = new ArrayList<>();

        mPath = new Path();

        mUnCompletedPaint = new Paint();
        setPaint(mUnCompletedPaint, mUnCompletedLineColor, Paint.Style.STROKE);
        PathEffect mEffects = new DashPathEffect(new float[]{8, 8, 8, 8}, 1);
        mUnCompletedPaint.setPathEffect(mEffects);

        mCompletedPaint = new Paint();
        setPaint(mCompletedPaint, mCompletedLineColor, Paint.Style.FILL);

        setCompletedLineWidth(2);
        setCircleRadius(11);
    }

    public void setPaint(Paint paint, @ColorInt int color, Paint.Style style) {
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(style);
        paint.setStrokeWidth(2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        float maxItemWidth = 0, maxItemHeight = 0;
        for (Step step : mSteps) {
            maxItemWidth = Math.max(maxItemWidth, step.width);
            maxItemHeight = Math.max(maxItemHeight, step.height);
        }
        float circleLines = Math.min(1, mStepNum);
        if (mOrientation == LinearLayout.HORIZONTAL) {
            mLineLength = Math.max(maxItemWidth, defaultLineLength);
            float lineSpace = mLineLength * (mStepNum - 1);
            firstPointToStart = mSteps.get(0).width / 2f > mCircleRadius ? mSteps.get(0).width / 2f : mCircleRadius;
            firstPointToStart += getPaddingStart();
            lastPointToEnd = mSteps.get(mSteps.size() - 1).width / 2f > mCircleRadius ? mSteps.get(mSteps.size() - 1).width / 2f : mCircleRadius;
            lastPointToEnd += getPaddingEnd();
            width = Math.round(firstPointToStart + lineSpace + lastPointToEnd);
            height = Math.round(getPaddingTop() + mCircleRadius * 2 * circleLines + getPaddingBottom());
        } else {
            mLineLength = Math.max(maxItemHeight, defaultLineLength);
            float lineSpace = mLineLength * (mStepNum - 1);
            if (mSteps.get(0).lineHeight > 2 * mCircleRadius) {
                firstPointToStart = mSteps.get(0).lineHeight / 2f;
                lastPointToEnd = mSteps.get(mSteps.size() - 1).height - mSteps.get(mSteps.size() - 1).lineHeight / 2f;
            } else {
                firstPointToStart = mCircleRadius;
                if (mSteps.get(mSteps.size() - 1).height > mCircleRadius * 2) {
                    lastPointToEnd = mSteps.get(mSteps.size() - 1).height - mSteps.get(0).lineHeight / 2f;
                } else {
                    lastPointToEnd = mCircleRadius;
                }
            }
            firstPointToStart += getPaddingTop();
            lastPointToEnd += getPaddingBottom();
            width = Math.round(getPaddingStart() + mCircleRadius * 2 * circleLines + getPaddingEnd());
            height = Math.round(firstPointToStart + lineSpace + lastPointToEnd);
        }
        int mWidth = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
        int mHeight = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (mOrientation == LinearLayout.HORIZONTAL) {
            //获取中间的高度,目的是为了让该view绘制的线和圆在该view垂直居中   get view centerY，keep current stepView center vertical
            mCenterAxis = 0.5f * h;
        } else {
            mCenterAxis = 0.5f * w;
        }
        //获取上方Y的位置，方便画矩形左上的Y位置
        mLinePositionFromBorder = mCenterAxis - mCompletedLineWidth / 2f;
        //获取右下方Y的位置，方便画矩形右下的Y位置
        mLinePositionToBorder = mCenterAxis + mCompletedLineWidth / 2f;
        mCircleCenterPointPositionList.clear();
        for (int i = 0; i < mStepNum; i++) {
            mCircleCenterPointPositionList.add(firstPointToStart + i * mLineLength);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mUnCompletedPaint.setColor(mUnCompletedLineColor);
        mCompletedPaint.setColor(mCompletedLineColor);
        drawLines(canvas);
        drawIcon(canvas);
        drawText(canvas);
        drawIconStepText(canvas);
    }

    /**
     * draw the step's text
     */
    public abstract void drawText(Canvas canvas);

    private void drawLines(Canvas canvas) {
        for (int i = 0; i < mCircleCenterPointPositionList.size() - 1; i++) {
            //prev position
            float from = mCircleCenterPointPositionList.get(i);
            //next position
            float to = mCircleCenterPointPositionList.get(i + 1);

            if (i <= mCompletingPosition && mSteps.get(0).state != Step.UNDO) {
                if (mOrientation == LinearLayout.HORIZONTAL) {
                    canvas.drawRect(from, mLinePositionFromBorder, to, mLinePositionToBorder, mCompletedPaint);
                } else {
                    canvas.drawRect(mLinePositionFromBorder, from, mLinePositionToBorder, to, mCompletedPaint);
                }
            } else {
                if (mOrientation == LinearLayout.HORIZONTAL) {
                    if (mUnCompletedPaint.getPathEffect() instanceof DashPathEffect) {
                        mPath.moveTo(from, mCenterAxis);
                        mPath.lineTo(to, mCenterAxis);
                        canvas.drawPath(mPath, mUnCompletedPaint);
                    } else {
                        canvas.drawRect(from, mLinePositionFromBorder, to, mLinePositionToBorder, mUnCompletedPaint);
                    }
                } else {
                    if (mUnCompletedPaint.getPathEffect() instanceof DashPathEffect) {
                        mPath.moveTo(mCenterAxis, from);
                        mPath.lineTo(mCenterAxis, to);
                        canvas.drawPath(mPath, mUnCompletedPaint);
                    } else {
                        canvas.drawRect(mLinePositionFromBorder, from, mLinePositionToBorder, to, mUnCompletedPaint);
                    }
                }
            }
        }
    }

    private void drawIcon(Canvas canvas) {
        if (mCompleteIcon == null || mAttentionIcon == null || mDefaultIcon == null) {
            mCompletedPaint.setColor(Color.parseColor("#2ca146"));
            mUnCompletedPaint.setStyle(Paint.Style.FILL);
            drawFromPaint(canvas);
        } else {
            drawFromDrawable(canvas);
        }
    }

    private void drawFromDrawable(Canvas canvas) {
        for (int i = 0; i < mCircleCenterPointPositionList.size(); i++) {
            float currentComplectedXPosition = mCircleCenterPointPositionList.get(i);
            Rect mRect;
            if (mOrientation == LinearLayout.HORIZONTAL) {
                mRect = new Rect((int) (currentComplectedXPosition - mCircleRadius), (int) (mCenterAxis - mCircleRadius), (int) (currentComplectedXPosition + mCircleRadius), (int) (mCenterAxis + mCircleRadius));
            } else {
                mRect = new Rect((int) (mCenterAxis - mCircleRadius), (int) (currentComplectedXPosition - mCircleRadius), (int) (mCenterAxis + mCircleRadius), (int) (currentComplectedXPosition + mCircleRadius));
            }
            Step stepsBean = mSteps.get(i);
            if (stepsBean.state == Step.COMPLETED) {
                mCompleteIcon.setBounds(mRect);
                mCompleteIcon.draw(canvas);
            } else if (stepsBean.state == Step.CURRENT) {
                mAttentionIcon.setBounds(mRect);
                mAttentionIcon.draw(canvas);
            } else {
                mDefaultIcon.setBounds(mRect);
                mDefaultIcon.draw(canvas);
            }
        }
    }

    private void drawFromPaint(Canvas canvas) {
        for (int i = 0; i < mCircleCenterPointPositionList.size(); i++) {
            float currentComplectedXPosition = mCircleCenterPointPositionList.get(i);
            float cx, cy;
            if (mOrientation == LinearLayout.HORIZONTAL) {
                cx = currentComplectedXPosition;
                cy = mCenterAxis;
            } else {
                cx = mCenterAxis;
                cy = currentComplectedXPosition;
            }
            Step stepsBean = mSteps.get(i);
            if (stepsBean.state == Step.COMPLETED) {
                canvas.drawCircle(cx, cy, mCircleRadius, mCompletedPaint);
            } else if (stepsBean.state == Step.CURRENT) {
                canvas.drawCircle(cx, cy, mCircleRadius, mUnCompletedPaint);
            } else {
                canvas.drawCircle(cx, cy, mCircleRadius, mUnCompletedPaint);
            }
        }
    }

    public void setDrawIconStepText(boolean drawIconStepText) {
        isDrawIconStepText = drawIconStepText;
    }

    private void drawIconStepText(Canvas canvas) {

        if (!isDrawIconStepText) {
            return;
        }

        if (mIconTextPaint == null) {
            mIconTextPaint = new TextPaint();
            mIconTextPaint.setAntiAlias(true);
            mIconTextPaint.setColor(Color.WHITE);
            mIconTextPaint.setTextSize(mCircleRadius);
            mIconTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        for (int i = 0; i < mCircleCenterPointPositionList.size(); i++) {
            float currentComplectedXPosition = mCircleCenterPointPositionList.get(i);
            if (mOrientation == LinearLayout.HORIZONTAL) {
                canvas.drawText(String.valueOf(i + 1), currentComplectedXPosition - mCenterAxis / 4, mCenterAxis + mCenterAxis / 3, mIconTextPaint);
            } else {
                canvas.drawText(String.valueOf(i + 1), mCenterAxis - mCenterAxis / 4, currentComplectedXPosition + mCenterAxis / 3, mIconTextPaint);
            }

        }
    }

    /**
     * Should the layout be a column or a row.
     *
     * @param orientation Pass {LinearLayout.HORIZONTAL} or {LinearLayout.VERTICAL}.
     */
    public void setOrientation(@LinearLayoutCompat.OrientationMode int orientation) {
        this.mOrientation = orientation;
        requestLayout();
    }

    public void setCircleRadius(float mCircleRadius) {
        this.mCircleRadius = mCircleRadius;
    }

    public float getCircleRadius() {
        return mCircleRadius;
    }

    public List<Float> getCircleCenterPointPositionList() {
        return mCircleCenterPointPositionList;
    }

    public void setStep(List<Step> steps) {
        if (mSteps != null) {
            this.mSteps = steps;
            mStepNum = mSteps.size();
            for (int i = 0; i < mStepNum; i++) {
                Step step = mSteps.get(i);
                if (step.state == Step.COMPLETED) {
                    mCompletingPosition = i;
                }
            }
            requestLayout();
        }
    }

    /**
     * @param unCompletedLineColor line
     * @param completedLineColor   line
     */
    public void setLine(int unCompletedLineColor, Paint.Style unCompletedLineStyle, int completedLineColor, Paint.Style completedLineStyle, float defaultLineLength) {
        this.mUnCompletedLineColor = unCompletedLineColor;
        this.mCompletedLineColor = completedLineColor;

        if (unCompletedLineStyle != Paint.Style.STROKE) {
            mUnCompletedPaint.setPathEffect(null);
        }
        setPaint(mUnCompletedPaint, unCompletedLineColor, unCompletedLineStyle);
        setPaint(mCompletedPaint, mCompletedLineColor, completedLineStyle);

        this.defaultLineLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, defaultLineLength, getResources().getDisplayMetrics());
    }

    /**
     * all drawable could be null
     */
    public void setStepDrawables(@Nullable Drawable defaultIcon, @Nullable Drawable completeIcon, @Nullable Drawable attentionIcon) {
        this.mDefaultIcon = defaultIcon;
        this.mCompleteIcon = completeIcon;
        this.mAttentionIcon = attentionIcon;
    }

    /**
     *
     * @param mCompletedLineWidth dp
     */
    public void setCompletedLineWidth(float mCompletedLineWidth) {
        this.mCompletedLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mCompletedLineWidth, getResources().getDisplayMetrics());
    }

    //---------------------------------------------step----------------------------------------------

    public static class Step {
        public static final int UNDO = -1;
        public static final int CURRENT = 0;
        public static final int COMPLETED = 1;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({UNDO, CURRENT, COMPLETED})
        private @interface State {
        }

        int state;
        String text;
        float width;
        float height;// if VERTICAL, the value should be use to make sure the view measure width.
        int lineHeight;// every line height contain lingSpace(top and bottom)
//        int textLineSpace;
//        int lines;//step item line count

        /**
         * @param state {@link #UNDO}, {@link #CURRENT} or {@link #COMPLETED}
         * @param text  step's text
         */
        public Step(@State int state, String text) {
            this.state = state;
            this.text = text;
        }

        /**
         * @param state {@link #UNDO}, {@link #CURRENT} or {@link #COMPLETED}
         */
        public void setState(@State int state) {
            this.state = state;
        }
    }
}
