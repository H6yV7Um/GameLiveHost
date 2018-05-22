package com.wushuangtech.gamelive.login;

import android.support.annotation.NonNull;


import com.wushuangtech.gamelive.base.BaseObserver;
import com.wushuangtech.gamelive.base.BasePresenter;
import com.wushuangtech.gamelive.base.BaseResponse;
import com.wushuangtech.gamelive.net.NetManager;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Iverson on 2016/12/27 下午3:25
 * 此类用于：
 */

public class ResetPwdPresenter extends BasePresenter<ResetPwdUiInterface> {
    protected ResetPwdPresenter(ResetPwdUiInterface uiInterface) {
        super(uiInterface);
    }

    public void ForgetNextStep(@NonNull String phone, @NonNull String password){
        //String md5 = MD5Util.string2MD5(password);
        Subscription subscribe = NetManager.getInstance().create(RegitestApi.class).resetPassword(phone,password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<Object>>(getUiInterface()) {
                    @Override
                    public void onSuccess(BaseResponse<Object> response) {
                        getUiInterface().resetPwdSuccess();
                    }
                });
        addSubscription(subscribe);
    }
}
