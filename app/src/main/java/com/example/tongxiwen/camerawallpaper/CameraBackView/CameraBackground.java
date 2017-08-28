package com.example.tongxiwen.camerawallpaper.CameraBackView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by tong.xiwen on 2017/8/25.
 */
public class CameraBackground extends FrameLayout {

    private CameraView mCameraView;

    private int mWidth;
    private int mHeight;

    public CameraBackground(Context context) {
        this(context, null, 0);
    }
    public CameraBackground(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CameraBackground(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        mCameraView = new CameraView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mCameraView.setLayoutParams(params);
        addView(mCameraView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        super.measureChildren(widthMeasureSpec,heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        super.onLayout(b,i,i1,i2,i3);
        int right = mCameraView.getMeasuredWidth();
        int bottom = mCameraView.getMeasuredHeight();
        mCameraView.layout(0,0,right, bottom);
    }
}
