package com.wushuangtech.library;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.wushuangtech.library.Constants.AUDIO_ROUTE_SPEAKER;
import static com.wushuangtech.library.Constants.VIDEO_PROFILE_240P;

/**
 * Created by wangzhiguo on 17/6/9.
 */

public class GlobalConfig {

    public static final String SDK_VERSION_NAME = "2.0.0 (0209)";
    /**
     * 频道模式
     */
    public static int mCurrentChannelMode = Constants.CHANNEL_PROFILE_COMMUNICATION;
    /**
     * APP ID
     */
    public static String mAppID;
    /**
     * 自己的ID
     */
    public static long mLocalUserID;
    /**
     * 成功进入房间时的时间戳
     */
    public static long mStartRoomTime;
    /**
     * 是否已经进入房间
     */
    public static boolean mIsInRoom;
    /**
     * 本地视频编码和预览的模式
     */
    public static int mLocalCameraShowMode = Constants.RENDER_MODE_HIDDEN;
    /**
     * 本地视频质量预设等级
     */
    public static int mLocalVideoMass = VIDEO_PROFILE_240P;
    /**
     * 自己是否是主播
     */
    public static int mIsLocalHost = Constants.CLIENT_ROLE_ANCHOR;
    /**
     * 是否启用视频模式，true启用，false禁用
     */
    public static boolean mIsEnableVideoMode;
    /**
     * 是否启用音频模式，true启用，false禁用
     */
    public static boolean mIsEnableAudioMode;
    /**
     * 是否禁用本地音频数据的发送
     */
    public static boolean mIsMuteLocalAudio;
    /**
     * 是否禁用本地视频数据的发送
     */
    public static boolean mIsMuteLocalVideo;
    /**
     * 是否是耳机输出优先，true是耳机优先，false扬声器优先
     */
    public static boolean mIsHeadsetPriority = true;
    /**
     * 当前是设置耳机输出还是扬声器输出
     */
    public static boolean mIsSpeakerphoneEnabled = true;
    /**
     * 程序默认音频路由
     */
    public static int mDefaultAudioRoute = AUDIO_ROUTE_SPEAKER;
    /**
     * 程序当前播放伴奏，该伴奏的时长
     */
    public static int mCurrentAudioMixingDuration;
    /**
     * 程序当前播放伴奏的时间进度
     */
    public static int mCurrentAudioMixingPosition;
    /**
     * 是否使用外部视频源
     */
    public static boolean mExternalVideoSource;
    /**
     * 外部视频源是否使用 Texture 作为输入
     */
    public static boolean mExternalVideoSourceIsTexture;

    public static boolean trunOnCallback;

    public static String mCDNPullAddressPrefix = "rtmp://pull.3ttech.cn/sdk/";

    public static String mCDNAPullddress;

    public static String mPushUrl;

    public static AtomicBoolean mIsScreenRecordShare;

    public static boolean mAudioRecordFailed;
}
