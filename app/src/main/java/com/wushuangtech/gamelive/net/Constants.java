package com.wushuangtech.gamelive.net;

/**
 * Created by Iverson on 2016/12/23 下午5:44
 *  此类用于  放置一些常量的数据和网址
 */

public class Constants {


    public static final String MAIN_HOST_FOR_PING = "118.25.15.37";
    //服务器root地址
    public static String MAIN_HOST_URL = "http://" + MAIN_HOST_FOR_PING;
    /**
     * 直播间心心防抖动时间。
     * 1s <= 50次点击。
     */
    public static final int LIVE_ROOM_HEART_THROTTLE = 200;

    /**
     * Web Socket 服务器地址。
     */
    public static String SOCKET_URL = "ws://118.25.15.37:9505";

    public static final int VIEW_THROTTLE_TIME_SHORT = 5;


    public static final String GET_CHECK_CODE_TO_REGIEST = "register";
    public static final String GET_CHECK_CODE_TO_RESET = "reset";

}
