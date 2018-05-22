package com.wushuangtech.videocore;

import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;

import com.wushuangtech.api.ExternalVideoModule;
import com.wushuangtech.library.GlobalConfig;
import com.wushuangtech.utils.PviewLog;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import project.android.imageprocessing.FastImageProcessingPipeline;
import project.android.imageprocessing.beauty.BeautifyFilter;
import project.android.imageprocessing.filter.MultiInputFilter;
import project.android.imageprocessing.filter.blend.WaterMarklBlendFilter;
import project.android.imageprocessing.input.CameraPreviewInput;
import project.android.imageprocessing.input.GLTextureOutputRenderer;
import project.android.imageprocessing.input.ImageResourceInput;
import project.android.imageprocessing.output.ScreenEndpoint;


class LocaSurfaceView implements GLTextureOutputRenderer.FrameAvaliableListener {
    private static LocaSurfaceView locaSurfaceView = null;

    private int mIndex = 0;
    RemoteSurfaceView mfastImageProcessingView = null;
    FastImageProcessingPipeline mPipeline = null;
    private MultiInputFilter filter = null;
    ScreenEndpoint mScreen = null;
    private BeautifyFilter mBeautifyFilter = null;
    CameraPreviewInput mPreviewInput = null;
    private GLTextureOutputRenderer mWatermark = null;
    WaterMarkPosition mWaterMarkPos = null;

    private boolean bPreview = false;
    private VideoEncoder mEncoder = null;
    private boolean mIsEncoding = false;
    private boolean bsartEncoding = false;
    private boolean bAllocatebuf = false;

    private final ConcurrentLinkedQueue<IntBuffer> mGLIntBufferCache = new ConcurrentLinkedQueue<>();
    private int starX = 0;
    private int startY = 0;
    private int mOutWidth = 0;
    private int mOutHeight = 0;
    private int mCount = 10;

    private IntBuffer[] mArrayGLFboBuffer;
    private ByteBuffer mGlPreviewBuffer;
    private final Object writeLock = new Object();

    int mActivityDirector;
    boolean bcreate = false;

    private boolean enable_drop_frame = true;
    private int capturedFrameCount = 0;
    private int last_buffer_duration = 0;
    private double real_fps;
    private long last_time = 0;
    private int low_buffer_duration_times = 0;
    private int high_buffer_duration_times = 0;
    private double highest_fps;
    private boolean fps_shrink_begin = true;
    private int fps_stay_stable_times = 0;
    private Thread worker;

    public static LocaSurfaceView getInstance() {
        if (locaSurfaceView == null) {
            synchronized (MyVideoApi.class) {
                if (locaSurfaceView == null) {
                    locaSurfaceView = new LocaSurfaceView();
                }
            }
        }
        return locaSurfaceView;
    }

    boolean setPreview(boolean bPreview) {
        this.bPreview = bPreview;
        if (mScreen != null) {
            mScreen.setPreView(bPreview);
            return true;
        }
        PviewLog.funEmptyError("setPreview", "ScreenEndpoint", "bcreate : " + bcreate + " | bPreview : " + bPreview);
        return false;
    }

    boolean setDisplayMode(int mode) {
        if (mScreen != null) {
            mScreen.setScaleMode(mode);
            mScreen.reInitialize();
            return true;
        }
        PviewLog.funEmptyError("setDisplayMode", "ScreenEndpoint", "bcreate : " + bcreate + " | mode : " + mode);
        return false;
    }

    public byte[] getLocalBuffer() {
        if (mBeautifyFilter != null) {
            return mBeautifyFilter.getFBOBuffer();
        }
        return null;
    }

    public int getRenderWidth() {
        if (mBeautifyFilter != null) {
            return mBeautifyFilter.getWidth();
        }
        return 0;
    }

    public int getRenderHeight() {
        if (mBeautifyFilter != null) {
            return mBeautifyFilter.getHeight();
        }
        return 0;
    }

    void CreateLocalSurfaceView(final WaterMarkPosition waterMarkPosition) {
        if (bcreate) {
            return;
        }
        bcreate = true;

        real_fps = MyVideoApi.getInstance().getVideoConfig().videoFrameRate;
        last_buffer_duration = 0;
        last_time = 0;
        low_buffer_duration_times = 0;
        high_buffer_duration_times = 0;
        highest_fps = MyVideoApi.getInstance().getVideoConfig().videoFrameRate;
        fps_shrink_begin = true;
        fps_stay_stable_times = 0;

        mPreviewInput = new CameraPreviewInput(mfastImageProcessingView);
        mPreviewInput.setActivityOrientation(mActivityDirector);
        mBeautifyFilter = new BeautifyFilter();
        mPreviewInput.addTarget(mBeautifyFilter);
        mScreen = new ScreenEndpoint(mPipeline);
        mBeautifyFilter.setAmount(0.30f);

        if (waterMarkPosition != null) {
            filter = new WaterMarklBlendFilter(waterMarkPosition);
            filter.addTarget(mScreen);
            filter.registerFilterLocation(mBeautifyFilter);
            filter.SetFrameAvaliableListener(this);
            mBeautifyFilter.addTarget(filter);
        } else {
            mBeautifyFilter.SetFrameAvaliableListener(this);
            mBeautifyFilter.addTarget(mScreen);
        }

        mPipeline.addRootRenderer(mPreviewInput);
        mPreviewInput.setCameraCbObj(new CameraPreviewInput.CameraSizeCb() {
            @Override
            public void startPrieview() {
                mPreviewInput.StartCamera();
                mScreen.setPreView(bPreview);

                Camera.Size size = mPreviewInput.getClsSize();
                if (mPreviewInput.getPreviewRotation() == 90
                        || mPreviewInput.getPreviewRotation() == 270) {
                    mScreen.SetRawSize(size.height, size.width);
                } else {
                    mScreen.SetRawSize(size.width, size.height);
                }
                mScreen.SetEncodeSize(mPreviewInput.getOutWidth() , mPreviewInput.getOutHeight());

                if (waterMarkPosition != null) {
                    if (mPreviewInput.getPreviewRotation() == 90
                            || mPreviewInput.getPreviewRotation() == 270) {
                        filter.setRenderSize(size.height, size.width);
                    } else {
                        filter.setRenderSize(size.width, size.height);
                    }
                    mWatermark = new ImageResourceInput(mfastImageProcessingView,
                            waterMarkPosition.activity, waterMarkPosition.resid);
                    filter.registerFilterLocation(mWatermark);
                    mWatermark.addTarget(filter);
                    mPipeline.addRootRenderer(mWatermark);
                }
            }
        });

        PviewLog.d("LocalCamera LocaSurfaceView startRendering ....");
        startRendering();
    }

    private void startRendering() {
        if (mPipeline == null) {
            return;
        }
        mPipeline.startRendering();
    }

    private void AllocateBuffer() {
        if (mPreviewInput == null) {
            return;
        }
        Camera.Size size = mPreviewInput.getClsSize();
        if (size == null) {
            return;
        }
        mOutWidth = mPreviewInput.getOutWidth();
        mOutHeight = mPreviewInput.getOutHeight();
        mArrayGLFboBuffer = new IntBuffer[mCount];
        for (int i = 0; i < mCount; i++) {
            mArrayGLFboBuffer[i] = IntBuffer.allocate(mOutWidth * mOutHeight);
        }
        if (mPreviewInput.getPreviewRotation() == 90) {
            starX = (size.height - mOutWidth) / 2;
            startY = (size.width - mOutHeight) / 2;
        } else {
            starX = (size.width - mOutWidth) / 2;
            startY = (size.height - mOutHeight) / 2;
        }
        mGlPreviewBuffer = ByteBuffer.allocate(mOutWidth * mOutHeight * 4);
        bAllocatebuf = true;
    }

    void setBmEncode(boolean bmEncode) {
        this.mIsEncoding = bmEncode;
    }

    private void StartEncoder() { // EncoderEngine
        if (bsartEncoding) {
            return;
        }
        Camera.Size size = mPreviewInput.getClsSize();
        if (size == null) {
            return;
        }
        bsartEncoding = true;
        AllocateBuffer();
        mEncoder = VideoEncoder.getInstance();
        mEncoder.setResolution(mOutWidth, mOutHeight);
        PviewLog.d(PviewLog.TAG, "StartEncoder mOutWidth : " + mOutWidth + " | mOutHeight : " + mOutHeight);
        PviewLog.d(PviewLog.TAG, "Camera.Siz width : " + size.width + " | height : " +
                "" + size.height);
        try {
            mEncoder.setEnableSoftEncoder(false);
            mEncoder.start();
            PviewLog.d(PviewLog.TAG, "硬编成功");
        } catch (Exception e) {
            PviewLog.d(PviewLog.TAG, "硬编失败，转为软编");
            try {
                mEncoder.setEnableSoftEncoder(true);
                mEncoder.start();
            } catch (Exception e1) {
                e1.printStackTrace();
                PviewLog.d(PviewLog.TAG, "软编失败");
            }
        }
        enableEncoding();
    }

    private void FreeEncoder() { // EncoderEngine
        if (!bsartEncoding) {
            return;
        }

        disableEncoding();

        bsartEncoding = false;
        if (mEncoder != null) {
            mEncoder.stop();
        }
        mEncoder = null;
    }

    void setAmount(float level) {
        if (mBeautifyFilter != null) {
            mBeautifyFilter.setAmount(level);
        }
    }

    public void FreeAll() {
        if (!bcreate) {
            return;
        }
        bcreate = false;
        mPipeline.pauseRendering();
        mPreviewInput.StopCamera();
        mPreviewInput.removeTarget(mBeautifyFilter);
        if (filter != null) {
            filter.clearRegisteredFilterLocations();
            filter.removeTarget(mScreen);
            mBeautifyFilter.removeTarget(filter);
            mWatermark.removeTarget(filter);
            mPipeline.removeRootRenderer(mWatermark);
        } else {
            mBeautifyFilter.removeTarget(mScreen);
        }
        mPipeline.removeRootRenderer(mPreviewInput);

        FreeEncoder();

        mScreen.destroy();
        if (filter != null) {
            mWatermark.destroy();
            filter.destroy();
        }
        mBeautifyFilter.destroy();
        mPreviewInput.destroy();

        mScreen = null;
        mBeautifyFilter = null;
        mPreviewInput = null;
        mArrayGLFboBuffer = null;
        mGlPreviewBuffer = null;
        bAllocatebuf = false;
    }

    private void enableEncoding() { // EncoderEngine
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    synchronized (mGLIntBufferCache) {
                        while (!mGLIntBufferCache.isEmpty()) {
                            IntBuffer picture = mGLIntBufferCache.poll();
                            mGlPreviewBuffer.asIntBuffer().put(picture.array());
                            mEncoder.onGetRgbaFrame(mGlPreviewBuffer.array(), mOutWidth, mOutHeight);
                            mGlPreviewBuffer.clear();
                            picture.clear();
                        }
                    }

                    // Waiting for next frame
                    synchronized (writeLock) {
                        try {
                            writeLock.wait(30);
                        } catch (InterruptedException ie) {
                            worker.interrupt();
                        }
                    }
                }
            }
        });
        worker.start();
    }

    private void disableEncoding() { // EncoderEngine
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                worker.interrupt();
            }
            worker = null;
            mGLIntBufferCache.clear();
        }
    }

    private IntBuffer getIntBuffer() {
        if (mIndex > mCount - 1) {
            mIndex = 0;
        }
        return mArrayGLFboBuffer[mIndex++];
    }

    private void putIntBuffer() {

        capturedFrameCount++;

        if (enable_drop_frame) {

            updateRealFrameRate();

            if (needDropThisFrame()) {
                return;
            }
        }

        if (mIsEncoding) {
            StartEncoder();
            if (bAllocatebuf) {
                synchronized (mGLIntBufferCache) {
                    IntBuffer mGLFboBuffer = getIntBuffer();
                    GLES20.glReadPixels(starX, startY, mOutWidth, mOutHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mGLFboBuffer);
                    if (mGLIntBufferCache.size() >= mCount) {
                        IntBuffer picture = mGLIntBufferCache.poll();
                        picture.clear();
                    }
                    mGLIntBufferCache.add(mGLFboBuffer);
                }
            }
        } else {
            FreeEncoder();
        }
    }

    boolean switchCamera(boolean bfront) {
        if (mPreviewInput == null) {
            PviewLog.funEmptyError("switchCamera->switchCamera", "mPreviewInput", String.valueOf(bfront));
            return false;
        }
        int camerid = mPreviewInput.getmCamId();
        if (bfront && camerid == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return true;
        }
        if (bfront) {
            camerid = 1;
        } else {
            camerid = 0;
        }
        mPreviewInput.switchCarmera(camerid);
        return true;
    }

    void Reset() {
        if (mfastImageProcessingView != null && mfastImageProcessingView.getHolder().getSurface().isValid()) {
            FreeAll();
            CreateLocalSurfaceView(mWaterMarkPos);
        }
    }

    @Override
    public void OnFrameAvaliable(int width, int height) {
        if (!GlobalConfig.mIsScreenRecordShare.get()) {
            PviewLog.wf("VideoEncoder OnFrameAvaliable width : " + width + " | height : " + height);
            putIntBuffer();
        }
        /*if (mEGLContext != EGL10.EGL_NO_CONTEXT && !isDecoded) {
            isDecoded = true;
            int textid = createTexture(GlobalConfig.mBitmap);
            ConfVideoFrame mConfVideoFrame = new ConfVideoFrame();
            mConfVideoFrame.stride = GlobalConfig.mBitmap.getWidth();
            mConfVideoFrame.height = GlobalConfig.mBitmap.getHeight();
            mConfVideoFrame.eglContext11 = mEGLContext;
            mConfVideoFrame.textureID = textid;
            EncoderEngine.getInstance().startDecodeVideoFrame
                    (GlobalConfig.mContext , mConfVideoFrame , false);
        }*/
    }

    private void updateRealFrameRate() {
        double shrink_step = Math.ceil(real_fps / 10);
        int grow_step = 1;

        int buffer_duration = ExternalVideoModule.getInstance().getBufferDuration();
        int videoFrameRate = MyVideoApi.getInstance().getVideoConfig().videoFrameRate;

        if (buffer_duration < 100) {
            fps_shrink_begin = true;
            low_buffer_duration_times++;
            if (low_buffer_duration_times > videoFrameRate) {
                real_fps = Math.min(highest_fps, real_fps + grow_step);
                if (real_fps == highest_fps) {
                    fps_stay_stable_times++;
                } else {
                    fps_stay_stable_times = 0;
                }
                if (fps_stay_stable_times > 10) {
                    fps_stay_stable_times = 0;
                    highest_fps = Math.min(videoFrameRate, highest_fps + grow_step);
                    Log.e("LocaSurfaceView", "++++++ update highest_fps to " + highest_fps + " ++++++");
                }
                low_buffer_duration_times = 0;
            }
            high_buffer_duration_times = 0;
        } else if (last_buffer_duration != 0) {
            if (buffer_duration > 500 && last_buffer_duration < buffer_duration) {
                if (fps_shrink_begin) {
                    fps_shrink_begin = false;
                    highest_fps = real_fps;
                    Log.e("LocaSurfaceView", "------- update highest_fps to " + highest_fps + " ------");
                }
                high_buffer_duration_times++;
                if (high_buffer_duration_times > videoFrameRate) {
                    real_fps = Math.max(5, real_fps - shrink_step);
                    high_buffer_duration_times = 0;
                }
                low_buffer_duration_times = 0;
                fps_stay_stable_times = 0;
            }
        }

        last_buffer_duration = buffer_duration;
    }

    private boolean needDropThisFrame() {
        boolean ret = false;

        int videoFrameRate = MyVideoApi.getInstance().getVideoConfig().videoFrameRate;

        if ((int) real_fps == videoFrameRate) {
            return ret;
        }

        if (real_fps / videoFrameRate > 0.5) {
            int drop_interval = videoFrameRate / (videoFrameRate - (int) real_fps);
            if (capturedFrameCount % drop_interval == 0) {
                //printf("------ drop_interval is [%d] [%d] -------\n", drop_interval, capturedFrameCount);
                ret = false;
            }
        } else {
            int frame_period = 1000 / (int) real_fps;

            int nTickErr;
            long timediff = System.nanoTime() / 1000 - last_time;
            //printf("------ real_fps is [%f] [%d] [%lld] -------\n", real_fps, capturedFrameCount, timediff);
            if (timediff < frame_period) {
                return true;
            }
            last_time = System.nanoTime() / 1000;
        }

        return ret;
    }
}
