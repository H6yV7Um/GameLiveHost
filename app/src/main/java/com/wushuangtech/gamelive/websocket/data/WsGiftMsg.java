package com.wushuangtech.gamelive.websocket.data;

/**
 * Created by mrliu on 2018/1/12.
 * 此类用于:收到礼物的通知
 */

public class WsGiftMsg extends WsResponse {


    /**
     * data : {"roomId":"100008","userId":"200004","userIdTo":"100008","giftId":"100001","price":"10","num":1,"balance":99990}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * roomId : 100008
         * userId : 200004
         * userIdTo : 100008
         * giftId : 100001
         * price : 10
         * num : 1
         * balance : 99990
         */

        private String roomId;
        private String userId;
        private String userIdTo;
        private String giftId;
        private String price;
        private int num;
        private int balance;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserIdTo() {
            return userIdTo;
        }

        public void setUserIdTo(String userIdTo) {
            this.userIdTo = userIdTo;
        }

        public String getGiftId() {
            return giftId;
        }

        public void setGiftId(String giftId) {
            this.giftId = giftId;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getBalance() {
            return balance;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }
    }
}
