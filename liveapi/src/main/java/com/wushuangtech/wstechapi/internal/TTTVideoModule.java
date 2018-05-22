package com.wushuangtech.wstechapi.internal;

import android.content.Context;
import android.os.Build;
import android.util.LongSparseArray;
import android.view.SurfaceView;

import com.wushuangtech.api.ExternalVideoModule;
import com.wushuangtech.api.JniWorkerThread;
import com.wushuangtech.bean.ConfVideoFrame;
import com.wushuangtech.bean.RemoteVideoStats;
import com.wushuangtech.library.Constants;
import com.wushuangtech.library.GlobalConfig;
import com.wushuangtech.library.GlobalHolder;
import com.wushuangtech.library.LocalSDKConstants;
import com.wushuangtech.videocore.EncoderEngine;
import com.wushuangtech.videocore.MyVideoApi;
import com.wushuangtech.videocore.VideoNewControl;
import com.wushuangtech.videocore.RemotePlayerManger;
import com.wushuangtech.videocore.RemoteSurfaceView;
import com.wushuangtech.videocore.WaterMarkPosition;
import com.wushuangtech.wstechapi.model.TTTModuleConstants;
import com.wushuangtech.wstechapi.model.VideoModuleConfig;

import java.util.Map;
import java.util.Set;


/**
 * Created by wangzhiguo on 17/11/30.
 */

public class TTTVideoModule {

    private static TTTVideoModule holder;

    private TTTVideoModule() {
    }

    public static TTTVideoModule getInstance() {
        if (holder == null) {
            synchronized (TTTVideoModule.class) {
                if (holder == null) {
                    holder = new TTTVideoModule();
                }
            }
        }
        return holder;
    }

    public Object receiveVideoModuleEvent(VideoModuleConfig config) {
        switch (config.eventType) {
            case TTTModuleConstants.VIDEO_INIT:
                initVideoModule();
                break;
            case TTTModuleConstants.VIDEO_SENDER_ADJUST:
                adjustVideoSender((Boolean) config.objs[0]);
                break;
            case TTTModuleConstants.VIDEO_CREATE_VIEW:
                return createRemoteSurfaceView((Context) config.objs[0]);
            case TTTModuleConstants.VIDEO_PREVIEW_ADJUST:
                return adjustVideoPreview((Boolean) config.objs[0]);
            case TTTModuleConstants.VIDEO_CAPTURE_ADJUST:
                return adjustVideoCapture((Boolean) config.objs[0]);
            case TTTModuleConstants.VIDEO_REMOTE_VIDEO_ADJUST:
                adJustRemoteVideoOpenOrClose((Boolean) config.objs[0]);
                break;
            case TTTModuleConstants.VIDEO_OPEN_LOCAL:
                setupLocalVideo((SurfaceView) config.objs[0], (int) config.objs[1]
                        , (WaterMarkPosition) config.objs[2], (int) config.objs[3]);
                break;
            case TTTModuleConstants.VIDEO_OPEN_REMOTE:
                setupRemoteVideo((SurfaceView) config.objs[0], (long) config.objs[1]
                        , (String) config.objs[2], (int) config.objs[3]);
                break;
            case TTTModuleConstants.SWITCH_CAMERA:
                return switchCamera();
            case TTTModuleConstants.VIDEO_PROFILE:
                setVideoProfile((int) config.objs[0], (boolean) config.objs[1]);
                break;
            case TTTModuleConstants.VIDEO_EXTERNAL_VIDEO_FRAME:
                return pushExternalVideoFrame((ConfVideoFrame) config.objs[0], (Context) config.objs[1]);
            case TTTModuleConstants.VIDEO_REMOTE_STUTS:
                return createRemoteVideoStatus();
            case TTTModuleConstants.VIDEO_REMOTE_RTC_STUTS:
                sendRemoteVideoStatus((LongSparseArray<WorkerThread.RemoteUserVideoWorkStats>) config.objs[0]);
                break;
        }
        return LocalSDKConstants.ERROR_FUNCTION_ERROR_EMPTY_VALUE;
    }

    public Object receiveVideoModuleEvent(int eventType) {
        return receiveVideoModuleEvent(new VideoModuleConfig(eventType, null));
    }

    private void initVideoModule() {
        ExternalVideoModule externalVideoModule = ExternalVideoModule.getInstance();
        MyVideoApi videoApi = MyVideoApi.getInstance();
        externalVideoModule.setExternalVideoModuleCallback(videoApi);
        videoApi.addVideoSender(externalVideoModule);
    }

    private void adjustVideoSender(boolean isClose) {
        if (isClose) {
            MyVideoApi.getInstance().removeVideoSender(ExternalVideoModule.getInstance());
        } else {
            MyVideoApi.getInstance()
                    .addVideoSender(ExternalVideoModule.getInstance());
        }
    }

    private SurfaceView createRemoteSurfaceView(Context mContext) {
        return VideoNewControl.createRemoteSurfaceView(mContext);
    }

    private int adjustVideoPreview(boolean isPreview) {
        boolean result;
        if (isPreview) {
            result = MyVideoApi.getInstance().startPreview();
        } else {
            result = MyVideoApi.getInstance().stopPreview();
        }

        if (result) {
            return LocalSDKConstants.FUNCTION_SUCCESS;
        } else {
            return LocalSDKConstants.ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }
    }

    private boolean adjustVideoCapture(boolean isCapture) {
        if (isCapture) {
            return MyVideoApi.getInstance().startCapture();
        } else {
            return MyVideoApi.getInstance().stopCapture();
        }
    }

    private void adJustRemoteVideoOpenOrClose(boolean isOpen) {
        VideoNewControl.adJustRemoteVideoOpenOrClose(isOpen);
    }

    private void adJustLocalSurfaceView(boolean enabled) {
        VideoNewControl.adJustLocalSurfaceView(enabled);
    }

    private void setupLocalVideo(SurfaceView mView, int direction, WaterMarkPosition position, int showMode) {
        VideoNewControl.setupLocalVideo(mView, direction, position, showMode);
    }

    private void setupRemoteVideo(SurfaceView mView, long mUserID, String mDeviceID, int showMode) {
        VideoNewControl.setupRemoteVideo(mView, mUserID, mDeviceID, showMode);
    }

    private int switchCamera() {
        MyVideoApi.VideoConfig config = MyVideoApi.getInstance().getVideoConfig();
        if (config == null) {
            return LocalSDKConstants.ERROR_FUNCTION_INVOKE_ERROR;
        }

        boolean isSwitched = MyVideoApi.getInstance().switchCarmera(!config.enabeleFrontCam);
        if (!isSwitched) {
            return LocalSDKConstants.ERROR_FUNCTION_INVOKE_ERROR;
        }
        return LocalSDKConstants.FUNCTION_SUCCESS;
    }

    private void setVideoProfile(int profile, boolean swapWidthAndHeight) {
        MyVideoApi.VideoConfig config = MyVideoApi.getInstance().getVideoConfig();
        int width = config.videoWidth;
        int height = config.videoHeight;
        if (Build.MODEL.equals("H60-L03") && profile == Constants.VIDEO_PROFILE_120P) {
            profile = Constants.VIDEO_PROFILE_240P;
        }
        switch (profile) {
            case Constants.VIDEO_PROFILE_120P:
                width = 160;
                height = 120;
                config.videoFrameRate = 15;
                config.videoBitRate = 65 * 1000;
                break;
            case Constants.VIDEO_PROFILE_180P:
                width = 320;
                height = 180;
                config.videoFrameRate = 15;
                config.videoBitRate = 140 * 1000;
                GlobalConfig.mLocalVideoMass = Constants.VIDEO_PROFILE_240P;
                break;
            case Constants.VIDEO_PROFILE_240P:
                width = 320;
                height = 240;
                config.videoFrameRate = 15;
                config.videoBitRate = 200 * 1000;
                GlobalConfig.mLocalVideoMass = Constants.VIDEO_PROFILE_240P;
                break;
            case Constants.VIDEO_PROFILE_360P:
                width = 640;
                height = 360;
                config.videoFrameRate = 15;
                config.videoBitRate = 400 * 1000;
                GlobalConfig.mLocalVideoMass = Constants.VIDEO_PROFILE_240P;
                break;
            case Constants.VIDEO_PROFILE_480P:
                width = 640;
                height = 480;
                config.videoFrameRate = 15;
                config.videoBitRate = 500 * 1000;
                GlobalConfig.mLocalVideoMass = Constants.VIDEO_PROFILE_480P;
                break;
            case Constants.VIDEO_PROFILE_720P:
                width = 1280;
                height = 720;
                config.videoFrameRate = 15;
                config.videoBitRate = 1130 * 1000;
                GlobalConfig.mLocalVideoMass = Constants.VIDEO_PROFILE_720P;
                break;
            case Constants.VIDEO_PROFILE_1080P:
                width = 1920;
                height = 1080;
                config.videoFrameRate = 15;
                config.videoBitRate = 2080 * 1000;
                GlobalConfig.mLocalVideoMass = Constants.VIDEO_PROFILE_1080P;
                break;
        }

        if (swapWidthAndHeight) {
            config.videoWidth = height;
            config.videoHeight = width;
        } else {
            config.videoWidth = width;
            config.videoHeight = height;
        }
        EncoderEngine.getInstance().setEncodeWH(config.videoWidth, config.videoHeight);
        MyVideoApi.getInstance().setVideoConfig(config);
    }

    private boolean pushExternalVideoFrame(ConfVideoFrame mFrame, Context mContext) {
        if (GlobalConfig.mExternalVideoSourceIsTexture) {
            if (GlobalConfig.mExternalVideoSource && ((mFrame.format == ConfVideoFrame.FORMAT_TEXTURE_2D)
                    || (mFrame.format == ConfVideoFrame.FORMAT_TEXTURE_OES))) {
                if (mFrame.eglContext14 != null) {
                    return EncoderEngine.getInstance().startDecodeVideoFrame
                            (mContext, mFrame, true);
                } else if ((mFrame.eglContext11 != null)) {
                    return EncoderEngine.getInstance().startDecodeVideoFrame
                            (mContext, mFrame, false);
//                setTextureIdWithMatrix(frame.textureID, frame.eglContext11, frame.format, frame.stride, frame.height, frame.timeStamp, frame.transform) == 0;
                }
            }
            // TODO ConfVideoFrame里面的syncMode和transform还没用到，需要完善
        } else {
            // TODO NV21的YUV图片没处理，需要完善
            if (GlobalConfig.mExternalVideoSource && (mFrame.format == ConfVideoFrame.FORMAT_I420
                    || mFrame.format == ConfVideoFrame.FORMAT_NV21 || mFrame.format == ConfVideoFrame.FORMAT_RGBA)) {
                return EncoderEngine.getInstance().encodVideoFrame(mFrame);
            }
        }
        return false;
    }

    private WorkerThread.RemoteUserVideoWorkStats[] createRemoteVideoStatus() {
        Map<String, RemoteSurfaceView> mAllRemoteViews =
                RemotePlayerManger.getInstance().getAllRemoteViews();
        WorkerThread.RemoteUserVideoWorkStats[] mStatusList = new WorkerThread.RemoteUserVideoWorkStats[mAllRemoteViews.size()];
        Set<Map.Entry<String, RemoteSurfaceView>> entriess =
                mAllRemoteViews.entrySet();
        int count = 0;
        for (Map.Entry<String, RemoteSurfaceView> next : entriess) {
            RemoteSurfaceView value = next.getValue();
            int mTempFps = (value.getDecFrames() - value.getLastDecFrameCount()) * 1000 / WorkerThread.SPEED_TIME;
            float mTempEncodeDatas = WorkerThread.formatedSpeedKbps(value.getDecDatas() - value.getLastDecDataCount(), WorkerThread.SPEED_TIME);
            WorkerThread.RemoteUserVideoWorkStats tempStats = new WorkerThread.RemoteUserVideoWorkStats(value.getBindUserID(), (int) mTempEncodeDatas, mTempFps);
            value.setLastDecDataCount(value.getDecDatas());
            value.setLastDecFrameCount(value.getDecFrames());
            mStatusList[count] = tempStats;
            count++;
        }
        return mStatusList;
    }

    private void sendRemoteVideoStatus(LongSparseArray<WorkerThread.RemoteUserVideoWorkStats> mRemoteUserDataStats) {
        JniWorkerThread mJniWorkerThread = GlobalHolder.getInstance().getWorkerThread();
        Map<String, RemoteSurfaceView> mAllRemoteViews =
                RemotePlayerManger.getInstance().getAllRemoteViews();
        Set<Map.Entry<String, RemoteSurfaceView>> entriess =
                mAllRemoteViews.entrySet();
        for (Map.Entry<String, RemoteSurfaceView> next : entriess) {
            long uid = next.getValue().getBindUserID();
            WorkerThread.RemoteUserVideoWorkStats remoteUserVideoWorkStats = mRemoteUserDataStats.get(uid);
            if (remoteUserVideoWorkStats != null) {
                RemoteVideoStats tempVideoStats = new RemoteVideoStats(uid, 0, remoteUserVideoWorkStats.mBitrateRate, remoteUserVideoWorkStats.mFrameRate);
                mJniWorkerThread.sendMessage(JniWorkerThread.JNI_CALL_BACK_REMOTE_VIDEO_SATAUS, new Object[]{tempVideoStats});
            }
        }
    }
}
