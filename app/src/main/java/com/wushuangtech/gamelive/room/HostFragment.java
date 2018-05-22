package com.wushuangtech.gamelive.room;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.wushuangtech.gamelive.R;
import com.wushuangtech.library.Constants;
import com.wushuangtech.room.core.EnterUserInfo;

import java.util.List;

/**
 * Created by Iverson on 2018/1/3 下午5:19
 * 此类用于：
 */

public class HostFragment extends RoomFragment implements View.OnClickListener{

    private FinishRoomDialog dialog ;
    public static HostFragment newInstance(@NonNull Bundle bundle, String hostId, String roomId) {
        HostFragment fragment = new HostFragment();
        bundle.putString(RoomLiveActivity.LIVE_HOST_ID, hostId);
        bundle.putString(RoomLiveActivity.LIVE_ROOM_ID, roomId);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live_host;
    }

    @Override
    protected void initViews(View view) {
        mAnchorId = getArguments().getString(RoomLiveActivity.LIVE_HOST_ID);
        mRoomId = getArguments().getString(RoomLiveActivity.LIVE_ROOM_ID);
        super.initView(view);
        mRlLocalRootView = view.findViewById(R.id.rl_publish_root);
        view.findViewById(R.id.live_gift_img).setVisibility(View.GONE);
        mRoomLiveHelp.enterRoom(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING, Constants.CLIENT_ROLE_BROADCASTER,Integer.parseInt(mRoomId),Long.parseLong(mAnchorId));
    }

    @Override
    public void enterRoomSuccess() {
        mRlLocalRootView.removeAllViews();
        mRoomLiveHelp.openLocalVideo(mRlLocalRootView,false);
    }

    @Override
    public void enterRoomFailue(int errorCode) {

    }

    @Override
    public void onDisconnected(int errorCode) {

    }

    @Override
    public void onMemberExit(long userId) {

    }

    @Override
    public void onMemberEnter(long userId, EnterUserInfo userInfo) {

    }

    @Override
    public void onHostEnter(long userId, EnterUserInfo userInfo) {

    }

    @Override
    public void onUpdateLiveView(List<EnterUserInfo> userInfos) {

    }
    //显示主播收入信息
    @Override
    protected void onHostExit(String audienceNum,String durtion,String gold) {
        if(dialog==null){
            dialog = new FinishRoomDialog(mContext,audienceNum,durtion,gold);
            dialog.setFinishRoomDialogLisnter(new FinishRoomDialog.FinishRoomDialogLisnter() {
                @Override
                public void onCloseClick(FinishRoomDialog dialog) {
                    if(dialog.isShowing())dialog.dismiss();
                    getActivity().finish();
                }
            });
            dialog.show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {

    }
}
