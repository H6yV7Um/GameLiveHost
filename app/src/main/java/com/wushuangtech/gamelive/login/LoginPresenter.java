package com.wushuangtech.gamelive.login;


import com.wushuangtech.gamelive.base.BaseObserver;
import com.wushuangtech.gamelive.base.BasePresenter;
import com.wushuangtech.gamelive.base.BaseResponse;
import com.wushuangtech.gamelive.data.LoginInfo;
import com.wushuangtech.gamelive.domin.DataManager;
import com.wushuangtech.gamelive.net.NetManager;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Iverson on 2016/12/24 上午11:37
 * 此类用于：
 */

public class LoginPresenter extends BasePresenter<LoginUiinterface> {

    public LoginPresenter(LoginUiinterface uiInterface) {
        super(uiInterface);
    }

    /**
     * 手机号方式登录
     */
    public void loginNow(final String phone, String password) {
        Subscription subscription = NetManager.getInstance().create(LoginApi.class).Login(phone, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<BaseResponse<LoginInfo>>(getUiInterface()) {
                        @Override
                        public void onSuccess(BaseResponse<LoginInfo> response) {
                            LoginInfo loginInfo = response.getData();
                            loginInfo.setRoomId(loginInfo.getUserId());
                            DataManager.getInstance().saveLoginInfo(loginInfo);
                            getUiInterface().onLoginSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                            getUiInterface().onLoginFailuer();
                        }
                    });

        addSubscription(subscription);
    }
}
