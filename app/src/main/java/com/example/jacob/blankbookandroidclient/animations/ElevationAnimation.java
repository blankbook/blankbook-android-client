package com.example.jacob.blankbookandroidclient.animations;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ElevationAnimation extends Animation {
    private View view;
    private float startElevation;
    private float endElevation;

    public ElevationAnimation(View view, float startElevation, float endElevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.view = view;
            this.startElevation = startElevation;
            this.endElevation = endElevation;
        }
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final float elevation = startElevation + interpolatedTime * (endElevation - startElevation);
            view.setElevation(elevation);
        }
    }
}
