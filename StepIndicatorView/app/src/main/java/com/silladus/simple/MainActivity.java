package com.silladus.simple;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.silladus.stepindicator.StepIndicatorView;
import com.silladus.stepindicator.StepView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showStepViewHorizontal();
        showStepViewVertical();
        findViewById(R.id.start_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, InScrollViewActivity.class));
            }
        });
    }

    private void showStepViewHorizontal() {
        StepView stepView = (StepView) findViewById(R.id.mStep);
        List<StepIndicatorView.Step> steps = new ArrayList<>();
        int[] state = {StepIndicatorView.Step.COMPLETED,
                StepIndicatorView.Step.COMPLETED,
                StepIndicatorView.Step.CURRENT,
                StepIndicatorView.Step.UNDO,
                StepIndicatorView.Step.UNDO,
                StepIndicatorView.Step.UNDO};
        String[] text = new String[]{"接单", "打包", "出发", "送单", "完成", "支付"};
        for (int i = 0; i < state.length; i++) {
            steps.add(new StepIndicatorView.Step(state[i], text[i]));
        }
        stepView.setStep(steps, LinearLayout.HORIZONTAL)
                .setStepDrawables(ContextCompat.getDrawable(this, R.drawable.ic_check_order_progress_normal),
                        ContextCompat.getDrawable(this, R.drawable.ic_checked_order_progress),
                        ContextCompat.getDrawable(this, R.drawable.ic_check_order_progress_normal))
                .setLine(Color.GRAY, Paint.Style.FILL, Color.parseColor("#2ca146"), Paint.Style.FILL, 40)
//                .setDrawIconStepText(true)
                .setTextColor(Color.WHITE, Color.WHITE);
    }

    private void showStepViewVertical() {
        StepView stepView = (StepView) findViewById(R.id.mStep1);
        List<StepIndicatorView.Step> steps = new ArrayList<>();
        int[] state = {StepIndicatorView.Step.COMPLETED,
                StepIndicatorView.Step.COMPLETED,
                StepIndicatorView.Step.CURRENT,
                StepIndicatorView.Step.UNDO};
        String[] text = new String[]{
                "支付订单",
                "出门寄件",
                "确定收件（收到资料后对资料进行审核）",
                "处理完成 处理完成 处理完成 处理完成 处理完成 处理完成 处理完成 处理完成 处理完成 处理完成 处理完成"};
        for (int i = 0; i < state.length; i++) {
            steps.add(new StepIndicatorView.Step(state[i], text[i]));
        }
        stepView.setLineSpacingExtra(10)
                .setStep(steps, LinearLayout.VERTICAL)
                .setLine(Color.parseColor("#A3E0D9"), Paint.Style.STROKE, Color.parseColor("#2ca146"), Paint.Style.FILL, 30)
                .setCompletedLineWidth(1)
                .setTextSize(12)
                .setDrawIconStepText(true)
                .setCircleRadius(10)
        ;
    }

}
