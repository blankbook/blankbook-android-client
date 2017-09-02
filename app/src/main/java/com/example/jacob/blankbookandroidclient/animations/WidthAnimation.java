package com.example.jacob.blankbookandroidclient.animations;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class WidthAnimation extends Animation {
    final private View view;
    final private int startWidth;
    final private int endWidth;

    public WidthAnimation(View view, int endWidth) {
        this.view = view;
        this.startWidth = view.getWidth();
        this.endWidth = endWidth;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int)(startWidth + (endWidth - startWidth) * interpolatedTime);
        view.setLayoutParams(params);
    }
}
