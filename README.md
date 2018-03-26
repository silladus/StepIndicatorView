# StepIndicatorView
一个流程显示控件

效果预览，包含横向和竖向两种模式
---------------------------------  

![image](https://github.com/silladus/StepIndicatorView/blob/master/StepIndicatorView/img/normal.png)

可以设置字体大小、颜色、风格等
连线的样式也可以设置
就连图标也可以设置

示例代码
-------
```java
        
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

        stepView.setLine(Color.parseColor("#A3E0D9"), Paint.Style.STROKE, Color.parseColor("#2ca146"), Paint.Style.FILL, 30)
                .setCompletedLineWidth(1)
                .setTextSize(12)
		.setTextColor(0xFF474747, 0xFF474747)// 设置文字内容的颜色
//                .setCircleRadius(10)// 设置绘制图标的大小，传入Drawable的话不需要设置
                .setLineSpacingExtra(5)// 设置行间距
                .setDrawIconStepText(true)// 在图标内绘制文字
                .setStepDrawables(getResources().getDrawable(R.mipmap.icon_step_oval), // 默认状态图标
				  getResources().getDrawable(R.mipmap.icon_step_oval), // 完成状态图标
				  getResources().getDrawable(R.mipmap.icon_step_oval)) // 正在处理状态图标
                .setTextPaddingToStepIndicatorView(10) // 内容文字与图标的间距
                .setStep(steps, LinearLayout.VERTICAL); // 传入的数据和显示方向
    }