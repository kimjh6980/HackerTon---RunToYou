package com.microsoft.projectoxford.face.samples.ui;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by Kim Jin Hyuk on 2017-11-10.
 */

class MyCameraSurface extends SurfaceView implements SurfaceHolder.Callback{
    SurfaceHolder mHolder;
    Camera mCamera;

    public MyCameraSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        try{
          //  mCamera.setDisplayOrientation(90);  // 프리뷰가 회전을 하다보니 일단 돌려놓음.(세로모드 기준)
            mCamera.setPreviewDisplay(mHolder);

        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> arSize = params.getSupportedPreviewSizes();
        if(arSize == null){
            params.setPreviewSize(width, height);
        }else{
            int diff = 10000;
            Camera.Size opti = null;
            for(Camera.Size s : arSize){
                if(Math.abs(s.height - height) < diff){
                    diff = Math.abs(s.height - height);
                    opti = s;
                }
            }
            params.setPreviewSize(opti.width, opti.height);
        }
        mCamera.setParameters(params);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.e("TAG", "Distroy()");
        }
    }
}