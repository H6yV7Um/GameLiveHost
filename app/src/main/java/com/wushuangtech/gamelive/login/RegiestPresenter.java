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
 * Created by Iverson on 2016/12/27 下午1:58
 * 此类用于：
 */

public class RegiestPresenter extends BasePresenter<RegiestUiInterface> {

    protected RegiestPresenter(RegiestUiInterface uiInterface) {
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
                        getUiInterface().getSendCodeSuccess(response.getData());
                    }
                });
        addSubscription(subscribe);
    }
    /**
     * 注册
     * @param username
     * @param password
     */
    public void regiestUserNow(@NonNull String username, @NonNull String password){
        //String password_md5 = MD5Util.string2MD5(password);
        Subscription subscription = NetManager.getInstance().create(RegitestApi.class).register(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<BaseResponse<Object>>(getUiInterface()) {
                    @Override
                    public void onSuccess(BaseResponse<Object> response) {
                        if(response.getData() == null) return;
                        //保存登录信息和跳转界面
                        getUiInterface().regiestSuccess();
                    }
                });
        addSubscription(subscription);
    }
}
