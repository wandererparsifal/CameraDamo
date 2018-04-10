package com.parsifal.cameradamo;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.google.gson.Gson;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private HandlerThread mThreadHandler;

    private Handler mHandler;

    private TextureView mPreviewView;

    private CaptureRequest.Builder mPreviewBuilder;

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            try {
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mThreadHandler = new HandlerThread("CAMERA2");
        mThreadHandler.start();
        mHandler = new Handler(mThreadHandler.getLooper());
        mPreviewView = findViewById(R.id.previewView);
        mPreviewView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @SuppressLint("MissingPermission")
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
                try {
                    String[] CameraIdList = cameraManager.getCameraIdList();
                    String CameraId0 = CameraIdList[0];
                    //获取可用相机设备列表
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(CameraId0);
                    // 获取摄像头支持的配置属性
                    StreamConfigurationMap map = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Log.e("getOutputSizes", new Gson().toJson(map.getOutputSizes(ImageFormat.JPEG)));
                    // TODO: 18-4-10 设置预览尺寸
                    cameraManager.openCamera(CameraId0, mCameraDeviceStateCallback, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
    }

    private void startPreview(CameraDevice camera) throws CameraAccessException {
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewView.getWidth(), mPreviewView.getHeight());
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);
        camera.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mHandler);
    }

    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                //session.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
                session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                                @NonNull CaptureResult partialResult) {
                }
            };

    @Override
    protected void onStop() {
        super.onStop();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
