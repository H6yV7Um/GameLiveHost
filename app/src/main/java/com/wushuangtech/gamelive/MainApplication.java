package com.wushuangtech.gamelive;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.wushuangtech.gamelive.domin.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Iverson on 2018/4/3 下午2:33
 * 此类用于：
 */

public class MainApplication extends Application {

    private Context mContext;
    private static MainApplication instance;
    public static List<Activity> activities = new ArrayList<Activity>();

    private void MainApplication(){}
    public static MainApplication getInstance() {
        if (instance == null) {
            instance = new MainApplication();
        }
        return  instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        RoomSDK.init(mContext);
        SPUtils.init(mContext,"gamelive");
        Fresco.initialize(mContext);
    }

    public void addActivity(Activity activity) {
        if(activity == null) return;
        activities.add(activity);
        Log.e("activityName: ","activityName add:"+activity.toString());
    }

    public void deleActvitity(Activity activity){
        if(activity == null) return;
        activities.remove(activity);
        Log.e("activityName: ","activityName deleActvitity: "+activity.toString());
    }
    public static <T> boolean isContainActivity(Class<T> cls){
        for (Activity activity : activities){
            if(activity.getClass().equals(cls)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mContext != null) {
            RoomSDK.release();
            mContext = null;
        }
    }
}
