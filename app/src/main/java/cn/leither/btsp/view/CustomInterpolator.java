package cn.leither.btsp.view;

import android.animation.TimeInterpolator;

/**
 * Created by lvqiang on 17-9-19.
 */

public class CustomInterpolator implements TimeInterpolator {
    @Override
    public float getInterpolation(float input) {
        input *= 0.9f;
        return input * input;
    }
}
