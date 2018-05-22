package com.wushuangtech.gamelive;

/**
 * Created by Iverson on 2018/4/2 下午2:33
 * 此类用于：用于第三方的数据载体
 */

public class RoomInfo {

    private String mRoomId;//房间id
    private String mRoomHostId;//主播id
    private long mUserBanlance;//用户money
    private int mRoomRole;//1:主播  2：观众
    private String mUserId;//用户userid
    private String mUserNickname;//用户昵称
    private String mUserAvatar;//用户的头像

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        this.mRoomId = roomId;
    }

    public String getRoomHostId() {
        return mRoomHostId;
    }

    public void setRoomHostId(String roomHostId) {
        this.mRoomHostId = roomHostId;
    }

    public long getUserBanlance() {
        return mUserBanlance;
    }

    public void setUserBanlance(long userBanlance) {
        this.mUserBanlance = userBanlance;
    }

    public int getRoomRole() {
        return mRoomRole;
    }

    public void setRoomRole(int roomRole) {
        this.mRoomRole = roomRole;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getUserNickname() {
        return mUserNickname;
    }

    public void setUserNickname(String userNickname) {
        this.mUserNickname = userNickname;
    }

    public String getUserAvatar() {
        return mUserAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.mUserAvatar = userAvatar;
    }
}
