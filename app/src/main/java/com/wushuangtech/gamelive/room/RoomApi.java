package com.wushuangtech.gamelive.room;

import com.wushuangtech.gamelive.base.BaseResponse;
import com.wushuangtech.gamelive.data.PublishRoomIdBean;


import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import rx.Observable;

/**
 * Created by Iverson on 2018/3/27 下午2:39
 * 此类用于：
 */

public interface RoomApi {

    //主播开始直播
    @Multipart
    @POST("/live/start-live")
    Observable<BaseResponse<PublishRoomIdBean>> setLiveStatus(@Part("userId") String userId,
                                                              @Part("remark") String remark,
                                                              @PartMap Map<String, RequestBody> params,
                                                              @Part("title") String title);


    //主播开始直播
//    @FormUrlEncoded
//    @POST("/live/start-live")
//    Observable<BaseResponse<PublishRoomIdBean>> setLiveStatus(@Field("userId") String userId,
//                                                              @Field("remark") String remark,
//                                                              @PartMap Map<String, RequestBody> imgSrc,
//                                                              @Field("title") String title);

}
