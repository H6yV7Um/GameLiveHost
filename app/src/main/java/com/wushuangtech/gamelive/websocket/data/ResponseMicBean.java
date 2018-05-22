package com.wushuangtech.gamelive.websocket.data;

import java.util.List;

/**
 * Created by mrliu on 2018/1/12.
 * 此类用于:响应连麦
 */

public class ResponseMicBean {
    private String messageType ;


    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * data : {"userList":[{"avatar":"http://img5.imgtn.bdimg.com/it/u=3208258265,2930130286&fm=27&gp=0.jpg","nickName":"魁拔","introduction":"连个麦可好","userId":"2"}]}
     */

    private DataBean data;

    @Override
    public String toString() {
        return "ResponseMicBean{" +
                "data=" + data +
                '}';
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<UserListBean> userList;

        public List<UserListBean> getUserList() {
            return userList;
        }

        public void setUserList(List<UserListBean> userList) {
            this.userList = userList;
        }

        public static class UserListBean {
            /**
             * avatar : http://img5.imgtn.bdimg.com/it/u=3208258265,2930130286&fm=27&gp=0.jpg
             * nickName : 刘景
             * introduction : 连个麦可好
             * userId : 2
             */

            private String avatar;
            private String nickName;
            private String introduction;
            private String userId;
            private String type ;

            @Override
            public String toString() {
                return "UserListBean{" +
                        "avatar='" + avatar + '\'' +
                        ", nickName='" + nickName + '\'' +
                        ", introduction='" + introduction + '\'' +
                        ", userId='" + userId + '\'' +
                        ", type='" + type + '\'' +
                        '}';
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

            public String getIntroduction() {
                return introduction;
            }

            public void setIntroduction(String introduction) {
                this.introduction = introduction;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }
}
