package cn.leither.btsp.view;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.renderscript.Sampler;
import android.view.View;

public class Animators {

    public static ValueAnimator scaleValueAnimator(final View v){
        //1.设置目标属性名及属性变化的初始值和结束值
        PropertyValuesHolder mPropertyValuesHolderScaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f,0.6f);
        PropertyValuesHolder mPropertyValuesHolderScaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f,0.6f);
        ValueAnimator mAnimator = ValueAnimator.ofPropertyValuesHolder(mPropertyValuesHolderScaleX,mPropertyValuesHolderScaleY);
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 3.根据属性名获取属性变化的值分别为ImageView目标对象设置X和Y轴的缩放值
                float animatorValueScaleX =  (float) animation.getAnimatedValue("scaleX");
                float animatorValueScaleY = (float) animation.getAnimatedValue("scaleY");
                v.setScaleX(animatorValueScaleX);
                v.setScaleY(animatorValueScaleY);

            }
        });
        //4.为ValueAnimator设置自定义的Interpolator
        mAnimator.setInterpolator(new CustomInterpolator());
        //5.设置动画的持续时间、是否重复及重复次数等属性
        mAnimator.setDuration(1000);
        mAnimator.setRepeatCount(-1);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        //6.为ValueAnimator设置目标对象并开始执行动画
        mAnimator.setTarget(v);
        return mAnimator;
    }
}
