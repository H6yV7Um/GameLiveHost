package com.wushuangtech.gamelive.domin;

import android.support.annotation.NonNull;

import com.wushuangtech.gamelive.data.LoginInfo;
import com.wushuangtech.gamelive.data.UserInfo;


/**
 * Created by 刘景 on 2017/05/11.
 */

public class DataManager {
    private static DataManager instance;
    private LoginInfo mLoginInfo;
    private UserInfo mUserInfo ;
    private DataManager() {}
    public static DataManager getInstance() {
        if (instance == null) {
            synchronized (SPHelper.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }
    public void saveUserinfo(@NonNull UserInfo userInfo){
        mUserInfo = userInfo ;
        SPHelper.setUserInfo(mUserInfo);
    }
    public void saveLoginInfo(@NonNull LoginInfo loginInfo) {
        //Update cached object!
        mLoginInfo = loginInfo;
        SPHelper.setLoginInfo(loginInfo);
//        CrashReport.setUserId(loginInfo.getUserId());
    }

    //保存微信登录unionId
    public void removeUserInfo(){
        mUserInfo = null;
        SPHelper.removeUserInfo();
    }

    public UserInfo getmUserInfo() {
        if (mUserInfo == null) {
            mUserInfo = SPHelper.getUserInfo();
        }
        return mUserInfo;
    }
    public void clearLoginInfo(){
        mLoginInfo = null;
        SPHelper.removeLoginInfo();
    }

    public LoginInfo getLoginInfo() {
        if (mLoginInfo == null) {
            mLoginInfo = SPHelper.getLoginInfo();
        }
        return mLoginInfo;
    }

}
