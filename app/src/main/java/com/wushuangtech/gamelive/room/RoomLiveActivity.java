package com.wushuangtech.gamelive.room;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.wushuangtech.gamelive.Config;
import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.RoomInterface;
import com.wushuangtech.gamelive.RoomManager;
import com.wushuangtech.gamelive.data.LoginInfo;
import com.wushuangtech.gamelive.domin.DataManager;
import com.wushuangtech.gamelive.websocket.WebSocketService;

/**
 * Created by Iverson on 2018/1/3 下午2:47
 * 此类用于：
 */

public class RoomLiveActivity extends AppCompatActivity {

    public static String LIVE_HOST_ID = "live_host_id";
    public static String LIVE_ROOM_ID = "live_room_id";

    private RoomFragment fragment;
    private static final int FRAG_CONTAINER = R.id.room_container;
    public static final int TYPE_VIEW_LIVE = 0;//观看直播
    public static final int TYPE_PUBLISH_LIVE = 1;//直播
    private RoomInterface mRoomInstance;
    private WebSocketService wsService;

    private ServiceConnection wsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            wsService = ((WebSocketService.ServiceBinder) service).getService();
            showVideoFragment();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };




    public static Intent createIntent(Context context, Bundle args){
        Intent intent = new Intent(context,RoomLiveActivity.class);
        intent.putExtras(args);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_room);
        bindService(WebSocketService.createIntent(this), wsConnection, BIND_AUTO_CREATE);

        mRoomInstance = RoomManager.getInstance().getRoomInstance();
        findView();

    }

    //初始化控件
    private void findView() {

        LoginInfo loginInfo = DataManager.getInstance().getLoginInfo();
        Config.ROOM_HOST_AVATAR = loginInfo.getAvatar();
        Config.ROOM_HOST_ID = loginInfo.getUserId();
        Config.ROOM_HOST_NICKNAME = loginInfo.getNickname();
    }


    private void showVideoFragment() {
        Intent intent = getIntent();
        getSupportFragmentManager().beginTransaction()
                .add(FRAG_CONTAINER, createFragmentByType(intent.getExtras(),TYPE_PUBLISH_LIVE, Config.ROOM_HOST_ID, Config.ROOM_ID))
                .commit();
    }




    private RoomFragment createFragmentByType(Bundle mBundleArgs,int roomType,String hostId,String roomId){
        switch (roomType){
//            直播间
            case TYPE_PUBLISH_LIVE:
                fragment = HostFragment.newInstance(mBundleArgs,hostId,roomId);
                break;

            default:
                throw new IllegalArgumentException("Wrong room type: " + roomType);
        }
        return fragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wsService != null) {
            wsService.prepareShutdown();
            unbindService(wsConnection);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fragment.showExitDialog();
    }

    public WebSocketService getWsService(){
        return wsService ;
    }
}
