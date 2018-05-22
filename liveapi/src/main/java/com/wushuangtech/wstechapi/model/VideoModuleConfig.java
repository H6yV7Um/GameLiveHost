package com.wushuangtech.wstechapi.model;

/**
 * Created by wangzhiguo on 17/11/30.
 */

public class VideoModuleConfig {

    public int eventType;
    public Object[] objs;

    public VideoModuleConfig(int eventType, Object[] objs) {
        this.eventType = eventType;
        this.objs = objs;
    }
}
