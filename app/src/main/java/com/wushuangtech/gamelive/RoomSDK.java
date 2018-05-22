package com.wushuangtech.gamelive;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.wushuangtech.gamelive.domin.SPUtils;
import com.wushuangtech.gamelive.websocket.WebSocketService;
import com.wushuangtech.room.core.TTTSDK;

/**
 * Created by Iverson on 2018/4/2 下午3:37
 * 此类用于：
 */

public class RoomSDK {

    private static RoomSDK instance;
    private static Context mContext;

    private void RoomSDK(){}



    //初始化sdk数据
    public static void init(Context context) {
        if(instance == null){
            synchronized (RoomSDK.class){
                if (instance == null) {
                    instance = new RoomSDK();
                }
            }
        }
        mContext = context;
        TTTSDK.init(mContext,"555e189bf1278119c78f6b1753bfe4b5");
        SPUtils.init(mContext,"gamelive");
        Fresco.initialize(mContext);
    }

    public static Context getContext(){
        return mContext;
    }


    //释放一些数据
    public static void release(){
        if (mContext != null) {
            mContext = null;
        }
    }
}
