package com.example.tongxiwen.camerawallpaper.CameraBackView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by tong.xiwen on 2017/8/24.
 */
public class CameraView extends SurfaceView {

    private CameraDevice mDevice;
    private CameraManager mManager;
    private SurfaceHolder mHolder;

    private ImageView ivShow;
    private ImageReader mReader;

    private Handler mainHandler;
    private Handler childHandler;

    private CameraStateCallBack cameraStateCallBack;

    private String[] cameraIds;
    private String curCamera;

    public CameraView(Context context) {
        super(context, null, 0);
        initView();
    }

    /*主要方法************************************************************************/

    /**
     * 测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Size size = getSize();

        int width, w;
        int height, h;
        if (size == null) {
            return;
        }


        if (isLandscape()) {
            w = size.getWidth();
            h = size.getHeight();

            width = MeasureSpec.getSize(widthMeasureSpec);
            height = (int) ((float) width * ((float) h / (float) w));
        } else {
            w = size.getHeight();
            h = size.getWidth();
            height = MeasureSpec.getSize(heightMeasureSpec);
            width = (int) ((float) height * ((float) w / (float) h));
        }
        Log.d("获取的相机尺寸w：h", String.valueOf(w)+ "：" + String.valueOf(h));
        Log.d("最终画面尺寸w:h", String.valueOf(width) + "：" + String.valueOf(height));
        setMeasuredDimension(width, height);
    }

    /**
     * 初始化
     */
    private void initView() {
        mHolder = getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.addCallback(new SurfaceCallBack());

        cameraStateCallBack = new CameraStateCallBack();

        HandlerThread mThreadHandler = new HandlerThread("CAMERA2");
        mThreadHandler.start();

        childHandler = new Handler(mThreadHandler.getLooper());
        mainHandler = new Handler(getContext().getMainLooper());
    }

    /**
     * 初始化Camera2
     */
    private void initCamera2() {

        mManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        Size size = getSize();
        if (size == null){
            size = new Size(1080, 1920);
        }
        mReader = ImageReader.newInstance(size.getWidth() / 10, size.getHeight() / 10, ImageFormat.JPEG, 1);
        mReader.setOnImageAvailableListener(new OnImageAvailableListener(), mainHandler);

        try {
            cameraIds = mManager.getCameraIdList();
            if (curCamera == null) {
                curCamera = cameraIds[0];
            }
            mManager.openCamera(curCamera, cameraStateCallBack, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /*各种内部方法****************************************************************************/

    /**
     * 获取屏幕状态
     */
    private boolean isLandscape() {
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        }
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return false;
        }
        return false;
    }

    /**
     * 开启预览
     */
    private void takePreview() {
        try {
            // 创建负责预览请求和拍照请求的CameraCaptureSession
            mDevice.createCaptureSession(Arrays.asList(mHolder.getSurface(), mReader.getSurface())
                    , new SessionStateCallBack(), childHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Size
     */
    private Size getSize() {
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (curCamera == null) {
                curCamera = cameraIds[0];
            }
            CameraCharacteristics chara = manager.getCameraCharacteristics(curCamera);

            //支持的STREAM CONFIGURATION
            StreamConfigurationMap map = chara.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            //显示的size
            return map.getOutputSizes(SurfaceTexture.class)[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*各种callback****************************************************************************/


    /**
     * SurfaceHolder的Callback
     * 用于回调SurfaceView的状态
     */
    private class SurfaceCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            initCamera2();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            mDevice.close();
            mDevice = null;
        }
    }

    /**
     * 摄像头控制回调
     */
    private class CameraStateCallBack extends CameraDevice.StateCallback {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mDevice = cameraDevice;
            takePreview();
        }

        /**
         * 移除摄像头
         */
        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            if (mDevice != null) {
                cameraDevice.close();
                mDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Toast.makeText(getContext(), "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 照片预览显示ImageView
     */
    private class OnImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            // todo 这一段完全没看懂
            mDevice.close();
            setVisibility(View.GONE);
            ivShow.setVisibility(View.VISIBLE);
            Image image = imageReader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            //由缓冲区存入字节数组
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                ivShow.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * 相机阅览请求和拍照请求的回调
     */
    private class SessionStateCallBack extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {

            try {
                // 预览需求的CaptureRequest
                final CaptureRequest.Builder requestBuilder;
                requestBuilder = mDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                // 将CaptureRequest指向SurfaceView
                requestBuilder.addTarget(mHolder.getSurface());
                if (mDevice == null) {
                    return;
                }
                // 若摄像头已就位
                CameraCaptureSession mSession = cameraCaptureSession;

                // 自动对焦
                requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // 显示阅览
                CaptureRequest previewRequest = requestBuilder.build();
                mSession.setRepeatingRequest(previewRequest, null, childHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Toast.makeText(getContext(), "配置失败", Toast.LENGTH_SHORT).show();
        }
    }


    /*各种外部方法****************************************************************************/


    /**
     * 设置显示照片用ImageView
     *
     * @param imageView
     */
    protected void setIvShow(ImageView imageView) {
        this.ivShow = imageView;
    }

    /**
     * 获取全部相机id
     */
    protected String[] getCameraIds() {

        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        try {
            return manager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 设置使用的相机
     * fixme 无法正常切换前置摄像头，会造成系统死循环
     * fixme ↑我的mi5s已经因此重启3次了
     */
    protected void setCurCamera(String cameraId) {
        curCamera = cameraId;
        pausePreview();
        startPreview();
    }

    /**
     * 获取当前相机
     */
    protected String getCurCamera() {
        return curCamera;
    }

    /**
     * 开始预览
     */
    protected void startPreview() {
        try {
            mManager.openCamera(curCamera, cameraStateCallBack, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停预览
     */
    protected void pausePreview() {
        mDevice.close();
    }
}
