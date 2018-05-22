package com.wushuangtech.room.core;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.wushuangtech.bean.VideoCompositingLayout;
import com.wushuangtech.jni.ChatJni;
import com.wushuangtech.jni.RoomJni;
import com.wushuangtech.library.Constants;
import com.wushuangtech.videocore.MyVideoApi;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.internal.TTTRtcEngineImpl;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_BAD_VERSION;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_FAILED;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_TIMEOUT;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_UNKNOW;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_VERIFY_FAILED;
import static com.wushuangtech.library.Constants.ERROR_KICK_BY_HOST;
import static com.wushuangtech.library.Constants.ERROR_KICK_BY_MASTER_EXIT;
import static com.wushuangtech.library.Constants.ERROR_KICK_BY_NEWCHAIRENTER;
import static com.wushuangtech.library.Constants.ERROR_KICK_BY_NOAUDIODATA;
import static com.wushuangtech.library.Constants.ERROR_KICK_BY_NOVIDEODATA;
import static com.wushuangtech.library.Constants.ERROR_KICK_BY_PUSHRTMPFAILED;
import static com.wushuangtech.library.Constants.ERROR_KICK_BY_RELOGIN;
import static com.wushuangtech.library.Constants.ERROR_KICK_BY_SERVEROVERLOAD;
import static com.wushuangtech.room.core.LocalConstans.CALL_BACK_ON_ENTER_ROOM;
import static com.wushuangtech.room.core.LocalConstans.CALL_BACK_ON_ERROR;
import static com.wushuangtech.room.core.LocalConstans.CALL_BACK_ON_USER_JOIN;
import static com.wushuangtech.room.core.LocalConstans.CALL_BACK_ON_USER_OFFLINE;


/**
 * Created by Iverson on 2017/11/15 下午2:54
 * 此类用于：用于直播的辅助类
 */

public class RoomLiveHelp {

    private RoomLiveInterface mLiveInstance;
    private Activity mContext;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private TTTRtcEngine mTTTEngine;
    private boolean mIsExitRoom;
    private int DISCONNECT = 100;
    private EnterUserInfo mRemoteUserInfo;
    public boolean isKickout = false;
    public Map<String,String> messageData = new HashMap<String , String>();

    public RoomLiveHelp(RoomLiveInterface liveInterface, Activity activity){
        this.mLiveInstance = liveInterface;
        mContext = activity;
        initBroadcast();
    }


    //注册广播信息
    private void initBroadcast() {
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        mContext.registerReceiver(mLocalBroadcast, filter);
        TTTSDK.mMyTTTRtcEngineEventHandler.setIsSaveCallBack(false);
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MyTTTRtcEngineEventHandler.TAG)) {
                JniObjs mJniObjs = intent.getParcelableExtra(
                        MyTTTRtcEngineEventHandler.MSG_TAG);

                switch (mJniObjs.mJniType) {
                    case CALL_BACK_ON_ENTER_ROOM:
                        mLiveInstance.enterRoomSuccess();
                        break;
                    case CALL_BACK_ON_ERROR:
                        final int errorType = mJniObjs.mErrorType;
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (errorType == ERROR_ENTER_ROOM_TIMEOUT || errorType == ERROR_ENTER_ROOM_FAILED ||
                                        errorType == ERROR_ENTER_ROOM_VERIFY_FAILED || errorType == ERROR_ENTER_ROOM_BAD_VERSION ||
                                        errorType == ERROR_ENTER_ROOM_UNKNOW) {
                                    mLiveInstance.enterRoomFailue(errorType);
                                } else if (errorType == ERROR_KICK_BY_HOST || errorType == ERROR_KICK_BY_PUSHRTMPFAILED ||
                                        errorType == ERROR_KICK_BY_SERVEROVERLOAD || errorType == ERROR_KICK_BY_MASTER_EXIT ||
                                        errorType == ERROR_KICK_BY_RELOGIN || errorType == ERROR_KICK_BY_NEWCHAIRENTER ||
                                        errorType == ERROR_KICK_BY_NOAUDIODATA || errorType == ERROR_KICK_BY_NOVIDEODATA) {
                                    mLiveInstance.onDisconnected(errorType);
                                } else if (errorType == DISCONNECT) {
                                    mLiveInstance.onDisconnected(errorType);
                                }
                            }
                        });
                        break;
                    case CALL_BACK_ON_USER_JOIN:
                        long uid = mJniObjs.mUid;
                        int identity = mJniObjs.mIdentity;
                        mRemoteUserInfo = new EnterUserInfo(uid, identity);
                        if(identity == Constants.CLIENT_ROLE_BROADCASTER){
                            mLiveInstance.onHostEnter(uid, mRemoteUserInfo);
                        }else {
                            mLiveInstance.onMemberEnter(uid, mRemoteUserInfo);
                        }
                        break;
                    case CALL_BACK_ON_USER_OFFLINE:
                        mLiveInstance.onMemberExit(mJniObjs.mUid);
                        break;
                    case LocalConstans.CALL_BACK_ON_SEI:
                        String sei = mJniObjs.mSEI;
                        List<EnterUserInfo> mInfos = new ArrayList<>();
                        try {
                            JSONObject jsonObject = new JSONObject(sei);
                            String mid = (String) jsonObject.get("mid");
                            LocalConfig.mBroadcasterID = Integer.valueOf(mid);
                            JSONArray jsonArray = jsonObject.getJSONArray("pos");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonobject2 = (JSONObject) jsonArray.get(i);
                                String devid = jsonobject2.getString("id");
                                float x = Float.valueOf(jsonobject2.getString("x"));
                                float y = Float.valueOf(jsonobject2.getString("y"));
                                long userId;
                                int index = devid.indexOf(":");
                                if (index > 0) {
                                    userId = Long.parseLong(devid.substring(0, index));
                                } else {
                                    userId = Long.parseLong(devid);
                                }
                                LocalConfig.mBroadcasterID = Integer.valueOf(mid);
                                int role = 2;
                                if(LocalConfig.mBroadcasterID == userId){
                                    role = 1;
                                }
                                EnterUserInfo temp = new EnterUserInfo(userId,role,x,y);
                                mInfos.add(temp);
                            }
                            mLiveInstance.onUpdateLiveView(mInfos);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case LocalConstans.CALL_BACK_ON_CHAT_MESSAGE_RECIVED:
//                        mLiveInstance.dispatchMessage(mJniObjs.nSrcUserID,mJniObjs.type,mJniObjs.msSeqID,mJniObjs.strData);
                        break;

                    case LocalConstans.CALL_BACK_ON_CHAT_MESSAGE_SENT:
                        if(!TextUtils.isEmpty(mJniObjs.msSeqID)){
                            String mDatas = messageData.get(mJniObjs.msSeqID);

                            String ss = mJniObjs.strData;
                            for (Map.Entry<String , String> entry : messageData.entrySet()){
                                mapKey = entry.getKey();
                                mapValue = entry.getValue();
                            }
                            if(mJniObjs.error == 0){
//                                mLiveInstance.sendMessageResult(mJniObjs.error,mapValue);
                            }else {
//                                mLiveInstance.sendMessageResult(mJniObjs.error,"");
                            }
//                            messageData.remove(mJniObjs.msSeqID);
//                            messageData.clear();
                        }
                        break;

                }
            }
        }
    }

    private String mapValue;
    private String mapKey;


    /***************************************直播模块开始***************************************/

    //初始化engine并进入直播 liveType:直播类型 role：进入的角色 roomId:直播的房间id, userId:进入房间的userid
    public void enterRoom(int liveType,int role,final int roomId,final long userId) {
        mTTTEngine = TTTRtcEngine.getInstance();
//        RoomJni.getInstance().setServerAddress("39.107.64.215",5000);
//        ChatJni.getInstance().enableChat();
        // 设置频道属性
        mTTTEngine.setChannelProfile(liveType);
        // 启用视频模式
        mTTTEngine.enableVideo();
        // 重置直播房间内的参数
        mTTTEngine.setClientRole(role, null);
        TTTRtcEngineImpl.getInstance().setVideoProfile(Constants.VIDEO_PROFILE_720P,false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //设置推流地址
//                GlobalConfig.mPushUrl = "";
                int result = mTTTEngine.joinChannel("", String.valueOf(roomId), userId);
                LogUtil.e("result",""+result);
            }
        }).start();

    }

    //主播预览视频
    public void startPreview(ViewGroup rootView){
        mTTTEngine = TTTRtcEngine.getInstance();
        SurfaceView localSurfaceView = mTTTEngine.CreateRendererView(mContext);
        MyVideoApi.VideoConfig videoConfig = MyVideoApi.getInstance().getVideoConfig();
        videoConfig.videoWidth = 640;
        videoConfig.videoHeight = 360;

        MyVideoApi.getInstance().setVideoConfig(videoConfig);
        localSurfaceView.getHolder().addCallback(new RoomLiveHelp.RemoteSurfaceViewCb());
        mTTTEngine.setupLocalVideo(new VideoCanvas(0, Constants.RENDER_MODE_HIDDEN,
                localSurfaceView), mContext.getRequestedOrientation());
        rootView.addView(localSurfaceView, 0);
        mTTTEngine.startPreview();
    }

    //打开本地视频 rootView：父控件 zorderMediaOverlay：显示层级
    public void openLocalVideo(ViewGroup rootView, boolean zorderMediaOverlay){
        SurfaceView localSurfaceView;
        MyVideoApi.VideoConfig videoConfig = MyVideoApi.getInstance().getVideoConfig();
        videoConfig.videoWidth = 1280;
        videoConfig.videoHeight = 720;
        videoConfig.enabeleFrontCam = true;
        MyVideoApi.getInstance().setVideoConfig(videoConfig);
        localSurfaceView = mTTTEngine.CreateRendererView(mContext);
        localSurfaceView.setZOrderMediaOverlay(zorderMediaOverlay);
        localSurfaceView.getHolder().addCallback(new RoomLiveHelp.RemoteSurfaceViewCb());
        mTTTEngine.setupLocalVideo(new VideoCanvas(0, Constants.RENDER_MODE_HIDDEN,
                localSurfaceView), mContext.getRequestedOrientation());
        rootView.addView(localSurfaceView, 0);
    }

    // 打开远端视频 rootView:傅控件 zorderMediaOverlay：显示层级 info：副播的详细信息
    public void  openRemoteVideo(ViewGroup rootView, EnterUserInfo info, boolean zorderMediaOverlay){
        SurfaceView mSurfaceView;
        long id = info.getId();
        mSurfaceView = mTTTEngine.CreateRendererView(mContext);
        mSurfaceView.getHolder().addCallback(new RemoteSurfaceViewCb());
        mTTTEngine.setupRemoteVideo(new VideoCanvas(info.getId(), Constants.
                RENDER_MODE_HIDDEN, mSurfaceView));
        rootView.setVisibility(View.VISIBLE);
        if(zorderMediaOverlay){
            mSurfaceView.bringToFront();
        }
        mSurfaceView.setZOrderMediaOverlay(zorderMediaOverlay);
        rootView.addView(mSurfaceView, 0);
    }

    // 打开远端视频 ,直接传入userID rootView:傅控件 zorderMediaOverlay：显示层级 info：副播的详细信息
    public void  openIdRemoteVideo(ViewGroup rootView, Long userId, boolean zorderMediaOverlay){
        SurfaceView mSurfaceView;
//        long id = info.getId();
        mSurfaceView = mTTTEngine.CreateRendererView(mContext);
        mSurfaceView.getHolder().addCallback(new RemoteSurfaceViewCb());
        mTTTEngine.setupRemoteVideo(new VideoCanvas(userId, Constants.
                RENDER_MODE_HIDDEN, mSurfaceView));
        rootView.setVisibility(View.VISIBLE);
        if(zorderMediaOverlay){
            mSurfaceView.bringToFront();
        }
        mSurfaceView.setZOrderMediaOverlay(zorderMediaOverlay);
        rootView.addView(mSurfaceView, 0);
    }

    public void setClientRole(int type){
            mTTTEngine.setClientRole(type,null);
    }

    //主播设置副播的小窗口位置 userId：副播的id
    public void resetRemoteLayout(VideoCompositingLayout.Region[] regions){
        VideoCompositingLayout layout = new VideoCompositingLayout();
        layout.regions = regions;
        mTTTEngine.setVideoCompositingLayout(layout);
    }

    //踢出房间成员 userId被T的userid
    public boolean kickRoomMember(long userId){
        if(null != mTTTEngine ){
            boolean status = mTTTEngine.kickChannelUser(userId);
            return status;
        }
        return false;
    }

    //切换摄像头成功与否
    public boolean switchCamera() {
        if (mTTTEngine != null) {
            int status = mTTTEngine.switchCamera();
            return status == 0 ? true : false;
        }
        return false;
    }

    /**
     * 发送聊天信息
     * @param nDstUserID 目标ID 0:所有人 其他代表某个人的id
     * @param type 消息类型 1：文字 2：图片  3：音频
     * @param sSeqID 消息唯一标识 当前的消息tag
     * @param sData 消息内容
     */
    public void sendMessage(long nDstUserID, int type, String sSeqID, String sData){
        messageData.put(sSeqID,sData);
        mTTTEngine = TTTRtcEngine.getInstance();
        mTTTEngine.sendChatMessage(nDstUserID,type,sSeqID,sData);
    }

    //控制本地视频
    public void controlLocalVideo(boolean flag){
        mTTTEngine.muteLocalVideoStream(flag);
    }
    //控制本地声音
    public void controlLocalAudio(boolean flag){
        mTTTEngine.muteLocalAudioStream(flag);
    }
    //离开房间
    public void exitRoom(){
        if(null != mTTTEngine){
            mTTTEngine.stopAudioMixing();
            mTTTEngine.leaveChannel();
        }
    }

    public class RemoteSurfaceViewCb implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (LocalConfig.mRole == CLIENT_ROLE_BROADCASTER) {
                VideoCompositingLayout layout = new VideoCompositingLayout();
                List<VideoCompositingLayout.Region> tempList = new ArrayList<>();
                VideoCompositingLayout.Region[] mRegions = new VideoCompositingLayout.Region[tempList.size()];
                layout.regions = mRegions;
                mTTTEngine.setVideoCompositingLayout(layout);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (LocalConfig.mRole == CLIENT_ROLE_BROADCASTER) {
                VideoCompositingLayout layout = new VideoCompositingLayout();
                List<VideoCompositingLayout.Region> tempList = new ArrayList<>();
                VideoCompositingLayout.Region hostRegion = new VideoCompositingLayout.Region();
                hostRegion.zOrder = 1;
                hostRegion.width = 0.2;
                hostRegion.height = 0.2;
                hostRegion.x = 0.4;
                hostRegion.y = 0.8;
                tempList.add(hostRegion);

                VideoCompositingLayout.Region[] mRegions = new VideoCompositingLayout.Region[tempList.size()];
                layout.regions = mRegions;
                mTTTEngine.setVideoCompositingLayout(layout);
            }
        }
    }

    //退出帮助
    public void exitHelp(){
        if(mContext != null){
            if(null != mTTTEngine){
                mTTTEngine.stopAudioMixing();
                mTTTEngine.leaveChannel();
            }
            mContext.unregisterReceiver(mLocalBroadcast);
        }
    }
}
