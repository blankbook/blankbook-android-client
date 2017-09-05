package com.example.jacob.blankbookandroidclient.animations;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ElevationAnimation extends Animation {
    final private View view;
    final private float startElevation;
    final private float endElevation;

    public ElevationAnimation(View view, float endElevation) {
        this.view = view;
        this.startElevation = view.getElevation();
        this.endElevation = endElevation;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        final float elevation = startElevation + interpolatedTime * (endElevation - startElevation);
        view.setElevation(elevation);
    }
}
