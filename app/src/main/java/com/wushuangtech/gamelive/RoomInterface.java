package com.wushuangtech.gamelive;

/**
 * Created by Iverson on 2018/4/2 下午2:32
 * 此类用于：用于对接第三方的顶层接口
 */

public interface RoomInterface {


     RoomInfo getRoomInfo();

     void sendGift(int num,String giftName,String totalMoney,String sendUserId,String hostId);

}
