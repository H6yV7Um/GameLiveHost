package com.wushuangtech.gamelive.room;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.magiepooh.recycleritemdecoration.ItemDecorations;
import com.wushuangtech.gamelive.Config;
import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.base.BaseFragment;
import com.wushuangtech.gamelive.data.LoginInfo;
import com.wushuangtech.gamelive.data.UserInfo;
import com.wushuangtech.gamelive.data.pubmsg.PublicChatAdapter;
import com.wushuangtech.gamelive.data.pubmsg.SimpleRecyclerAdapter;
import com.wushuangtech.gamelive.data.pubmsg.SimpleRecyclerHolder;
import com.wushuangtech.gamelive.domin.DataManager;
import com.wushuangtech.gamelive.domin.SPUtils;
import com.wushuangtech.gamelive.net.Constants;
import com.wushuangtech.gamelive.net.NetManager;
import com.wushuangtech.gamelive.websocket.SocketConstants;
import com.wushuangtech.gamelive.websocket.WebSocketService;
import com.wushuangtech.gamelive.websocket.WsListener;
import com.wushuangtech.gamelive.websocket.WsObjectPool;
import com.wushuangtech.gamelive.websocket.data.DisConnectLmMsg;
import com.wushuangtech.gamelive.websocket.data.LightHeartMsg;
import com.wushuangtech.gamelive.websocket.data.RoomPublicMsg;
import com.wushuangtech.gamelive.websocket.data.SendGiftMsg;
import com.wushuangtech.gamelive.websocket.data.SystemWelcome;
import com.wushuangtech.gamelive.websocket.data.UserPublicMsg;
import com.wushuangtech.gamelive.websocket.data.WsGiftMsg;
import com.wushuangtech.gamelive.websocket.data.WsLoginMsg;
import com.wushuangtech.gamelive.websocket.data.WsLoginOutMsg;
import com.wushuangtech.gamelive.websocket.data.WsRequest;
import com.wushuangtech.gamelive.widget.gift.IAnimController;
import com.wushuangtech.gamelive.widget.gift.IGiftAnimPlayer;
import com.wushuangtech.gamelive.widget.gift.LocalAnimQueue;
import com.wushuangtech.gamelive.widget.gift.SendGiftAction;
import com.wushuangtech.gamelive.widget.heart.HeartAnim;
import com.wushuangtech.room.core.RoomLiveHelp;
import com.wushuangtech.room.core.RoomLiveInterface;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Iverson on 2018/1/3 下午5:19
 * 此类用于：
 */

public abstract class RoomFragment extends BaseFragment implements RoomLiveInterface {

    private final String LOG_TAG = RoomFragment.class.getSimpleName();

    protected RelativeLayout mRlLocalRootView;//本地视频容器
    protected RoomLiveHelp mRoomLiveHelp;
    protected String mAnchorId;
    protected String mRoomId;
    protected boolean isHost;
    protected WebSocketService wsService;

    //    创建一个布尔值判断只对userPublicMsg进行一次设置
    private boolean initUserPublicMsg = false;


    protected String mHostAvatar;//主播头像
    protected String mPublishNickName;//主播昵称
    protected String mHostLevel;//主播等级
    private PublicChatAdapter publicChatAdapter;
    private RecyclerView recyclerPublicChat;
    private int[] heartColorArray;
    private HeartAnim mHeartAnim;
    private int defaultColorIndex;

    private IAnimController localGiftController;
    private LinearLayout llChatBar;
    private LinearLayout giftLayout;
    protected EditText edtChatContent;
    public LinearLayout llOperationBar;
    private Button mBtSendMessage;
    protected ImageView mIvBack;
    private RecyclerView recyclerAudienceList;
    private AudienceAdapter audienceAdapter;
    private SimpleDraweeView mSdHostPic;
    protected TextView mTvOnlineCount;

    private boolean isSend =false ;
    private boolean isConnectSocket  = false;
    private String mOnLineCount = "";
    private String mPubdurtion = "0";//默认值为0
    private String mGetGold = "0";//默认值为0

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnchorId = Config.ROOM_HOST_ID;
        mHostAvatar = Config.ROOM_HOST_AVATAR;
        mPublishNickName = Config.ROOM_HOST_NICKNAME;
        mHostLevel = "1";
    }



    protected void initView(View view) {
        mRoomLiveHelp = new RoomLiveHelp(this, (RoomLiveActivity) getActivity());
        heartColorArray = getResources().getIntArray(R.array.room_heart_colors);
        giftLayout = view.findViewById(R.id.room_ll_gift_bar);
        edtChatContent = view.findViewById(R.id.room_edt_chat);
        edtChatContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(TextUtils.isEmpty(edtChatContent.getText().toString())){
                    mBtSendMessage.setEnabled(false);
                }else {
                    mBtSendMessage.setEnabled(true);
                }
            }
        });
        llOperationBar = view.findViewById(R.id.room_ll_operation_bar);

        llChatBar = view.findViewById(R.id.room_ll_chat_bar);
        if (giftLayout != null) {
            List<IGiftAnimPlayer> playerViews = new ArrayList<>();
            int childCount = giftLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                IGiftAnimPlayer player = (IGiftAnimPlayer) giftLayout.getChildAt(i);
                playerViews.add(player);
            }
            localGiftController = new LocalAnimQueue(playerViews);
        }

        recyclerPublicChat = view.findViewById(R.id.room_recycler_chat);
        if (recyclerPublicChat != null) {
            recyclerPublicChat.setLayoutManager(new LinearLayoutManager(getActivity()));
            publicChatAdapter = new PublicChatAdapter(new ArrayList<RoomPublicMsg>());
            recyclerPublicChat.setAdapter(publicChatAdapter);
        }

        heartColorArray = getResources().getIntArray(R.array.room_heart_colors);
        mSdHostPic = view.findViewById(R.id.img_user_avatar);
        mSdHostPic.setImageURI(Uri.parse(Config.LIVE_SRC));
        mHeartAnim = view.findViewById(R.id.room_heart_view);

        view.findViewById(R.id.live_heart_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultColorIndex = (int) (Math.random() * heartColorArray.length);
//                mHeartAnim.addLove(heartColorArray[defaultColorIndex]);
                wsService.sendRequest(WsObjectPool.newPublicMsgRequest(mRoomId,""+defaultColorIndex,"1"));
            }
        });

        view.findViewById(R.id.tv_say_something).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputLayout(true);
            }
        });

        mTvOnlineCount = view.findViewById(R.id.online_audience);
        recyclerAudienceList = view.findViewById(R.id.room_recycler_audience);
        recyclerAudienceList.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        recyclerAudienceList.addItemDecoration(ItemDecorations.horizontal(getActivity())
                .type(0, R.drawable.divider_decoration_transparent_w5)
                .create());

        audienceAdapter = new AudienceAdapter(new ArrayList<SystemWelcome.SystemWelcomeData.SystemWelcomeDataList>());
        recyclerAudienceList.setAdapter(audienceAdapter);
        mTvOnlineCount.setText(getResources().getString(R.string.on_line_audience,"0"));

        mIvBack = view.findViewById(R.id.live_back_img);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showExitDialog();
            }
        });
        mBtSendMessage = view.findViewById(R.id.room_btn_send);
        mBtSendMessage.setEnabled(false);
        mBtSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( edtChatContent.getText().toString().length() > 20){
                    toastShort("发送的内容不能超过20个字！");
                    return;
                }
                if (TextUtils.isEmpty(edtChatContent.getText())) {
                    Toast.makeText(getActivity(), "发送内容不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    LoginInfo loginInfo = DataManager.getInstance().getLoginInfo();
                    UserPublicMsg userPublicMsg = new UserPublicMsg();
                    UserPublicMsg.UserPublicMsgData msgData = new UserPublicMsg.UserPublicMsgData();
                    //userPublicMsg.setLevel(publicMsgLevel);
                    msgData.setUserId(loginInfo.getUserId());
                    msgData.setNickName(loginInfo.getNickname());
                    String msg = edtChatContent.getText().toString();
                    userPublicMsg.setMessage(msg);
                    userPublicMsg.setData(msgData);
                    wsService.sendRequest(WsObjectPool.newPublicMsgRequest(mRoomId, msg, "0"));
                    publicChatAdapter.appendData(userPublicMsg);
                    //Auto scroll to last
                    recyclerPublicChat.scrollToPosition(publicChatAdapter.getItemCount() - 1);
                    edtChatContent.getText().clear();
                    showInputLayout(false);
                }
            }
        });

        view.findViewById(R.id.rl_all_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRootClickAction();

            }
        });

        initWebSocket();
    }


    //显示退出框
    protected  void showExitDialog(){
        MessageDialog dialog = new MessageDialog(mContext);
        dialog.setContent("确认关闭此窗口","关闭","取消");
        dialog.setMessageDialogListener(new MessageDialog.MessageDialogListener() {
            @Override
            public void onCancelClick(MessageDialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void onCommitClick(MessageDialog dialog) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                //1容错处理,为了防止断网或者websocket链接不上的情况,强制退出,2也可以不在这处理,先弹出直播结束信息的框,之后在获取到websocket之后赋值
                if(isConnectSocket){
                    requesetRoomLoginOut(mAnchorId,mHostAvatar,mPublishNickName,mHostLevel,Config.WEBSOCKET_ROLE_HOST, SocketConstants.EVENT_LOGOUT);
                }else {
                    onHostExit(mOnLineCount,mPubdurtion,mGetGold);
                }
            }
        });
        dialog.show();
    }


    protected void onRootClickAction() {
        if(llChatBar.getVisibility() == View.VISIBLE){
            showInputLayout(false);
            llOperationBar.setVisibility(View.VISIBLE);
        }
    }

    //    初始化webSocket
    private void initWebSocket() {


        wsService = ((RoomLiveActivity)mContext).getWsService();

        isConnectSocket = true ;

        LoginInfo loginInfo = DataManager.getInstance().getLoginInfo();
        if(wsService!=null){
            if (loginInfo != null) {
                if (mAnchorId.equals(loginInfo.getUserId())) {
                    isHost = true;
                    wsService.sendRequest(WsObjectPool.newLoginRequest(Config.ROOM_ID, mAnchorId, mHostAvatar, mPublishNickName, mHostLevel, Config.WEBSOCKET_ROLE_HOST));
                } else {
                    wsService.sendRequest(WsObjectPool.newLoginRequest(Config.ROOM_ID, loginInfo.getUserId(), mHostAvatar, mPublishNickName, mHostLevel, Config.WEBSOCKET_ROLE_AUDIENCE));

                }
            }
            initWsListeners();
        }

    }

    /**
     * 因为WebSocket能连接的时间具有不确定性，所以必须在ServiceConnection里初始化。
     */
    protected void initWsListeners() {

        //进入房间给本人发的消息
        WsListener<WsLoginMsg> loginListener = new WsListener<WsLoginMsg>() {

            @Override
            public void handleData(WsLoginMsg wsLoginMsg) {
                Log.e(LOG_TAG, wsLoginMsg.getData().getNickName() + "mrliu");
            }
        };
        wsService.registerListener(SocketConstants.EVENT_LOGIN_RSP, loginListener);

        //  进入房间群发消息
        if (recyclerAudienceList != null) {
            WsListener<SystemWelcome> welcomeListenet = new WsListener<SystemWelcome>() {
                @Override
                public void handleData(SystemWelcome systemMsg) {
                    publicChatAdapter.appendData(systemMsg);
                    //Auto scroll to last
                    recyclerPublicChat.scrollToPosition(publicChatAdapter.getItemCount() - 1);
                    mOnLineCount = String.valueOf(systemMsg.getData().getCount());
                    mTvOnlineCount.setText(getResources().getString(R.string.on_line_audience,systemMsg.getData().getCount()+""));
                }
            };
            wsService.registerListener(SocketConstants.EVENT_SYSWElCOME, welcomeListenet);
        }

        WsListener<WsLoginOutMsg> loginOutMsgWsListener = new WsListener<WsLoginOutMsg>() {
            @Override
            public void handleData(WsLoginOutMsg wsLoginOutMsg) {
                mTvOnlineCount.setText(getResources().getString(R.string.on_line_audience,wsLoginOutMsg.getData().getCount()+""));
                if (Integer.parseInt(mAnchorId) == wsLoginOutMsg.getData().getUserId()) {
                    onHostExit(wsLoginOutMsg.getData().getCount()+"",wsLoginOutMsg.getData().getDurtion(),wsLoginOutMsg.getData().getTotalCoin());
                }
                mOnLineCount = String.valueOf(wsLoginOutMsg.getData().getCount());
            }
        };
        wsService.registerListener(SocketConstants.EVENT_LOGOUT_RSP, loginOutMsgWsListener);

//        接受用户发出的消息
        WsListener<UserPublicMsg> chatListener = new WsListener<UserPublicMsg>() {
            @Override
            public void handleData(UserPublicMsg msg) {
                if (msg.getData().getUserId().equals(DataManager.getInstance().getLoginInfo().getUserId())) {
                    showInputLayout(false);
                }
                if (DataManager.getInstance().getLoginInfo() != null) {
                    if("1".equals(""+msg.getData().getFly())){
                        int colorIndex = Integer.parseInt(msg.getMessage());
                        //设置安全的默认值
                        if (colorIndex < 0 || colorIndex >= heartColorArray.length) {
                            colorIndex = 0;
                        }
                        mHeartAnim.addLove(heartColorArray[colorIndex]);
                        return;
                    }
                    //如果不是用户本人的信息则添加到Recycleview
                    if (!msg.getData().getUserId().equals(DataManager.getInstance().getLoginInfo().getUserId())) {
                        publicChatAdapter.appendData(msg);
                        //Auto scroll to last
                        recyclerPublicChat.scrollToPosition(publicChatAdapter.getItemCount() - 1);
                    }
                }
            }
        };
        wsService.registerListener(SocketConstants.EVENT_PUB_MSG_RSP, chatListener);


        WsListener<LightHeartMsg> heartListener = new WsListener<LightHeartMsg>() {
            @Override
            public void handleData(LightHeartMsg lightHeartMsg) {
                //要插入一条聊天数据
                publicChatAdapter.appendData(lightHeartMsg);
                //Auto scroll to last
                recyclerPublicChat.scrollToPosition(publicChatAdapter.getItemCount() - 1);
                //画心，如果不是来自自己的点亮
                String myUserId = DataManager.getInstance().getLoginInfo().getUserId();
                if ((!TextUtils.isEmpty(myUserId)) && (!myUserId.equalsIgnoreCase(lightHeartMsg
                        .getFromUserId()))) {
                    int colorIndex = lightHeartMsg.getColor();
                    //设置安全的默认值
                    if (colorIndex < 0 || colorIndex >= heartColorArray.length) {
                        colorIndex = 0;
                    }
                    mHeartAnim.addLove(heartColorArray[colorIndex]);
                }
            }
        };
        wsService.registerListener(SocketConstants.EVENT_LIGHT_HEART, heartListener);

        //礼物群发通知
        if (localGiftController != null) {
            WsListener<SendGiftMsg> giftAnimListener = new WsListener<SendGiftMsg>() {
                @Override
                public void handleData(SendGiftMsg sendGiftMsg) {
                        //publicChatAdapter.appendData(sendGiftMsg);
                    //if (!sendGiftMsg.getIsred().equals("1")) {
                    //tvGold.setText(String.valueOf(sendGiftMsg.getAnchorBalance()));
                    // }
                   // recyclerPublicChat.scrollToPosition(publicChatAdapter.getItemCount() - 1);
                    localGiftController.enqueue(adapter(sendGiftMsg));
                }

                private SendGiftAction adapter(SendGiftMsg msg) {
                    SendGiftAction action = new SendGiftAction();
                    action.setAvatar(msg.getData().getAvatar());
                    action.setFromUid(msg.getData().getUserId());
                    action.setCombo(msg.getData().getNum());
                    action.setNickname(msg.getData().getNickName());
                    action.setGiftIcon(msg.getData().getGiftImg());
                    action.setGiftName(msg.getData().getGiftName());
                    return action;
                }
            };
            wsService.registerListener(SocketConstants.EVENT_NOTIFY_GIFT_RSP, giftAnimListener);
        }
        //发送礼物回调给自己的
        WsListener<WsGiftMsg> wsGiftMsgWsListener = new WsListener<WsGiftMsg>() {
            @Override
            public void handleData(WsGiftMsg wsGiftMsg) {
                UserInfo userInfo = DataManager.getInstance().getmUserInfo();
                Log.e(LOG_TAG, userInfo.getId());
                if (userInfo != null && wsGiftMsg.getData().getUserId().equals(userInfo.getId())) {
                    int balance = wsGiftMsg.getData().getBalance();//用户余额
                    userInfo.setBalance(String.valueOf(balance));
                    DataManager.getInstance().saveUserinfo(userInfo);
                }

            }
        };

        wsService.registerListener(SocketConstants.EVENT_SEND_GIFT_RSP, wsGiftMsgWsListener);
    }


    public void showInputLayout(boolean show) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (show) {
            llChatBar.setVisibility(View.VISIBLE);
            edtChatContent.requestFocus();
            imm.showSoftInput(edtChatContent, InputMethodManager.SHOW_IMPLICIT);
            llOperationBar.setVisibility(View.INVISIBLE);
        } else {
            llOperationBar.setVisibility(View.VISIBLE);
            llChatBar.setVisibility(View.INVISIBLE);
            imm.hideSoftInputFromWindow(edtChatContent.getWindowToken(), InputMethodManager
                    .HIDE_NOT_ALWAYS);
        }
    }

    protected abstract void onHostExit(String audienceNum,String durtion,String gold);



    private class AudienceAdapter extends SimpleRecyclerAdapter<SystemWelcome.SystemWelcomeData.SystemWelcomeDataList, AudienceHolder> {

        public AudienceAdapter(List<SystemWelcome.SystemWelcomeData.SystemWelcomeDataList> audienceInfoList) {
            super(audienceInfoList);
        }

        @Override
        protected int getItemLayoutId(int viewType) {
            return R.layout.item_online_audience;
        }

        @NonNull
        @Override
        protected AudienceHolder createHolder(View view) {
            return new AudienceHolder(view);
        }
    }

    private class AudienceHolder extends SimpleRecyclerHolder<SystemWelcome.SystemWelcomeData.SystemWelcomeDataList> {

        private SimpleDraweeView draweeAvatar;
        private ImageView icon;
        private UserInfo mInfo;

        public AudienceHolder(View itemView) {
            super(itemView);
            draweeAvatar = (SimpleDraweeView) itemView.findViewById(R.id.img_user_avatar);
        }

        @Override
        public void displayData(final SystemWelcome.SystemWelcomeData.SystemWelcomeDataList data) {
//            判断当前用户是哪个观众，然后设置一下userPublicMsg的信息
            LoginInfo loginInfo = DataManager.getInstance().getLoginInfo();
            if (loginInfo != null && loginInfo.getUserId().equals(data.getUserId()) && !initUserPublicMsg) {
                initUserPublicMsg = true;
            }
            String avatar = data.getAvatar();
            mInfo = new UserInfo();
            mInfo.setId(data.getUserId());
            mInfo.setAvatar(data.getAvatar());
            mInfo.setNickName(data.getNickName());
            mInfo.setLevel(data.getLevel());
            if (!TextUtils.isEmpty(avatar)) {
                draweeAvatar.setImageURI(NetManager.wrapPathToUri(avatar));
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    /**
     * 退出房间wedsocket
     * @param id
     * @param Avatar
     * @param NickName
     * @param Level
     * @param websocketRoleHost
     * @param eventLogout
     */
    public void requesetRoomLoginOut(String id, String Avatar, String NickName, String Level, String websocketRoleHost, String eventLogout) {
        wsService.sendRequest(WsObjectPool.newLogoutRequest(mRoomId,id,Avatar,NickName,Level,websocketRoleHost,eventLogout));
        isSend = true ;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();


        if(!isSend){
            wsService.sendRequest(WsObjectPool.newLogoutRequest(mRoomId, mAnchorId, mHostAvatar, mPublishNickName, mHostLevel, Config.WEBSOCKET_ROLE_HOST, SocketConstants.EVENT_LOGOUT));
            isSend = false ;
        }
        if (wsService != null) {
            wsService.removeAllListeners();
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //退出直播
        if (mRoomLiveHelp != null) {
            mRoomLiveHelp.exitHelp();
            mRoomLiveHelp = null;
        }

    }
}
