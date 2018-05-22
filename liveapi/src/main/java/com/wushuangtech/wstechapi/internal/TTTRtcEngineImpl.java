package com.wushuangtech.wstechapi.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.wushuangtech.api.EnterConfApi;
import com.wushuangtech.api.ExternalAudioModule;
import com.wushuangtech.api.ExternalChatModule;
import com.wushuangtech.api.ExternalVideoModule;
import com.wushuangtech.api.JniWorkerThread;
import com.wushuangtech.api.LogWorkerThread;
import com.wushuangtech.audiocore.MyAudioApi;
import com.wushuangtech.bean.ConfVideoFrame;
import com.wushuangtech.bean.ScreenRecordConfig;
import com.wushuangtech.bean.VideoCompositingLayout;
import com.wushuangtech.jni.RoomJni;
import com.wushuangtech.jni.VideoJni;
import com.wushuangtech.library.Constants;
import com.wushuangtech.library.GlobalConfig;
import com.wushuangtech.library.GlobalHolder;
import com.wushuangtech.library.UserDeviceConfig;
import com.wushuangtech.library.screenrecorder.EncoderConfig;
import com.wushuangtech.library.screenrecorder.RecordCallback;
import com.wushuangtech.library.screenrecorder.ScreenCapture;
import com.wushuangtech.utils.PviewLog;
import com.wushuangtech.videocore.MyVideoApi;
import com.wushuangtech.videocore.RemotePlayerManger;
import com.wushuangtech.videocore.RemoteSurfaceView;
import com.wushuangtech.videocore.VideoEncoder;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.TTTRtcEngineEventHandler;
import com.wushuangtech.wstechapi.model.PublisherConfiguration;
import com.wushuangtech.wstechapi.model.TTTModuleConstants;
import com.wushuangtech.wstechapi.model.VideoCanvas;
import com.wushuangtech.wstechapi.model.VideoModuleConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.wushuangtech.api.EnterConfApi.RoomMode;
import static com.wushuangtech.library.Constants.CHANNEL_PROFILE_COMMUNICATION;
import static com.wushuangtech.library.Constants.CHANNEL_PROFILE_GAME_FREE_MODE;
import static com.wushuangtech.library.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_ANCHOR;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_AUDIENCE;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;
import static com.wushuangtech.library.Constants.LOG_FILTER_DEBUG;
import static com.wushuangtech.library.Constants.LOG_FILTER_ERROR;
import static com.wushuangtech.library.Constants.LOG_FILTER_INFO;
import static com.wushuangtech.library.Constants.LOG_FILTER_OFF;
import static com.wushuangtech.library.Constants.LOG_FILTER_WARNING;
import static com.wushuangtech.library.GlobalConfig.mCDNPullAddressPrefix;
import static com.wushuangtech.library.GlobalConfig.mCurrentChannelMode;
import static com.wushuangtech.library.GlobalConfig.mExternalVideoSource;
import static com.wushuangtech.library.GlobalConfig.mExternalVideoSourceIsTexture;
import static com.wushuangtech.library.GlobalConfig.mIsEnableAudioMode;
import static com.wushuangtech.library.GlobalConfig.mIsEnableVideoMode;
import static com.wushuangtech.library.GlobalConfig.mIsInRoom;
import static com.wushuangtech.library.GlobalConfig.mIsSpeakerphoneEnabled;
import static com.wushuangtech.library.GlobalConfig.mLocalUserID;
import static com.wushuangtech.library.LocalSDKConstants.ERROR_FUNCTION_ERROR_ARGS;
import static com.wushuangtech.library.LocalSDKConstants.ERROR_FUNCTION_ERROR_EMPTY_VALUE;
import static com.wushuangtech.library.LocalSDKConstants.ERROR_FUNCTION_INVOKE_ERROR;
import static com.wushuangtech.library.LocalSDKConstants.ERROR_FUNCTION_STATED;
import static com.wushuangtech.library.LocalSDKConstants.FUNCTION_SUCCESS;
import static com.wushuangtech.library.LocalSDKConstants.LOG_CREATE_ERROR_NO_FILE;
import static com.wushuangtech.library.LocalSDKConstants.LOG_CREATE_ERROR_UNKNOW;
import static com.wushuangtech.utils.PviewLog.TAG;

/**
 * Created by wangzhiguo on 17/6/7.
 */
public class TTTRtcEngineImpl extends TTTRtcEngine {

    private int mLogFilterLevel = LOG_FILTER_OFF;
    private WorkerThread mWorkerThread;
    private JniWorkerThread mJniWorkerThread;
    private LogWorkerThread mLogWorkerThread;
    private boolean mIsInited;

    private Process mLogProcess;
    private String mCacheLogPath;
    private ScreenCapture mScreenCapture;
    private Lock mRecorderLock = new ReentrantLock();
    private static final String[] H264_HW_BLACKLIST = {"SAMSUNG-SGH-I337", "Nexus 7", "Nexus 4",
            "P6-C00", "HM 2A", "XT105", "XT109", "XT1060"};

    public TTTRtcEngineImpl(Context mContext, String mAppID,
                            TTTRtcEngineEventHandler mEnterConfApiCallbackHandler) {
        super();
        GlobalConfig.mAppID = mAppID;
        EnterConfApi enterConfApi = EnterConfApi.getInstance();
        enterConfApi.setup(mAppID, mContext, true);
        //音频模块初始化
        ExternalAudioModule externalAudioModule = ExternalAudioModule.getInstance();
        MyAudioApi audioApi = MyAudioApi.getInstance(mContext);
        externalAudioModule.setExternalAudioModuleCallback(audioApi);
        audioApi.addAudioSender(externalAudioModule);
        //视频模块初始化
        handleVideoModule(TTTModuleConstants.VIDEO_INIT);
        //聊天模块初始化
        ExternalChatModule.getInstance().handleActionEvent(ExternalChatModule.ACTION_ALIOSS_INIT , mContext);
        ExternalChatModule.getInstance().handleActionEvent(ExternalChatModule.ACTION_MEDIARECORD_INIT , mContext);
        initWorkerThread(mContext);
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(
                Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
        GlobalConfig.mIsScreenRecordShare = new AtomicBoolean();
        if (mEnterConfApiCallbackHandler != null) {
            setTTTRtcEngineEventHandler(mEnterConfApiCallbackHandler);
        }
        mIsInited = true;
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-6-7 17:26:20<br/>
     * Description: 重新初始化SDK
     *
     * @param mContext                     安卓上下文
     * @param mAppID                       程序的唯一标识
     * @param mEnterConfApiCallbackHandler 回调接收者
     */
    public void reinitialize(Context mContext, String mAppID,
                             TTTRtcEngineEventHandler mEnterConfApiCallbackHandler) {
        GlobalConfig.mAppID = mAppID;
        EnterConfApi.getInstance().setAppID(mAppID);
        setTTTRtcEngineEventHandler(mEnterConfApiCallbackHandler);
        if (mIsInited) {
            return;
        }
//        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_SENDER_ADJUST, new Object[]{false}));
//        MyAudioApi.getInstance(mWorkerThread.getContext())
//                .addAudioSender(ExternalAudioModule.getInstance());
//        initWorkerThread(mContext);
        mIsInited = true;
    }

    public void doDestroy() {
        if (!mIsInited) {
            return;
        }
        mIsInited = false;
//        MyAudioApi.getInstance(mWorkerThread.getContext().getApplicationContext()).removeAudioSender(ExternalAudioModule.getInstance());
//        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_SENDER_ADJUST, new Object[]{true}));
//        MyAudioApi.getInstance(mWorkerThread.getContext()).stopAudioFileMixing();
//        ExternalAudioModule.getInstance().unInitialize();
//        ExternalVideoModule.getInstance().uninitialize();
//        RoomJni.getInstance().unInitialize();
//        VideoJni.getInstance().unInitialize();
//        ReportLogJni.getInstance().unInitialize();
//        deInitWorkerThread();
    }

    private void initWorkerThread(Context mContext) {
        if (mWorkerThread == null) {
            mWorkerThread = new WorkerThread();
            mWorkerThread.setContext(mContext);
            mWorkerThread.setTTTRtcEngine(this);
            mWorkerThread.start();
            mWorkerThread.waitForReady();
        }

        if (mJniWorkerThread == null) {
            mJniWorkerThread = new JniWorkerThread();
            mJniWorkerThread.start();
            mJniWorkerThread.waitForReady();
            GlobalHolder.getInstance().setWorkerThread(mJniWorkerThread);
        }

        if (mLogWorkerThread == null) {
            mLogWorkerThread = new LogWorkerThread();
            mLogWorkerThread.start();
            mLogWorkerThread.waitForReady();
        }
    }

    private synchronized void deInitWorkerThread() {
        if (mWorkerThread == null) {
            return;
        }
        mWorkerThread.exit();
        try {
            mWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWorkerThread = null;

        if (mJniWorkerThread == null) {
            return;
        }
        mJniWorkerThread.exit();
        try {
            mJniWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mJniWorkerThread = null;

        if (mLogWorkerThread == null) {
            return;
        }
        mLogWorkerThread.exit();
        try {
            mLogWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mLogWorkerThread = null;
    }

    private synchronized Object handleVideoModule(VideoModuleConfig config) {
        return TTTVideoModule.getInstance().receiveVideoModuleEvent(config);
    }

    private synchronized Object handleVideoModule(int eventType) {
        return TTTVideoModule.getInstance().receiveVideoModuleEvent(eventType);
    }

    @Override
    public void setTTTRtcEngineEventHandler(TTTRtcEngineEventHandler mGSWSEngineEventHandler) {
        GlobalHolder.getInstance().setCommunicationHelper(mGSWSEngineEventHandler);
    }

    @Override
    public SurfaceView CreateRendererView(Context context) {
        Object obj = handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_CREATE_VIEW, new Object[]{context}));
        if (obj != null) {
            return (SurfaceView) obj;
        }
        return null;
    }

    @Override
    public int joinChannel(String channelKey,
                           String channelName,
                           long optionalUid) {
        if (mCurrentChannelMode != CHANNEL_PROFILE_COMMUNICATION
                && mCurrentChannelMode != CHANNEL_PROFILE_LIVE_BROADCASTING
                && mCurrentChannelMode != CHANNEL_PROFILE_GAME_FREE_MODE) {
            return ERROR_FUNCTION_INVOKE_ERROR;
        }

        if (mIsInRoom) {
            return ERROR_FUNCTION_STATED;
        }

        if (!TextUtils.isDigitsOnly(channelName)) {
            GlobalHolder.getInstance().notifyCHChannelNameError();
            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }

        mWorkerThread.checkHeadsetListener();
        GlobalConfig.mLocalUserID = optionalUid;
        boolean result;
        String mCdnText;
        mCdnText = mCDNPullAddressPrefix + channelName;
        String mPushUrl = GlobalConfig.mPushUrl;
        if (TextUtils.isEmpty(mPushUrl)) {
            mPushUrl = "rtmp://push.3ttech.cn/sdk/" + channelName;
        }

        if (GlobalConfig.mCurrentChannelMode != Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
                || (GlobalConfig.mCurrentChannelMode == Constants.CHANNEL_PROFILE_LIVE_BROADCASTING && GlobalConfig.mIsLocalHost != Constants.CLIENT_ROLE_BROADCASTER)) {
            mPushUrl = "";
        }

        PviewLog.i(TAG, "finally mPushUrl : " + mPushUrl);
        if (TextUtils.isEmpty(channelKey)) {
            if (GlobalConfig.mIsEnableVideoMode) {
                result = EnterConfApi.getInstance().enterRoom
                        ("" , optionalUid, Long.valueOf(channelName), GlobalConfig.mIsLocalHost,
                                mPushUrl);
            } else {
                result = EnterConfApi.getInstance().enterAudioRoom
                        ("" , optionalUid, Long.valueOf(channelName), GlobalConfig.mIsLocalHost,
                                mPushUrl);
            }
        } else {
            if (GlobalConfig.mIsEnableVideoMode) {
                result = EnterConfApi.getInstance().enterRoom
                        (channelKey, optionalUid, Long.valueOf(channelName), GlobalConfig.mIsLocalHost,
                                mPushUrl);
            } else {
                result = EnterConfApi.getInstance().enterAudioRoom
                        (channelKey, optionalUid, Long.valueOf(channelName), GlobalConfig.mIsLocalHost,
                                mPushUrl);
            }
        }
        mJniWorkerThread.clearDelayMessages();
        if (result) {
            GlobalConfig.mCDNAPullddress = mCdnText;
            return FUNCTION_SUCCESS;
        } else {
            return ERROR_FUNCTION_INVOKE_ERROR;
        }
    }

    @Override
    public int leaveChannel() {
        if (!mIsInRoom) {
            return ERROR_FUNCTION_STATED;
        }
        EnterConfApi.getInstance().exitRoom();
        mWorkerThread.reset();
        return FUNCTION_SUCCESS;
    }

    @Override
    public int enableVideo() {
        if (mIsEnableVideoMode) {
            return ERROR_FUNCTION_STATED;
        }
        mIsEnableVideoMode = true;
        enableAudio();
        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_PREVIEW_ADJUST, new Object[]{true}));
        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_CAPTURE_ADJUST, new Object[]{true}));
        if (mIsInRoom) {
            EnterConfApi.getInstance().muteLocalVideo(false);
            handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_REMOTE_VIDEO_ADJUST, new Object[]{true}));
        }

        if (GlobalConfig.mCurrentChannelMode == CHANNEL_PROFILE_GAME_FREE_MODE) {
            SurfaceView surfaceView = CreateRendererView(mWorkerThread.getContext());
            setupLocalVideo(new VideoCanvas(0, Constants.RENDER_MODE_HIDDEN,
                    surfaceView), 90);
        }
        return FUNCTION_SUCCESS;
    }

    @Override
    public int disableVideo() {
        if (!mIsEnableVideoMode) {
            return ERROR_FUNCTION_STATED;
        }

        disableAudio();
        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_PREVIEW_ADJUST, new Object[]{false}));
        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_CAPTURE_ADJUST, new Object[]{false}));
        if (mIsInRoom) {
            EnterConfApi.getInstance().muteLocalVideo(true);
            handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_REMOTE_VIDEO_ADJUST, new Object[]{false}));
        }
        mIsEnableVideoMode = false;
        return FUNCTION_SUCCESS;
    }

    @Override
    public int enableAudio() {
        if (mIsEnableAudioMode) {
            return ERROR_FUNCTION_STATED;
        }
        mIsEnableAudioMode = true;
        MyAudioApi.getInstance(mWorkerThread.getContext()).stopCapture();
        MyAudioApi.getInstance(mWorkerThread.getContext()).startCapture();
        muteAllRemoteAudioStreams(false);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int disableAudio() {
        if (!mIsEnableAudioMode) {
            return ERROR_FUNCTION_STATED;
        }

        mIsEnableAudioMode = false;
        MyAudioApi.getInstance(mWorkerThread.getContext()).stopCapture();
        muteAllRemoteAudioStreams(true);
        stopAudioMixing();
        return FUNCTION_SUCCESS;
    }

    @Override
    public int enableLocalVideo(boolean enabled) {
        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_LOCAL_VIDEO_ADJUST, new Object[]{enabled}));
        return FUNCTION_SUCCESS;
    }

    @Override
    public int startPreview() {
        if (!mIsEnableVideoMode || mExternalVideoSource) {
            return ERROR_FUNCTION_STATED;
        }

        Object obj = handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_PREVIEW_ADJUST, new Object[]{true}));
        if (obj != null) {
            return (int) obj;
        }
        return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
    }

    @Override
    public int stopPreview() {
        Object obj = handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_PREVIEW_ADJUST, new Object[]{false}));
        if (obj != null) {
            return (int) obj;
        }
        return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
    }

    @Override
    public int setLogFile(String mLogPath) {
        try {
            if (mLogProcess != null) {
                mLogProcess.destroy();
            }

            String clear = "logcat -c";
            Process mClearPro = Runtime.getRuntime().exec(clear);
            processWaitFor(mClearPro);
            mClearPro.destroy();

            if (TextUtils.isEmpty(mLogPath)) {
                return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
            }

            File createLogFile = new File(mLogPath);
            if (!createLogFile.exists() || !createLogFile.isFile()) {
                return LOG_CREATE_ERROR_NO_FILE;
            }

            String mFilter = "";
            switch (mLogFilterLevel) {
                case LOG_FILTER_INFO:
                    mFilter = "*:i";
                    break;
                case LOG_FILTER_DEBUG:
                    mFilter = "*:d";
                    break;
                case LOG_FILTER_WARNING:
                    mFilter = "*:w";
                    break;
                case LOG_FILTER_ERROR:
                    mFilter = "*:e";
                    break;
                case LOG_FILTER_OFF:
                    mFilter = "";
                    break;
            }
            String log = "logcat " + mFilter + " -v time -f " + createLogFile.getAbsolutePath();
            mLogProcess = Runtime.getRuntime().exec(log);
            PviewLog.i("Cache mLogPath : " + mLogPath);
            mCacheLogPath = mLogPath;
        } catch (Exception e) {
            PviewLog.i(PviewLog.FUN_ERROR, "setLogFile exception : " + e.getLocalizedMessage());
            return LOG_CREATE_ERROR_UNKNOW;
        }
        return FUNCTION_SUCCESS;
    }

    @Override
    public int setLogFilter(int var1) {
        mLogFilterLevel = var1;
        if (!TextUtils.isEmpty(mCacheLogPath)) {
            setLogFile(mCacheLogPath);
            return FUNCTION_SUCCESS;
        }
        return ERROR_FUNCTION_ERROR_ARGS;
    }

    @Override
    public int setupLocalVideo(VideoCanvas canvas, int activityOrientation) {
        if (!mIsEnableVideoMode || mExternalVideoSource) {
            return ERROR_FUNCTION_STATED;
        }

        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_OPEN_LOCAL, new Object[]{canvas.getSurface(), activityOrientation, null, canvas.getShowMode()}));
        return FUNCTION_SUCCESS;
    }

    @Override
    public int setupRemoteVideo(VideoCanvas canvas) {
        int result = FUNCTION_SUCCESS;
        long userID = 0;
        if (canvas == null) {
            result = ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        } else {
            userID = canvas.getUserID();
            if (!mIsEnableVideoMode) {
                result = ERROR_FUNCTION_STATED;
            } else {
                UserDeviceConfig udc = GlobalHolder.getInstance().getUserDefaultDevice(canvas.getUserID());
                if (udc == null) {
                    PviewLog.funEmptyError("setupRemoteVideo", "UserDeviceConfig", String.valueOf(canvas.getUserID()));
                    result = ERROR_FUNCTION_ERROR_EMPTY_VALUE;
                } else {
                    if (!udc.isUse()) {
                        PviewLog.funEmptyError("setupRemoteVideo", "该用户的视频设备未启用,inuse=0", String.valueOf(canvas.getUserID()));
                        result = ERROR_FUNCTION_ERROR_EMPTY_VALUE;
                    }
                    handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_OPEN_REMOTE, new Object[]{
                            canvas.getSurface(), canvas.getUserID(), udc.getDeviceID(), canvas.getShowMode()}));
                }
            }

        }
        PviewLog.d("setupRemoteVideo 1 start open user video , id : " + userID);
        return result;
    }

    @Override
    public int switchCamera() {
        if (!mIsEnableVideoMode || mExternalVideoSource) {
            return ERROR_FUNCTION_STATED;
        }

        Object obj = handleVideoModule(new VideoModuleConfig(TTTModuleConstants.SWITCH_CAMERA, new Object[]{}));
        if (obj != null) {
            return (int) obj;
        }
        return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
    }

    @Override
    public int setVideoProfile(int profile, boolean swapWidthAndHeight) {
        handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_PROFILE, new Object[]{profile, swapWidthAndHeight}));
        return FUNCTION_SUCCESS;
    }

    @Override
    public int setEnableSpeakerphone(boolean isOpenSpeaker) {
        AudioManager audioManager = (AudioManager) mWorkerThread.getContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            if (isOpenSpeaker) {
                audioManager.setSpeakerphoneOn(true);
                mIsSpeakerphoneEnabled = true;
                PviewLog.i("setEnableSpeakerphone audio -> mIsSpeakerphoneEnabled true , speaker!");
                GlobalHolder.getInstance().notifyCHAudioRouteChanged(Constants.AUDIO_ROUTE_SPEAKER);
            } else {
                audioManager.setSpeakerphoneOn(false);
                PviewLog.i("setEnableSpeakerphone audio -> mIsSpeakerphoneEnabled false , headphone!");
                mIsSpeakerphoneEnabled = false;
                GlobalHolder.getInstance().notifyCHAudioRouteChanged(Constants.AUDIO_ROUTE_HEADPHONE);
            }
        } else {
            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }
        return FUNCTION_SUCCESS;
    }

    @Override
    public boolean isSpeakerphoneEnabled() {
        return mIsSpeakerphoneEnabled;
    }

    @Override
    public int muteLocalAudioStream(boolean muted) {
        GlobalConfig.mIsMuteLocalAudio = muted;
        RoomJni.getInstance().MuteLocalAudio(muted);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int muteLocalVideoStream(boolean muted) {
        GlobalConfig.mIsMuteLocalVideo = muted;
        EnterConfApi.getInstance().muteLocalVideo(muted);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int muteRemoteAudioStream(long uid, boolean muted) {
        RoomJni.getInstance().MuteRemoteAudio(uid, muted);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int muteRemoteVideoStream(long uid, boolean muted) {
        UserDeviceConfig udc = GlobalHolder.getInstance().getUserDefaultDevice(uid);
        if (udc != null) {
            if (muted) {
                EnterConfApi.getInstance().openDeviceVideo(uid, udc.getDeviceID());
            } else {
                EnterConfApi.getInstance().closeDeviceVideo(uid, udc.getDeviceID());
            }
        } else {
            return ERROR_FUNCTION_INVOKE_ERROR;
        }
        return FUNCTION_SUCCESS;
    }

    @Override
    public int muteAllRemoteAudioStreams(boolean muted) {
        RoomJni.getInstance().MuteAllRemoteAudio(muted);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int muteAllRemoteVideoStreams(boolean muted) {
        RoomJni.getInstance().MuteAllRemoteVideo(muted);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int setChannelProfile(int mode) {
        RoomMode value = RoomMode.getValue(mode);
        if (value == RoomMode.ROOM_MODE_UNFINE) {
            return ERROR_FUNCTION_ERROR_ARGS;
        }

        mCurrentChannelMode = mode;
        EnterConfApi.getInstance().setRoomMode(value);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int setDefaultAudioRouteToSpeakerphone(boolean defaultToSpeaker) {
        if (GlobalConfig.mIsInRoom) {
            setEnableSpeakerphone(defaultToSpeaker);
        }

        if (defaultToSpeaker) {
            GlobalConfig.mDefaultAudioRoute = Constants.AUDIO_ROUTE_SPEAKER;
        } else {
            GlobalConfig.mDefaultAudioRoute = Constants.AUDIO_ROUTE_HEADPHONE;
        }
        return FUNCTION_SUCCESS;
    }

    @Override
    public int setClientRole(int role, String permissionKey) {
        if (role == Constants.CLIENT_ROLE_BROADCASTER) {
            GlobalConfig.mIsLocalHost = CLIENT_ROLE_BROADCASTER;
            if (GlobalConfig.mIsInRoom) {
                RoomJni.getInstance().RoomChangeMyRole(CLIENT_ROLE_BROADCASTER);
                EnterConfApi.getInstance().applySpeakPermission(true);
                EnterConfApi.getInstance().muteLocalAudio(false);
                if (GlobalConfig.mIsEnableVideoMode) {
                    EnterConfApi.getInstance().muteLocalVideo(false);
                } else {
                    EnterConfApi.getInstance().muteLocalVideo(false);
                }
            }
        } else if (role == Constants.CLIENT_ROLE_AUDIENCE) {
            GlobalConfig.mIsLocalHost = CLIENT_ROLE_AUDIENCE;
            if (GlobalConfig.mIsInRoom) {
                RoomJni.getInstance().RoomChangeMyRole(CLIENT_ROLE_AUDIENCE);
                EnterConfApi.getInstance().applySpeakPermission(false);
                EnterConfApi.getInstance().muteLocalAudio(true);
                EnterConfApi.getInstance().muteLocalVideo(true);
            }
        } else if (role == CLIENT_ROLE_ANCHOR) {
            GlobalConfig.mIsLocalHost = CLIENT_ROLE_ANCHOR;
            if (GlobalConfig.mIsInRoom) {
                RoomJni.getInstance().RoomChangeMyRole(CLIENT_ROLE_ANCHOR);
                EnterConfApi.getInstance().applySpeakPermission(true);
                EnterConfApi.getInstance().muteLocalAudio(false);
                if (GlobalConfig.mIsEnableVideoMode) {
                    EnterConfApi.getInstance().muteLocalVideo(false);
                } else {
                    EnterConfApi.getInstance().muteLocalVideo(false);
                }
            }
        }
        PviewLog.d("setClientRole : " + GlobalConfig.mIsLocalHost);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int setHighQualityAudioParameters(boolean enable) {
        EnterConfApi.getInstance().useHighQualityAudio(enable);
        return FUNCTION_SUCCESS;
    }

    //陌陌测试的一个花屏BUG，先留着等解决验证
//    @Override
//    public int setVideoCompositingLayout(VideoCompositingLayout layout) {
//        if (layout == null) {
//            return ERROR_FUNCTION_ERROR_ARGS;
//        }
//
//        VideoCompositingLayout.Region[] regions = layout.regions;
//        if (GlobalConfig.mIsLocalHost != Constants.CLIENT_ROLE_BROADCASTER ||
//                regions == null || regions.length <= 0) {
//            return ERROR_FUNCTION_ERROR_ARGS;
//        }
//
//        try {
//            JSONObject Sei = new JSONObject();
//            Sei.put("mid", String.valueOf(GlobalConfig.mLocalUserID));
//            JSONArray pos = new JSONArray();
//            for (int i = 0; i < regions.length; i++) {
//                JSONObject temp = new JSONObject();
//                VideoCompositingLayout.Region region = regions[i];
//                temp.put("id", String.valueOf(region.uid));
//                temp.put("z", 0);
//                temp.put("x", 0);
//                temp.put("y", 0);
//                temp.put("w", 1);
//                temp.put("h", 1);
//                pos.put(temp);
//                if (region.uid != GlobalConfig.mLocalUserID) {
//                    VideoJni.getInstance().RtmpAddVideo(region.uid, String.valueOf(region.uid), 1);
//                }
//            }
//
//            int[] encSize = ExternalVideoModule.getInstance().getEncodeSize();
//            JSONObject positem = new JSONObject();
//            positem.put("id", String.valueOf(GlobalConfig.mLocalUserID));
//            positem.put("x", regions[0].x);
//            positem.put("y", regions[0].y);
//            positem.put("w", regions[0].width);
//            positem.put("h", regions[0].height);
//            positem.put("z", 1);
//            pos.put(positem);
//            Sei.put("pos", pos);
//            long ts = System.currentTimeMillis();
//            Sei.put("ts", ts);
//            Sei.put("ver", "20161227");
//            JSONObject canvas = new JSONObject();
//            canvas.put("w", 128);
//            canvas.put("h", 128);
//            JSONArray bgrgb = new JSONArray();
//            bgrgb.put(layout.backgroundColor[0]).put(layout.backgroundColor[1]).
//                    put(layout.backgroundColor[2]);
//            canvas.put("bgrgb", bgrgb);
//            Sei.put("canvas", canvas);
//            VideoJni.getInstance().RtmpSetH264Sei(Sei.toString());
//            Log.w("wzg" , " sei : " + Sei.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
//        }
//        return FUNCTION_SUCCESS;
//    }

    @Override
    public int setVideoCompositingLayout(VideoCompositingLayout layout) {
        if (layout == null) {
            return ERROR_FUNCTION_ERROR_ARGS;
        }

        VideoCompositingLayout.Region[] regions = layout.regions;
        if (GlobalConfig.mIsLocalHost != Constants.CLIENT_ROLE_BROADCASTER ||
                regions == null || regions.length <= 0) {
            return ERROR_FUNCTION_ERROR_ARGS;
        }

        try {
            UserDeviceConfig localDeviceConfig = GlobalHolder.getInstance().getUserDefaultDevice(GlobalConfig.mLocalUserID);
            if (localDeviceConfig == null) {
                PviewLog.e("SEI -> setVideoCompositingLayout error! < " + GlobalConfig.mLocalUserID + " > Get local device obj is null!");
                return ERROR_FUNCTION_INVOKE_ERROR;
            }

            String localDeviceID = localDeviceConfig.getDeviceID();
            if (TextUtils.isEmpty(localDeviceID)) {
                PviewLog.e("SEI -> setVideoCompositingLayout error! < " + GlobalConfig.mLocalUserID + " > Get local device id is null!");
                return ERROR_FUNCTION_INVOKE_ERROR;
            }
            JSONObject Sei = new JSONObject();
            Sei.put("mid", localDeviceID);
            JSONArray pos = new JSONArray();
            for (VideoCompositingLayout.Region region : regions) {
                JSONObject temp = new JSONObject();
                UserDeviceConfig mDefDeviceConfig = GlobalHolder.getInstance().getUserDefaultDevice(region.mUserID);
                if (mDefDeviceConfig == null) {
                    PviewLog.e("SEI -> setVideoCompositingLayout error! < " + region.mUserID + " > 获取此用户ID对应默认的设备ID是空的!");
                    continue;
                }

                String mDefDeviceID = mDefDeviceConfig.getDeviceID();
                if (TextUtils.isEmpty(mDefDeviceID)) {
                    PviewLog.e("SEI -> setVideoCompositingLayout error! < " + region.mUserID + " > 从UserDeviceConfig获取此用户ID对应默认的设备ID是空的!");
                    continue;
                }

                if (!localDeviceID.equals(mDefDeviceID)) {
                    VideoJni.getInstance().RtmpAddVideo(region.mUserID, mDefDeviceID, 1);
                    PviewLog.i("SEI -> RtmpAddVideo uid : " + region.mUserID + " | device id : " + mDefDeviceID);
                }

                temp.put("id", mDefDeviceID);
                temp.put("z", region.zOrder);
                temp.put("x", region.x);
                temp.put("y", region.y);
                temp.put("w", region.width);
                temp.put("h", region.height);
                pos.put(temp);
            }

            int[] encSize = ExternalVideoModule.getInstance().getEncodeSize();
            JSONObject positem = new JSONObject();
            positem.put("id", localDeviceID);
            positem.put("x", 0);
            positem.put("y", 0);
            positem.put("w", 1);
            positem.put("h", 1);
            positem.put("z", 0);
            pos.put(positem);
            Sei.put("pos", pos);
            long ts = System.currentTimeMillis();
            Sei.put("ts", ts);
            Sei.put("ver", "20161227");
            JSONObject canvas = new JSONObject();
            canvas.put("w", encSize[0]);
            canvas.put("h", encSize[1]);
            JSONArray bgrgb = new JSONArray();
            bgrgb.put(layout.backgroundColor[0]).put(layout.backgroundColor[1]).
                    put(layout.backgroundColor[2]);
            canvas.put("bgrgb", bgrgb);
            Sei.put("canvas", canvas);
            PviewLog.d("SEI -> finally send : " + Sei.toString());
            VideoJni.getInstance().RtmpSetH264Sei(Sei.toString(), "");
        } catch (JSONException e) {
            e.printStackTrace();
            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }
        return FUNCTION_SUCCESS;
    }

    @Override
    public int setSpeakerphoneVolume(int volume) {
        if (volume < 0 || volume > 255) {
            return ERROR_FUNCTION_ERROR_ARGS;
        }

        float rate = (float) volume / 255f;
        AudioManager audioManager = (AudioManager) mWorkerThread.getContext().
                getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            float result = streamMaxVolume * rate + 0.5f;
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    (int) result, 0);
        } else {
            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }
        return FUNCTION_SUCCESS;
    }

    @Override
    public int enableAudioVolumeIndication(int interval, int smooth) {
        int finallyInterval;
        if (interval <= 0) {
            finallyInterval = Integer.MAX_VALUE;
        } else {
            finallyInterval = interval;
        }
        EnterConfApi.getInstance().setAudioLevelReportInterval(finallyInterval);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int startAudioMixing(String filePath, boolean loopback, boolean replace, int cycle) {
        if (cycle <= 0) {
            return ERROR_FUNCTION_ERROR_ARGS;
        }

        MyAudioApi.getInstance(mWorkerThread.getContext()).startAudioFileMixing(filePath, loopback, cycle);
        float mixBit = 0.3f;
        if (replace) {
            mixBit = 1.0f;
        }
        MyAudioApi.getInstance(mWorkerThread.getContext()).adjustAudioFileVolumeScale(mixBit);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int stopAudioMixing() {
        if (!mIsInRoom) {
            return ERROR_FUNCTION_STATED;
        }
        MyAudioApi.getInstance(mWorkerThread.getContext()).stopAudioFileMixing();
        return FUNCTION_SUCCESS;
    }

    @Override
    public int pauseAudioMixing() {
        if (!mIsInRoom) {
            return ERROR_FUNCTION_STATED;
        }
        MyAudioApi.getInstance(mWorkerThread.getContext()).pauseAudioFileMix();
        return FUNCTION_SUCCESS;
    }

    @Override
    public int resumeAudioMixing() {
        if (!mIsInRoom) {
            return ERROR_FUNCTION_STATED;
        }
        MyAudioApi.getInstance(mWorkerThread.getContext()).resumeAudioFileMix();
        return FUNCTION_SUCCESS;
    }

    @Override
    public int getAudioMixingDuration() {
        return GlobalConfig.mCurrentAudioMixingDuration;
    }

    @Override
    public boolean kickChannelUser(long uid) {
        if (uid == mLocalUserID) {
            return false;
        }
        EnterConfApi.getInstance().kickUser(uid);
        return true;
    }

    @Override
    public int muteRemoteSpeaking(long uid, boolean isDisable) {
        if (isDisable) {
            RoomJni.getInstance().RoomGrantPermission(uid, 1, 1);
        } else {
            RoomJni.getInstance().RoomGrantPermission(uid, 1, 3);
        }
        return FUNCTION_SUCCESS;
    }

    @Override
    public int getAudioMixingCurrentPosition() {
        return GlobalConfig.mCurrentAudioMixingPosition;
    }

    @Override
    public String getVersion() {
        return GlobalConfig.SDK_VERSION_NAME;
    }

    @Override
    public boolean isTextureEncodeSupported() {
        List<String> exceptionModels = Arrays.asList(H264_HW_BLACKLIST);
        if (exceptionModels.contains(Build.MODEL)) {
            Log.w("DeviceUtils", "Model: " + Build.MODEL + " has black listed H.264 encoder.");
            return false;
        }
        if (Build.VERSION.SDK_INT <= 18) {
            return false;
        }
        return true;
    }

    @Override
    public void setExternalVideoSource(boolean enable, boolean useTexture, boolean pushMode) {
        mExternalVideoSource = enable;
        mExternalVideoSourceIsTexture = enable && useTexture && isTextureEncodeSupported();
        // TODO 第三个参数没还没管
    }

    @Override
    public boolean pushExternalVideoFrame(ConfVideoFrame mFrame) {
        if (!mIsEnableVideoMode && (mFrame == null) || (mFrame.format == 12)) {
            return false;
        }

        Object obj = handleVideoModule(new VideoModuleConfig(TTTModuleConstants.VIDEO_EXTERNAL_VIDEO_FRAME, new Object[]{mFrame, mWorkerThread.getContext()}));
        return obj != null && (boolean) obj;
    }

    @Override
    public int adjustAudioMixingVolume(int volume) {
        if (!mIsInRoom) {
            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }
        MyAudioApi.getInstance(mWorkerThread.getContext()).adjustAudioFileVolumeScale(volume);
        return FUNCTION_SUCCESS;
    }

    @Override
    public int configPublisher(PublisherConfiguration config) {
        if (config == null) {
            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }

        String pushUrl = config.getPushUrl();
        if (TextUtils.isEmpty(pushUrl)) {
            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }
        GlobalConfig.mPushUrl = pushUrl;
        return FUNCTION_SUCCESS;
    }

    @Override
    public void sendChatMessage(long nDstUserID, int type, String sSeqID, String sData) {
        EnterConfApi.getInstance().sendChat(nDstUserID, type, sSeqID, sData);
    }

    @Override
    public void startRecordChatAudio() {
        EnterConfApi.getInstance().startRecordChatAudio();
    }

    @Override
    public int stopRecordAndSendChatAudio(long nDstUserID) {
        return EnterConfApi.getInstance().stopRecordAndSendChatAudio(nDstUserID);
    }

    @Override
    public void cancleRecordChatAudio() {
        EnterConfApi.getInstance().cancleRecordChatAudio();
    }

    @Override
    public void playChatAudio(String audioPath) {
        EnterConfApi.getInstance().playChatAudio(audioPath);
    }

    @Override
    public void stopChatAudio() {
        EnterConfApi.getInstance().stopChatAudio();
    }

    @Override
    public boolean isChatAudioPlaying() {
        return EnterConfApi.getInstance().isChatAudioPlaying();
    }

    @Override
    public void shareScreenRecorder(boolean isShare) {
        if (isShare) {
            GlobalConfig.mIsScreenRecordShare.set(true);
        } else {
            GlobalConfig.mIsScreenRecordShare.set(false);
        }
    }

    @Override
    public void tryScreenRecorder(Activity mActivity) {
        try {
            mRecorderLock.lock();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return;
            }

            if (mScreenCapture == null) {
                mScreenCapture = new ScreenCapture(mActivity);
            }

            if (mScreenCapture.isRecording()) {
                return;
            }
            tryRecordScreen();
        } finally {
            mRecorderLock.unlock();
        }
    }

    @Override
    public boolean startScreenRecorder(Intent data, ScreenRecordConfig mConfig) {
        try {
            mRecorderLock.lock();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return false;
            }

            if (mScreenCapture == null) {
                return false;
            }

            EncoderConfig mEncoderConfig = new EncoderConfig(getFile(),
                    mConfig.mRecordWidth, mConfig.mRecordHeight,
                    mConfig.mRecordBitRate, mConfig.mRecordFrameRate, 1);
            mScreenCapture.startProjection(data, mEncoderConfig);
            mScreenCapture.attachRecorder(mEncoderConfig);
            return true;
        } finally {
            mRecorderLock.unlock();
        }
    }

    @Override
    public void stopScreenRecorder() {
        try {
            mRecorderLock.lock();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return;
            }

            if (mScreenCapture != null && mScreenCapture.isProjecting()) {
                mScreenCapture.stopProjection();
                mScreenCapture = null;
            }
        } finally {
            mRecorderLock.unlock();
        }
    }

    @Override
    public void speechRecognition(Context context, String path) {
        ExternalChatModule.getInstance().handleActionEvent(ExternalChatModule.ACTION_SPEECH_RECOGNITION, context, path);
    }

    private void processWaitFor(Process process) {
        InputStream stderr = process.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line;
        try {
            while ((line = br.readLine()) != null)
                System.out.println(line);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        File file = new File(path,
                System.currentTimeMillis() + ".temp");
        PviewLog.i("System Image Path : " + path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    private void tryRecordScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScreenCapture.setMediaProjectionReadyListener(new ScreenCapture.OnMediaProjectionReadyListener() {
                @Override
                public void onMediaProjectionReady(MediaProjection mediaProjection) {
                    PviewLog.i("录屏初始化成功，准备开始");
                }
            });

            mScreenCapture.setRecordCallback(new RecordCallback() {
                @Override
                public void onRecordSuccess(String filePath, long duration) {
                    PviewLog.i("录屏成功结束。filePath : " + filePath + " | duration : " + duration);
                }

                @Override
                public void onRecordFailed(Throwable e, long duration) {
                    PviewLog.i("录屏发生错误，失败。Throwable : " + e.getLocalizedMessage() + " | duration : " + duration);
                }

                @Override
                public void onRecordedDurationChanged(int s) {
                    PviewLog.i("录屏时间。time : " + s);
                    JniWorkerThread mJniWorkerThread = GlobalHolder.getInstance().getWorkerThread();
                    mJniWorkerThread.sendMessage(JniWorkerThread.JNI_CALL_BACK_ON_RECORD_TIME, new Object[]{s});
                }
            });
            mScreenCapture.requestScreenCapture();
        }
    }

    @Override
    public int insertH264SeiContent(String content) {
        if (TextUtils.isEmpty(content)) {
            return ERROR_FUNCTION_ERROR_EMPTY_VALUE;
        }
        VideoEncoder.getInstance().insertH264SeiContent(content.getBytes());
        return FUNCTION_SUCCESS;
    }


    @Override
    public byte[] getRemoteBuffer(String devId) {
        RemoteSurfaceView remoteSurfaceView = RemotePlayerManger.getInstance().getRemoteSurfaceView(devId);
        return remoteSurfaceView == null ? null : RemotePlayerManger.getInstance().getRemoteSurfaceView(devId).getRemoteBuffer();
    }

    @Override
    public int getRemoteWidth(String devId) {
        RemoteSurfaceView remoteSurfaceView = RemotePlayerManger.getInstance().getRemoteSurfaceView(devId);
        return remoteSurfaceView == null ? null : RemotePlayerManger.getInstance().getRemoteSurfaceView(devId).getRemoteWidth();
    }

    @Override
    public int getRemoteHeight(String devId) {
        RemoteSurfaceView remoteSurfaceView = RemotePlayerManger.getInstance().getRemoteSurfaceView(devId);
        return remoteSurfaceView == null ? null : RemotePlayerManger.getInstance().getRemoteSurfaceView(devId).getRemoteHeight();
    }

    @Override
    public byte[] getLocalBuffer() {
        return MyVideoApi.getInstance().getLocalBuffer();
    }

    @Override
    public int getLocalWidth() {
        return MyVideoApi.getInstance().getRenderWidth();
    }

    @Override
    public int getLocalHeight() {
        return MyVideoApi.getInstance().getRenderHeight();
    }

}
