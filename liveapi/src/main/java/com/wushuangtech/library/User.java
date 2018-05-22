package com.wushuangtech.library;


public class User {

    private long mUserId;
    private int mUserIdentity = Constants.CLIENT_ROLE_AUDIENCE;
    private int mLastReceiveAudioDatas;

    public User(long mUserId) {
        this.mUserId = mUserId;
    }

    public int getUserIdentity() {
        return mUserIdentity;
    }

    public long getmUserId() {
        return mUserId;
    }

    public int getLastReceiveAudioDatas() {
        return mLastReceiveAudioDatas;
    }

    public void setUserIdentity(int mUserIdentity) {
        this.mUserIdentity = mUserIdentity;
    }

    public void setLastReceiveAudioDatas(int mLastReceiveAudioDatas) {
        this.mLastReceiveAudioDatas = mLastReceiveAudioDatas;
    }
}
