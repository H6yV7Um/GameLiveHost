package com.wushuangtech.gamelive.login;

import com.wushuangtech.gamelive.base.BaseResponse;
import com.wushuangtech.gamelive.data.LoginInfo;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Iverson on 2018/4/3 上午9:30
 * 此类用于：
 */

public interface LoginApi {

    //手机号登陆
    @FormUrlEncoded
    @POST("/user/login")
    Observable<BaseResponse<LoginInfo>> Login(@Field("mobile") String phone, @Field("password") String password);
}
