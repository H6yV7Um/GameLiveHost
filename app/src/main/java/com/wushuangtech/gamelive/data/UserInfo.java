package com.wushuangtech.gamelive.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Iverson on 2016/12/27 下午4:31
 * 此类用于：个人信息bean
 */

public class UserInfo implements Parcelable {
    /**
     * id : 200004
     * applicationId : 0
     * userName :
     * realName :
     * idCard :
     * avatar :
     * nickName :
     * password : e10adc3949ba59abbe56e057f20f883e
     * mobile : 18610900467
     * description :
     * wxOpenId :
     * wxUnionId :
     * province :
     * city :
     * longitude : 0.000000
     * latitude : 0.000000
     * extra :
     * isValid : 1
     * liveTime : 0
     * balance : 0
     * level : 1
     * created : 0
     * updated : 0
     * followers_cnt :
     * followees_cnt :
     */

    private String userId;
    private String applicationId;
    private String userName;
    private String realName;
    private String idCard;
    private String avatar;
    private String nickName;
    private String password;
    private String mobile;
    private String description;
    private String wxOpenId;
    private String wxUnionId;
    private String province;
    private String city;
    private String longitude;
    private String latitude;
    private String extra;
    private String isValid;
    private String liveTime;
    private String balance;
    private String level;
    private String created;
    private String updated;
    private String followers_cnt;
    private String followees_cnt;
    private int isAttention ;
    private int isLive ;
    public UserInfo(){}

    protected UserInfo(Parcel in) {
        userId = in.readString();
        applicationId = in.readString();
        userName = in.readString();
        realName = in.readString();
        idCard = in.readString();
        avatar = in.readString();
        nickName = in.readString();
        password = in.readString();
        mobile = in.readString();
        description = in.readString();
        wxOpenId = in.readString();
        wxUnionId = in.readString();
        province = in.readString();
        city = in.readString();
        longitude = in.readString();
        latitude = in.readString();
        extra = in.readString();
        isValid = in.readString();
        liveTime = in.readString();
        balance = in.readString();
        level = in.readString();
        created = in.readString();
        updated = in.readString();
        followers_cnt = in.readString();
        followees_cnt = in.readString();
        isAttention = in.readInt();
        isLive = in.readInt();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public String getId() {
        return userId;
    }

    public void setId(String userId) {
        this.userId = userId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWxOpenId() {
        return wxOpenId;
    }

    public void setWxOpenId(String wxOpenId) {
        this.wxOpenId = wxOpenId;
    }

    public String getWxUnionId() {
        return wxUnionId;
    }

    public void setWxUnionId(String wxUnionId) {
        this.wxUnionId = wxUnionId;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getIsValid() {
        return isValid;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid;
    }

    public String getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(String liveTime) {
        this.liveTime = liveTime;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getFollowers_cnt() {
        return followers_cnt;
    }

    public void setFollowers_cnt(String followers_cnt) {
        this.followers_cnt = followers_cnt;
    }

    public String getFollowees_cnt() {
        return followees_cnt;
    }

    public void setFollowees_cnt(String followees_cnt) {
        this.followees_cnt = followees_cnt;
    }

    public int getIsAttention() {
        return isAttention;
    }

    public void setIsAttention(int isAttention) {
        this.isAttention = isAttention;
    }

    public int getIsLive() {
        return isLive;
    }

    public void setIsLive(int isLive) {
        this.isLive = isLive;
    }

    public static Creator<UserInfo> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(applicationId);
        dest.writeString(userName);
        dest.writeString(realName);
        dest.writeString(idCard);
        dest.writeString(avatar);
        dest.writeString(nickName);
        dest.writeString(password);
        dest.writeString(mobile);
        dest.writeString(description);
        dest.writeString(wxOpenId);
        dest.writeString(wxUnionId);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(longitude);
        dest.writeString(latitude);
        dest.writeString(extra);
        dest.writeString(isValid);
        dest.writeString(liveTime);
        dest.writeString(balance);
        dest.writeString(level);
        dest.writeString(created);
        dest.writeString(updated);
        dest.writeString(followers_cnt);
        dest.writeString(followees_cnt);
        dest.writeInt(isAttention);
        dest.writeInt(isLive);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", userName='" + userName + '\'' +
                ", realName='" + realName + '\'' +
                ", idCard='" + idCard + '\'' +
                ", avatar='" + avatar + '\'' +
                ", nickName='" + nickName + '\'' +
                ", password='" + password + '\'' +
                ", mobile='" + mobile + '\'' +
                ", description='" + description + '\'' +
                ", wxOpenId='" + wxOpenId + '\'' +
                ", wxUnionId='" + wxUnionId + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", extra='" + extra + '\'' +
                ", isValid='" + isValid + '\'' +
                ", liveTime='" + liveTime + '\'' +
                ", balance='" + balance + '\'' +
                ", level='" + level + '\'' +
                ", created='" + created + '\'' +
                ", updated='" + updated + '\'' +
                ", followers_cnt='" + followers_cnt + '\'' +
                ", followees_cnt='" + followees_cnt + '\'' +
                ", isAttention='" + isAttention + '\'' +
                '}';
    }



}
