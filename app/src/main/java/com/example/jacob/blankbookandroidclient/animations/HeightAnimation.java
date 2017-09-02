package com.example.jacob.blankbookandroidclient.animations;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class HeightAnimation extends Animation {
    final private View view;
    final private int startHeight;
    final private int endHeight;

    public HeightAnimation(View view, int endHeight) {
        this.view = view;
        this.startHeight = view.getHeight();
        this.endHeight = endHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = (int)(startHeight + (endHeight - startHeight) * interpolatedTime);
        view.setLayoutParams(params);
    }
}
