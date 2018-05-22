package com.wushuangtech.gamelive.room;


import com.wushuangtech.gamelive.Config;
import com.wushuangtech.gamelive.base.BaseObserver;
import com.wushuangtech.gamelive.base.BasePresenter;
import com.wushuangtech.gamelive.base.BaseResponse;
import com.wushuangtech.gamelive.data.PublishRoomIdBean;
import com.wushuangtech.gamelive.domin.SPUtils;
import com.wushuangtech.gamelive.net.Constants;
import com.wushuangtech.gamelive.net.NetManager;
import com.wushuangtech.library.GlobalConfig;

import java.util.Map;

import okhttp3.RequestBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Iverson on 2016/12/30 下午1:44
 * 此类用于：
 */

public class PublishFragmentPresenter extends BasePresenter<PublishUiInterface> {

    public PublishFragmentPresenter(PublishUiInterface uiInterface) {
        super(uiInterface);
    }

    /**
     * 主播是否开关直播
     */
    public void isOpenLive(final String uid, final String remark,Map<String, RequestBody> params, String title){
        NetManager.getInstance().create(RoomApi.class).setLiveStatus(uid,remark,params,title)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<PublishRoomIdBean>>(getUiInterface()) {
                    @Override
                    public void onSuccess(BaseResponse<PublishRoomIdBean> response) {
                        if("0".equals(response.getCode())){
                            //DataManager.getInstance().savePublishId(response.getData());
                            GlobalConfig.mPushUrl = response.getData().getPushRtmp();
                            Config.LIVE_SRC = response.getData().getImgSrc();
                            Config.LIVE_WS_URL = "ws://"+response.getData().getWsServer().getData().getRoomServer().getHost()+":"+response.getData().getWsServer().getData().getRoomServer().getPort();
                            Config.ROOM_ID = ""+response.getData().getLiveId();
                            getUiInterface().upImageSuccess();
                        }
                    }
                });
    }

}
