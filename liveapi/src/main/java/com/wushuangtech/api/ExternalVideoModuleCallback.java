package com.wushuangtech.api;


/**
 * 视频处理模块需要实现该接口
 * 内置视频模块MyVideo已实现该接口。
 * 用户也可以自己实现该接口，替换内置的视频模块
 */
public interface ExternalVideoModuleCallback {

    /**
     * 视频帧类型，暂不支持B帧
     */
    enum VideoFrameType {
        FRAMETYPE_INVALID,
        FRAMETYPE_SPS_PPS,
        FRAMETYPE_I,
        FRAMETYPE_P
    }

    /**
     * 开始采集视频，调用该接口后，视频模块需要向VideoSender发送编码后的视频数据
     */
    boolean startCapture();

    /**
     * 停止采集视频，调用该接口后，视频模块停止向VideoSender发送编码后的视频数据
     */
    boolean stopCapture();

    /**
     * 视频模块接收数据，用于解码并显示
     */
    void receiveVideoData(byte[] data, String devID, long timeStamp, int width, int height, VideoFrameType frameType);

    /**
     * 添加视频数据接收对象
     */
    void addVideoSender(VideoSender sender);

    /**
     * 移除视频数据接收对象
     */
    void removeVideoSender(VideoSender sender);

    /**
     * 获取编码字节数
     */
    int getEncodeDataSize();

    /**
     * 获取编码帧数
     */
    int getEncodeFrameCount();

    /**
     * 获取采集帧数
     */
    int getCaptureFrameCount();

    /**
     * 获取解码帧数
     */
    int getDecodeFrameCount();

    /**
     * 获取播放帧数
     */
    int getRenderFrameCount();

    /**
     * 获取编码视频尺寸
     * width at index of 0
     * height at index of 1
     */
    int[] getEncodeSize();

    boolean isCapturing();
}
