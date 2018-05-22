package com.wushuangtech.gamelive.login;

import android.support.annotation.NonNull;


import com.wushuangtech.gamelive.base.BaseObserver;
import com.wushuangtech.gamelive.base.BasePresenter;
import com.wushuangtech.gamelive.base.BaseResponse;
import com.wushuangtech.gamelive.data.CodeBean;
import com.wushuangtech.gamelive.net.NetManager;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Iverson on 2016/12/27 下午3:06
 * 此类用于：
 */

public class ForgetPasswordPresenter extends BasePresenter<ForgetPasswordUiInterfece> {

    protected ForgetPasswordPresenter(ForgetPasswordUiInterfece uiInterface) {
        super(uiInterface);
    }
    /**
     * 获取验证码
     * @param phone
     */
    public void getCheckCode(@NonNull String phone) {
        Subscription subscribe = NetManager.getInstance().create(RegitestApi.class)
                .getCheckCode(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<CodeBean>>(getUiInterface()) {
                    @Override
                    public void onSuccess(BaseResponse<CodeBean> response) {
                        if(response.getData()!=null){
                            getUiInterface().getSendCodeSuccess(response.getData());
                        }
                    }
                });
        addSubscription(subscribe);
    }
    /**
     * 判断该手机号是否已经注册
     */

    public void isRegiest(@NonNull final String mobile){
        Subscription subscription = NetManager.getInstance().create(RegitestApi.class)
                .isRegiest(mobile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<Object>>(getUiInterface()) {
                    @Override
                    public void onSuccess(BaseResponse<Object> response) {
                        getUiInterface().isSucess(mobile);
                    }

                    @Override
                    protected void onDataFailure(BaseResponse<Object> response) {
                        super.onDataFailure(response);
                    }
                });
        addSubscription(subscription);
    }

}
