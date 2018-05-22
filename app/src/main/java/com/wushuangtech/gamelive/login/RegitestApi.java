package com.wushuangtech.gamelive.login;



import com.wushuangtech.gamelive.base.BaseResponse;
import com.wushuangtech.gamelive.data.CodeBean;
import com.wushuangtech.gamelive.data.LoginInfo;
import com.wushuangtech.gamelive.data.VerifyCodeBean;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Iverson on 2016/12/27 下午2:00
 * 此类用于：
 */

public interface RegitestApi {

    @FormUrlEncoded
    @POST("/user/verification-code")
    Observable<BaseResponse<CodeBean>> getCheckCode(@Field("mobile") String phone);

    @FormUrlEncoded
    @POST("/user/phone-register")
    Observable<BaseResponse<Object>> register(@Field("mobile") String username,
                                              @Field("password") String password);

    @FormUrlEncoded
    @POST("/user/check-phone")
    Observable<BaseResponse<Object>> isRegiest(@Field("mobile")String mobile);

    @FormUrlEncoded
    @POST("/user/set-password")
    Observable<BaseResponse<Object>> resetPassword(@Field("mobile") String phone,@Field("password") String password);


}
