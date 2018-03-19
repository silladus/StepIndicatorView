package com.silladus.stepindicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silladus on 2017/8/26/0026.
 * GitHub: https://github.com/silladus
 * Description:
 */

public class StepView extends LinearLayout {
    private RelativeLayout mTextContainer;
    private StepIndicatorView mStepIndicatorView;
    private List<StepIndicatorView.Step> mSteps;
    private int mUnCompletedTextColor = Color.DKGRAY;
    private int mCompletedTextColor = Color.DKGRAY;
    private int mCompletedTextStyle = Typeface.NORMAL;
    private int mTextSize = 14;
    private int baseHeightValue = 40;
    private List<TextView> mTextViews;
    private int mTextPaddingToStepIndicatorView;

    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mStepIndicatorView = new StepIndicatorView(getContext()) {
            @Override
            public void drawText(Canvas canvas) {
                initText();
            }
        };
        setCompletedLineWidth(0.05f * baseHeightValue);
        setCircleRadius(0.28f * baseHeightValue);
        setTextPaddingToStepIndicatorView(6);
        mTextContainer = new RelativeLayout(getContext());
        setOrientation(HORIZONTAL);
    }

    /**
     *
     * @see StepIndicatorView
     */
    public StepView setStep(List<StepIndicatorView.Step> steps, int orientation) {
        if (steps != null) {
            mSteps = steps;
            setStep(steps);
            setOrientation(orientation);
        }
        return this;
    }

    /**
     * Should do measure the step's text space before to set step to the {#StepIndicatorView}
     *
     * @see StepIndicatorView
     */
    private void setStep(List<StepIndicatorView.Step> steps) {
        if (steps != null) {
            buildSteps2TextView();
            mStepIndicatorView.setStep(mSteps);
        }
    }

    private float mLineSpacingExtra = 5;

    public StepView setLineSpacingExtra(float mLineSpacingExtra) {
        this.mLineSpacingExtra = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLineSpacingExtra, getResources().getDisplayMetrics());
        return this;
    }

    /**
     * to fit the content width and height, should measure all text need space
     */
    private void buildSteps2TextView() {
        mTextViews = new ArrayList<>();
        for (StepIndicatorView.Step step : mSteps) {
            TextView mTextView = new TextView(getContext());
            mTextView.setLineSpacing(mLineSpacingExtra, 1f);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
            mTextView.setText(step.text);
            int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            mTextView.measure(spec, spec);
            step.width = mTextView.getMeasuredWidth();
            int mLayoutWidth = getLayoutParams().width < 0 ? getResources().getDisplayMetrics().widthPixels : getLayoutParams().width;
            float maxWidth = mLayoutWidth - 10 - mStepIndicatorView.getCircleRadius() * 2;
            int lines = 1;
            if (step.width > maxWidth) {
                lines = (int) Math.ceil(mTextView.getMeasuredWidth() / maxWidth);
            }
            float textLineSpace = 5 * mTextView.getLineSpacingMultiplier();// 行间距默认5，行间倍数默认1.0
            step.lineHeight = mTextView.getMeasuredHeight();
            step.height = (step.lineHeight - textLineSpace) * lines + textLineSpace;
            mTextViews.add(mTextView);
        }
    }

    /**
     * layout the TextViews to draw the text part
     */
    public void initText() {
        if (mTextContainer != null) {
            mTextContainer.removeAllViews();
            List<Float> mCircleCenterPointPositionList = mStepIndicatorView.getCircleCenterPointPositionList();
            if (mSteps != null && mCircleCenterPointPositionList != null && mCircleCenterPointPositionList.size() > 0) {
                for (int i = 0; i < mSteps.size(); i++) {
                    TextView mTextView = mTextViews.get(i);
                    if (getOrientation() == HORIZONTAL) {
                        float measuredWidth = mSteps.get(i).width;
                        mTextView.setX(mCircleCenterPointPositionList.get(i) - measuredWidth / 2f);
                        mTextView.setPadding(0, mTextPaddingToStepIndicatorView, 0, 0);
                    } else {
                        mTextView.setY(mCircleCenterPointPositionList.get(i) - mSteps.get(i).lineHeight / 2f);
                        mTextView.setPadding(mTextPaddingToStepIndicatorView, 0, 0, 0);
                    }
                    mTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    if (i <= mStepIndicatorView.mCompletingPosition) {
                        mTextView.setTypeface(null, mCompletedTextStyle);
                        mTextView.setTextColor(mCompletedTextColor);
                    } else {
                        mTextView.setTextColor(mUnCompletedTextColor);
                    }
                    mTextContainer.addView(mTextView);
                }
            }
        }
    }

    //---------------------------------------------orientation---------------------------------------

    /**
     * Should the layout be a column or a row. call after set step. use setStep to instead.
     *
     * @param orientation Pass {@link #HORIZONTAL} or {@link #VERTICAL}.
     *                    if StepView is {@link #HORIZONTAL}, LinerLayout is {@link #VERTICAL}.
     */
    @Override
    public void setOrientation(int orientation) {
        if (orientation == HORIZONTAL) {
            super.setOrientation(VERTICAL);
        } else {
            super.setOrientation(HORIZONTAL);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mTextContainer.setLayoutParams(lp);
        }
        mStepIndicatorView.setOrientation(getOrientation());
        setStep(mSteps);
        removeAllViews();
        addView(mStepIndicatorView);
        addView(mTextContainer);
    }

    /**
     * Returns the StepView current orientation.
     *
     * @return either {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    @Override
    public int getOrientation() {
        return super.getOrientation() == HORIZONTAL ? VERTICAL : HORIZONTAL;
    }

    //---------------------------------------------setter---------------------------------------------

    public StepView setCircleRadius(float mCircleRadius) {
        mStepIndicatorView.setCircleRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mCircleRadius, getResources().getDisplayMetrics()));
        return this;
    }

    /**
     * @param unCompletedLineColor  line
     * @param completedLineColor    line
     * @param mLineLength dp
     */
    public StepView setLine(int unCompletedLineColor, Paint.Style unCompletedLineStyle, int completedLineColor, Paint.Style completedLineStyle, float mLineLength) {
        mStepIndicatorView.setLine(unCompletedLineColor, unCompletedLineStyle, completedLineColor, completedLineStyle, mLineLength);
        return this;
    }

    /**
     *
     * @param mCompletedLineWidth dp
     */
    public StepView setCompletedLineWidth(float mCompletedLineWidth) {
        mStepIndicatorView.setCompletedLineWidth(mCompletedLineWidth);
        return this;
    }

    /**
     *
     * @param unComplectedTextColor {@link #mUnCompletedTextColor}
     * @param complectedTextColor   {@link #mCompletedTextColor}
     */
    public StepView setTextColor(int unComplectedTextColor, int complectedTextColor) {
        this.mUnCompletedTextColor = unComplectedTextColor;
        this.mCompletedTextColor = complectedTextColor;
        return this;
    }

    /**
     * @param textSize sp
     */
    public StepView setTextSize(int textSize) {
        if (textSize > 0) {
            mTextSize = textSize;
            if (mSteps != null) {
                setStep(mSteps, getOrientation());
            }
        }
        return this;
    }

    /**
     * make setting text possible if need
     * @param style Pass Typeface style
     * @see Typeface
     */
    public StepView setCompletedTextTypeface(int style) {
        this.mCompletedTextStyle = style;
        return this;
    }

    /**
     * all drawable could be null, all not null else it will be all null
     */
    public StepView setStepDrawables(@Nullable Drawable defaultIcon, @Nullable Drawable completeIcon, @Nullable Drawable attentionIcon) {
        mStepIndicatorView.setStepDrawables(defaultIcon, completeIcon, attentionIcon);
        return this;
    }

    /**
     * text to StepIndicatorView's space
     * @param mTextPaddingToStepIndicatorView dp
     * {@link #initText}
     */
    public StepView setTextPaddingToStepIndicatorView(float mTextPaddingToStepIndicatorView) {
        this.mTextPaddingToStepIndicatorView = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mTextPaddingToStepIndicatorView, getResources().getDisplayMetrics()));
        return this;
    }

    /**
     * draw step num to Icon.
     *
     * @param drawIconStepText default is false.
     */
    public StepView setDrawIconStepText(boolean drawIconStepText) {
        mStepIndicatorView.setDrawIconStepText(drawIconStepText);
        return this;
    }

}
