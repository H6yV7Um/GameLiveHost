package com.wushuangtech.gamelive.websocket;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.wushuangtech.gamelive.RoomManager;
import com.wushuangtech.gamelive.RoomSDK;
import com.wushuangtech.gamelive.data.LoginInfo;
import com.wushuangtech.gamelive.domin.DataManager;
import com.wushuangtech.gamelive.websocket.data.ApplyMicBean;
import com.wushuangtech.gamelive.websocket.data.ApplyMicRsqBean;
import com.wushuangtech.gamelive.websocket.data.DisConnectLmRequest;
import com.wushuangtech.gamelive.websocket.data.WsLightHeartRequest;
import com.wushuangtech.gamelive.websocket.data.WsLoginRequest;
import com.wushuangtech.gamelive.websocket.data.WsLogoutRequest;
import com.wushuangtech.gamelive.websocket.data.WsPongRequest;
import com.wushuangtech.gamelive.websocket.data.WsPublicMsgRequest;
import com.wushuangtech.gamelive.websocket.data.WsRequest;
import com.wushuangtech.gamelive.websocket.data.WsSendGiftRequest;


/**
 * Created by 刘景 on 2017/05/11.
 */

public class WsObjectPool {

    private static final String LOG_TAG = WsObjectPool.class.getSimpleName();
    private static Context mContext = RoomSDK.getContext();
    private static SparseArray<WsRequest> requestArray;
    private static final int REQ_LOGIN = 1;
    private static final int REQ_LOGOUT = 2;
    private static final int REQ_SEND_PUB_MSG = 3;
    private static final int APPLY_MIC_REQUEST = 4;
    private static final int APPLY_MIC_RESPONE = 5;
    private static final int REQ_HEARTBEAT = 6;
    private static final int REQ_SENDGIFT = 7;
    private static final int REQ_DISLM = 8;

    private static String nickname;
    private static String userId;
    private static String token;
    private static String ucuid;



    /**
     * 释放所有资源，清空数据。
     */

    public static void release(){
        nickname = null;
        token = null;
        userId = null;
        if (requestArray != null) {
            requestArray.clear();
            requestArray = null;
        }
    }

    /**
     * 为用户初始化对象池。
     */

    public static void init(LoginInfo loginInfo) {
        String username = loginInfo.getNickname();
        String userId = loginInfo.getUserId();
        String token = loginInfo.getToken();

        WsObjectPool.token = token ;
        if(userId == null){
            String androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            WsObjectPool.userId = androidId;
        }else {
            WsObjectPool.userId = userId;
        }
        WsObjectPool.nickname = username;

        requestArray = new SparseArray<>();

        //用户登录
        WsLoginRequest loginRequest = new WsLoginRequest();
        loginRequest.setMessageType(SocketConstants.EVENT_LOGIN);
        loginRequest.setData(new WsLoginRequest.WsLoginRequestData());
        requestArray.put(REQ_LOGIN,loginRequest);

        //用户登出
        WsLogoutRequest logoutRequest = new WsLogoutRequest();
        logoutRequest.setMessageType(SocketConstants.EVENT_LOGOUT);
        logoutRequest.setData(new WsLogoutRequest.WsLogoutRequestData());
        requestArray.put(REQ_LOGOUT, logoutRequest);
        //发送弹幕公共消息
        WsPublicMsgRequest pubMsgRequest = new WsPublicMsgRequest();
        pubMsgRequest.setMessageType(SocketConstants.EVENT_PUB_MSG);
        pubMsgRequest.setData(new WsPublicMsgRequest.WsPublicMsgRequestData());
        requestArray.put(REQ_SEND_PUB_MSG, pubMsgRequest);

        //直播心跳
        WsPongRequest pongRequest = new WsPongRequest();
        pongRequest.setMessageType(SocketConstants.EVENT_PONG);
        pongRequest.setData(new WsPongRequest.WsPongRequestData());
        requestArray.put(REQ_HEARTBEAT, pongRequest);

        //申请连麦
        ApplyMicBean applyMicBeanRequest = new ApplyMicBean();
        applyMicBeanRequest.setMessageType(SocketConstants.APPLY_MIC_REQUEST);
        applyMicBeanRequest.setData(new ApplyMicBean.ApplyMicBeanData());
        requestArray.put(APPLY_MIC_REQUEST, applyMicBeanRequest);

        //连麦响应
        ApplyMicRsqBean applyMicBeanResponse = new ApplyMicRsqBean();
        applyMicBeanResponse.setMessageType(SocketConstants.APPLY_MIC_REQUEST_REQ);
        applyMicBeanResponse.setData(new ApplyMicRsqBean.ApplyMicRsqBeanData());
        requestArray.put(APPLY_MIC_RESPONE, applyMicBeanResponse);

        //发送礼物
        WsSendGiftRequest sendGiftRequest = new WsSendGiftRequest();
        sendGiftRequest.setMessageType(SocketConstants.EVENT_SEND_GIFT);
        sendGiftRequest.setData(new WsSendGiftRequest.WsSendGiftRequestData());
        requestArray.put(REQ_SENDGIFT, sendGiftRequest);

        //断开连麦
        DisConnectLmRequest disConnectLmRequest = new DisConnectLmRequest();
        disConnectLmRequest.setMessageType(SocketConstants.DISCONNECT_LM_REQUEST);
        disConnectLmRequest.setData(new DisConnectLmRequest.DisConnectLmRequestData());
        requestArray.put(REQ_DISLM,disConnectLmRequest);

    }

    private static void checkInitOrThrow() {
        if (!tryRestorePoolFromLocal()) {
            throw new IllegalStateException("Pool not initialized correctly and cannot be " +
                    "restored!");
        }
    }

    /**
     * 在检测到对象池未初始化时执行的最后的恢复操作，如果能从本地恢复则可以避免抛出异常。
     *
     * @return 如果成功从本地存储的登录信息中恢复则返回true，否则返回false。
     */
    private static boolean tryRestorePoolFromLocal() {
        LoginInfo loginInfo = DataManager.getInstance().getLoginInfo();
        if (loginInfo != null) {
            init(loginInfo);
        }else{
            loginInfo = new LoginInfo();
            init(loginInfo);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    //Assume type safe
    private static <T> T get(int index) {
        return (T) requestArray.get(index);
    }

    public static WsLoginRequest newLoginRequest(String roomId, String userId, String avatar, String nickname, String level, String role) {
        checkInitOrThrow();
        LoginInfo userInfo = DataManager.getInstance().getLoginInfo();
        String username = userInfo.getNickname();
        String userAvatar = userInfo.getAvatar();
        String balance = ""+userInfo.getTotalBalance();

        WsLoginRequest request = get(REQ_LOGIN);
        request.getData().setNickName(TextUtils.isEmpty(username)?"昵称":username);
        request.getData().setAvatar(userAvatar);
        request.getData().setMessage(SocketConstants.EVENT_LOGIN);
        request.getData().setLevel(Integer.parseInt(userInfo.getLevel()));
        request.getData().setRoomId(roomId);
        request.getData().setMasterUserId(Integer.parseInt(roomId));
        request.getData().setUserId(userId);
        request.getData().setRole(Integer.parseInt(role));
        request.getData().setMasterAvatar(avatar);
        request.getData().setMasterNickName(TextUtils.isEmpty(nickname)?"主播昵称":nickname);
        request.getData().setMasterLevel(TextUtils.isEmpty(level)?1: Integer.parseInt(level));
        request.getData().setBalance(balance);
        return request;
    }
    public static WsLogoutRequest newLogoutRequest(String anchorId, String id, String avatar, String nickName, String level, String websocketRoleHost, String roomId) {
        checkInitOrThrow();
        WsLogoutRequest request = get(REQ_LOGOUT);
        request.getData().setRoomId(Integer.parseInt(anchorId));
        request.getData().setUserId(Integer.parseInt(id));
        request.getData().setAvatar(avatar);
        request.getData().setNickName(TextUtils.isEmpty(nickName)?"昵称":nickName);
        request.getData().setLevel(TextUtils.isEmpty(level)? "1":level);
        request.getData().setIsMaster(Integer.parseInt(websocketRoleHost));
        request.getData().setMessage(roomId);
        return request;
    }

    public static WsPublicMsgRequest newPublicMsgRequest(String anchorId, String content, String flymsg) {
        checkInitOrThrow();

        LoginInfo userInfo = DataManager.getInstance().getLoginInfo();
        String userId = userInfo.getUserId();
        String nickName = userInfo.getNickname();
        String avatar = userInfo.getAvatar();
        Log.e("WsPublicMsgRequest",userId+"==userId");
        Log.e("WsPublicMsgRequest",nickName+"==nickName");
        Log.e("WsPublicMsgRequest",avatar+"==avatar");
        WsPublicMsgRequest request = get(REQ_SEND_PUB_MSG);
        request.getData().setRoomId(anchorId);
        request.getData().setUserId(userId);
        request.getData().setNickName(TextUtils.isEmpty(nickName)?"昵称":nickName);
        request.getData().setAvatar(TextUtils.isEmpty(avatar)?"http://3tdoc.oss-cn-beijing.aliyuncs.com/wechat/avatar/8.jpg":avatar);
        request.getData().setMessage(content);
        request.getData().setFly(flymsg);
        return request;
    }

    public static WsPublicMsgRequest newHeartRequest(String anchorId, String content) {
        checkInitOrThrow();

        LoginInfo userInfo = DataManager.getInstance().getLoginInfo();
        String userId = userInfo.getUserId();
        String nickName = userInfo.getNickname();
        String avatar = userInfo.getAvatar();;
        WsPublicMsgRequest request = get(REQ_SEND_PUB_MSG);
        request.getData().setRoomId(anchorId);
        request.getData().setUserId(userId);
        request.getData().setNickName(TextUtils.isEmpty(nickName)?"昵称":nickName);
        request.getData().setAvatar(TextUtils.isEmpty(avatar)?"http://3tdoc.oss-cn-beijing.aliyuncs.com/wechat/avatar/8.jpg":avatar);
        request.getData().setMessage(content);
        request.getData().setFly("1");
        return request;
    }

    /**
     *申请连麦
     * @param hostId 房间号,主播ID
     * @param id 申请连麦人的id
     *@param nickname  @return
     */
    public static ApplyMicBean newApplyMicRequest(String liveId, String hostId, String id, String nickname, String avatar, String introduction) {
        checkInitOrThrow();
        ApplyMicBean request = get(APPLY_MIC_REQUEST);
        request.getData().setRoomId(liveId);
        request.getData().setAdminUserId(hostId);
        request.getData().setUserId(id);
        request.getData().setNickName(nickname);
        request.getData().setAvatar(avatar);
        request.getData().setIntroduction(introduction);
        return request;
    }
    /**
     *响应连麦
     * @param anchorId
     * @param userId 对方的userid
     * @param response 是否同意连麦  1：同意  0 不同意
     * @return
     */
    public static ApplyMicRsqBean newApplyMicResposnse(String liveId, String anchorId, String userId, String response) {
        checkInitOrThrow();
        ApplyMicRsqBean request = get(APPLY_MIC_RESPONE);
        request.getData().setRoomId(liveId);
        request.getData().setUserId(userId);
        request.getData().setType(response);
        return request;
    }

    /**
     * 直播心跳
     * @param roomId
     * @param userId
     * @param isMaster
     * @return
     */
    public static WsPongRequest newPongRequest(String roomId, String userId, String isMaster) {
        checkInitOrThrow();
        WsPongRequest request = get(REQ_HEARTBEAT);
        request.getData().setRoomId(roomId);
        request.getData().setUserId(userId);
        request.getData().setIsMaster(isMaster);
        return request;
    }

    /**
     * 发送礼物
     * @param anchorId
     * @param id
     * @param price
     * @param selectedGiftId
     * @param finalCombo
     * @param nickName
     * @param avatar
     * @param level
     * @param giftName
     * @param giftSrc
     * @return
     */
    public static WsSendGiftRequest newSendGiftRequest(String roomId, String anchorId, String id, String price, String selectedGiftId, int finalCombo,
                                                       String nickName, String avatar, String level, String giftName, String giftSrc){
        checkInitOrThrow();
        WsSendGiftRequest request = get(REQ_SENDGIFT);
        request.getData().setRoomId(roomId);
        request.getData().setUserId(id);
        request.getData().setUserIdTo(anchorId);
        request.getData().setGiftId(selectedGiftId);
        request.getData().setPrice(price);
        request.getData().setNum(finalCombo);
        request.getData().setNickName(nickName);
        request.getData().setAvatar(avatar);
        request.getData().setLevel(level);
        request.getData().setMessage(SocketConstants.EVENT_SEND_GIFT);
        request.getData().setGiftName(giftName);
        request.getData().setGiftImg(giftSrc);
        return request ;
    }

    /**
     * 断开连麦
     * @param roomId 房间ID
     * @param adminUserId 主播ID
     * @param userId 用户ID
     * @return
     */
    public static DisConnectLmRequest disConnectLmRequest(String roomId, String adminUserId, String userId){
        checkInitOrThrow();
        DisConnectLmRequest request = get(REQ_DISLM);
        request.getData().setRoomId(roomId);
        request.getData().setAdminUserId(adminUserId);
        request.getData().setUserId(userId);
        return request ;
    }

    public static WsLightHeartRequest newLightHeartRequest(int colorIndex){
        //暂不缓存
        WsLightHeartRequest request = new WsLightHeartRequest();
        request.setMethod(SocketConstants.EVENT_LIGHT_HEART);
        request.setColorIndex(colorIndex);
        return request;
    }
}
