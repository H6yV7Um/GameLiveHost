package com.wushuangtech.wstechapi.internal;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import com.wushuangtech.bean.LocalVideoStats;
import com.wushuangtech.bean.RemoteVideoStats;
import com.wushuangtech.bean.RtcStats;
import com.wushuangtech.library.Constants;
import com.wushuangtech.library.LocalSDKConstants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.TTTRtcEngineEventHandler;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import java.util.LinkedList;

/**
 * SDK原生游戏接口类,执行游戏相关的SDK各种功能
 */
public final class TTTRtcUnity {

    /**
     * 主体SDK的实例对象
     */
    private TTTRtcEngine mTTTRtcEngine;

    /**
     * 游戏SDK的实例对象
     */
    private static TTTRtcUnity mInstance = null;

    private Context mContext;
    private long mLocalId;
    private int mRoomMode = Constants.CHANNEL_PROFILE_GAME_FREE_MODE;
    private LinkedList<String> mMessage = new LinkedList<>();
    private boolean isJoined = false;
    private int mLocalWidth, mLocalHeight;

    /**
     * 游戏SDK的构造函数
     *
     * @param context 安卓程序的上下文
     * @param mAppID   SDK初始化需要用到的app id
     */
    private TTTRtcUnity(Context context, String mAppID) {
        mContext = context;
        EngineHandler engineHandler = new EngineHandler();
        mTTTRtcEngine = TTTRtcEngine.create(mContext, mAppID, engineHandler);
        mTTTRtcEngine.setTTTRtcEngineEventHandler(engineHandler);

        mTTTRtcEngine.setChannelProfile(mRoomMode);
        mTTTRtcEngine.setClientRole(Constants.CLIENT_ROLE_ANCHOR, null);
    }

    /**
     * 初始化游戏SDK，在程序生命周期中只需要调用一次，即便执行destroy函数也不需要调用
     *
     * @param mContext 安卓程序的上下文
     * @param mAppID   SDK初始化需要用到的app id
     * @return TTTRtcEngineForGamming
     * 返回游戏SDK的实例对象
     */
    public static synchronized TTTRtcUnity create(Context mContext, String mAppID) {
        if (mInstance == null) {
            synchronized (TTTRtcUnity.class) {
                if (mInstance == null) {
                    mInstance = new TTTRtcUnity(mContext, mAppID);
                }
            }
        }
        return mInstance;
    }

    /**
     * SDK的反初始化操作
     */
    public synchronized void destroy() {
    }

    /**
     * 查询 SDK 版本号
     *
     * @return 返回 SDK 版本号字符串
     */
    public String getVersion() {
        return mTTTRtcEngine.getVersion();
    }

    /**
     * 加入视频通信房间，需要异步调用
     *
     * @param channelName  频道名字
     * @param optionalUid  用户ID
     * @return 0代表方法调用成功，其他代表失败。
     */
    public synchronized int joinChannel(String channelName, int optionalUid) {
        return mTTTRtcEngine.joinChannel("", channelName, optionalUid);
    }

    /**
     * 加入视频通信房间，需要异步调用
     *
     * @param channelKey   频道key
     * @param channelName  频道名字
     * @param optionalUid  用户ID
     * @return 0代表方法调用成功，其他代表失败。
     */
    public synchronized int joinChannel(String channelKey, String channelName, int optionalUid) {
        mTTTRtcEngine.setChannelProfile(mRoomMode);
        disableAudio();
        enableAudio();
        mLocalId = optionalUid;
        return mTTTRtcEngine.joinChannel(channelKey, channelName, optionalUid);
    }

    /**
     * 离开视频通信房间
     *
     * @return 0代表方法调用成功，其他代表失败。
     */
    public synchronized int leaveChannel() {
        Log.d("zhx", "leaveChannel: ");
        return this.mTTTRtcEngine.leaveChannel();
    }

    public int enableVideo() {
        Log.d("zhx", "enableVideo: ");
        return this.mTTTRtcEngine.enableVideo();
    }

    public int disableVideo() {
        return this.mTTTRtcEngine.disableVideo();
    }

    public int enableLocalVideo(boolean enabled) {
        return this.mTTTRtcEngine.enableLocalVideo(enabled);
    }

    public int startPreview() {
        return this.mTTTRtcEngine.startPreview();
    }

    public int stopPreview() {
        return this.mTTTRtcEngine.stopPreview();
    }

    public int setEnableSpeakerphone(boolean enabled) {
        return this.mTTTRtcEngine.setEnableSpeakerphone(enabled);
    }

    public boolean isSpeakerphoneEnabled() {
        return this.mTTTRtcEngine.isSpeakerphoneEnabled();
    }

    public int setDefaultAudioRoutetoSpeakerphone(boolean defaultToSpeaker) {
        return this.mTTTRtcEngine.setDefaultAudioRouteToSpeakerphone(defaultToSpeaker);
    }

    public int enableAudioVolumeIndication(int interval, int smooth) {
        return this.mTTTRtcEngine.enableAudioVolumeIndication(interval, smooth);
    }

    public int startAudioMixing(String filePath, boolean loopback, boolean replace, int cycle) {
        return this.mTTTRtcEngine.startAudioMixing(filePath, loopback, replace, cycle);
    }

    public int stopAudioMixing() {
        return this.mTTTRtcEngine.stopAudioMixing();
    }

    public int pauseAudioMixing() {
        return this.mTTTRtcEngine.pauseAudioMixing();
    }

    public int resumeAudioMixing() {
        return this.mTTTRtcEngine.resumeAudioMixing();
    }

    public int adjustAudioMixingVolume(int volume) {
        return this.mTTTRtcEngine.adjustAudioMixingVolume(volume);
    }

    public int getAudioMixingDuration() {
        return this.mTTTRtcEngine.getAudioMixingDuration();
    }

    public int getAudioMixingCurrentPosition() {
        return this.mTTTRtcEngine.getAudioMixingCurrentPosition();
    }

    public int muteLocalAudioStream(boolean mute) {
        return this.mTTTRtcEngine.muteLocalAudioStream(mute);
    }

    public int muteAllRemoteAudioStreams(boolean mute) {
        return this.mTTTRtcEngine.muteAllRemoteAudioStreams(mute);
    }

    public int muteRemoteAudioStream(int uid, boolean mute) {
        return this.mTTTRtcEngine.muteRemoteAudioStream(uid, mute);
    }

    public int switchCamera() {
        return this.mTTTRtcEngine.switchCamera();
    }

    public int setVideoProfile(int profile, boolean swapWidthAndHeight) {
        return this.mTTTRtcEngine.setVideoProfile(profile, swapWidthAndHeight);
    }

    public int muteAllRemoteVideoStreams(boolean muted) {
        return this.mTTTRtcEngine.muteAllRemoteVideoStreams(muted);
    }

    public int muteLocalVideoStream(boolean muted) {
        return this.mTTTRtcEngine.muteLocalVideoStream(muted);
    }

    public int muteRemoteVideoStream(long uid, boolean muted) {
        return this.mTTTRtcEngine.muteRemoteVideoStream(uid, muted);
    }

    /**
     * 设置频道模式
     *
     * @param profile 频道模式 {@link Constants#CHANNEL_PROFILE_COMMUNICATION}
     * @return 0代表方法调用成功，其他代表失败。see {@link LocalSDKConstants#FUNCTION_SUCCESS}
     */
    public int setChannelProfile(int profile) {
        mRoomMode = profile;
        return LocalSDKConstants.FUNCTION_SUCCESS;
    }

    public int setLogFile(String filePath) {
        return this.mTTTRtcEngine.setLogFile(filePath);
    }

    public int setLogFilter(int filter) {
        return this.mTTTRtcEngine.setLogFilter(filter);
    }

    public int getMessageCount() {
        return mMessage.size();
    }

    public String getMessage() {
        return mMessage.poll();
    }

    private class EngineHandler extends TTTRtcEngineEventHandler {

        @Override
        public void onError(int err) {
            Log.d("zhx", "onError: " + err);
            mMessage.add("onError\t" + err);
        }

        @Override
        public void onConnectionLost() {
            Log.d("zhx", "onConnectionLost: ");
            mMessage.add("onConnectionLost");
        }

        @Override
        public void onJoinChannelSuccess(String channel, long uid) {
            Log.d("zhx", "onJoinChannelSuccess channel: " + channel + "uid:" + uid + " mLocalWidth:" + mLocalWidth + " mLocalHeight:" + mLocalHeight);
            isJoined = true;
            mMessage.add("onJoinChannelSuccess\t" + channel + "\t" + uid + "\t0");

            if (mLocalWidth != 0 && mLocalHeight != 0)
                mMessage.add("onFirstLocalVideoFrame\t" + mLocalId + "\t" + mLocalWidth + "\t" + mLocalHeight + "\t" + 0);
        }

        @Override
        public void onUserJoined(long nUserId, int identity) {
            Log.d("zhx", "onUserJoined: ");
            SurfaceView mSurfaceView = mTTTRtcEngine.CreateRendererView(mContext);
            mTTTRtcEngine.setupRemoteVideo(new VideoCanvas(nUserId, Constants.RENDER_MODE_HIDDEN, mSurfaceView));
            mMessage.add("onUserJoined\t" + nUserId + "\t" + identity);
        }

        @Override
        public void onFirstLocalVideoFrame(int width, int height) {
            Log.d("zhx", "onFirstLocalVideoFrame: " + width + " " + height);
            mLocalWidth = width;
            mLocalHeight = height;
            if (isJoined)
                mMessage.add("onFirstLocalVideoFrame\t" + mLocalId + "\t" + width + "\t" + height + "\t" + 0);
        }

        @Override
        public void onFirstRemoteVideoFrame(long uid, int width, int height) {
            Log.d("zhx", "onFirstRemoteVideoFrame: uid:" + uid + " " + width + " " + height);
            mMessage.add("onFirstRemoteVideoFrameDecoded\t" + uid + "\t" + width + "\t" + height + "\t" + 0);
        }

        @Override
        public void onUserOffline(long nUserId, int reason) {
            Log.d("zhx", "onUserOffline: ");
            mMessage.add("onUserOffline\t" + nUserId + "\t" + reason);
        }

        @Override
        public void onLocalVideoStats(LocalVideoStats stats) {
//            Log.d("zhx", "onLocalVideoStats: ");
//            mMessage.add("onLocalVideoStats");
        }

        @Override
        public void onRemoteVideoStats(RemoteVideoStats stats) {
//            Log.d("zhx", "onRemoteVideoStats: ");
//            mMessage.add("onRemoteVideoStats");
        }

        @Override
        public void onCameraReady() {
            Log.d("zhx", "onCameraReady: ");
            mMessage.add("onCameraReady");
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            Log.d("zhx", "onLeaveChannel: ");
            isJoined = false;
//            mMessage.add("onLeaveChannel");
        }

        @Override
        public void onAudioVolumeIndication(long nUserID, int audioLevel, int audioLevelFullRange) {
//            Log.d("zhx", "onAudioVolumeIndication: ");
            mMessage.add("onReportAudioVolumeIndications\t" + nUserID + "\t" + audioLevel);
        }

        @Override
        public void onRtcStats(RtcStats stats) {
//            Log.d("zhx", "onRtcStats: ");
//            mMessage.add("onRtcStats");
        }

        @Override
        public void onUserMuteAudio(long uid, boolean muted) {
            Log.d("zhx", "onUserMuteAudio: ");
            mMessage.add("onAudioMutedByPeer\t" + uid + "\t" + muted);
        }

        @Override
        public void onAudioRouteChanged(int routing) {
            Log.d("zhx", "onAudioRouteChanged: ");
            mMessage.add("onAudioRouteChanged\t" + routing);
        }
    }

    public int enableAudio() {
        return this.mTTTRtcEngine.enableAudio();
    }

    public int disableAudio() {
        return this.mTTTRtcEngine.disableAudio();
    }


    /*--------------------------------------------------------------Unity JNI层接口---------------------------------------------------------------*/
    private byte[] getRemoteBuffer(String devId) {
        return mTTTRtcEngine.getRemoteBuffer(devId);
    }

    private byte[] getLocalBuffer() {
        return mTTTRtcEngine.getLocalBuffer();
    }

    private int getRemoteWidth(String devId) {
        return mTTTRtcEngine.getRemoteWidth(devId);
    }

    private int getRemoteHeight(String devId) {
        return mTTTRtcEngine.getRemoteHeight(devId);
    }

    private int getLocalWidth() {
        return mTTTRtcEngine.getLocalWidth();
    }

    private int getLocalHeight() {
        return mTTTRtcEngine.getLocalHeight();
    }

    public byte[] getDeviceBuffer(int uid) {
        return uid == mLocalId ? getLocalBuffer() : getRemoteBuffer(String.valueOf(uid));
    }

    public int getDeviceWidth(int uid) {
        return uid == mLocalId ? getLocalWidth() : getRemoteWidth(String.valueOf(uid));
    }

    public int getDeviceHeight(int uid) {
        return uid == mLocalId ? getLocalHeight() : getRemoteHeight(String.valueOf(uid));
    }
    /*--------------------------------------------------------------Unity JNI层接口---------------------------------------------------------------*/

}
