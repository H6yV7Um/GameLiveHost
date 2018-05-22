package com.wushuangtech.gamelive.util;


import com.wushuangtech.gamelive.domin.DataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Iverson on 2017/1/13 下午2:47
 * 此类用于：
 */

public class CreateReuqestBodyUtil {

    public static Map<String, RequestBody> createRequestBody(List<String> list){
        File file = null;
        Map<String, RequestBody> bodyMap = new HashMap<>();
        String userId = DataManager.getInstance().getLoginInfo().getUserId();
        for (int i = 0; i < list.size(); i++){
                    file = new File(list.get(i));
                     int j = i + 1;
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    switch (i){
                        case 0:
                            bodyMap.put("file1\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+j+"_small.png",requestFile);
                            break;
                        case 1:
                            bodyMap.put("file2\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+j+"_small.png",requestFile);
                            break;
                        case 2:
                            bodyMap.put("file3\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+j+"_small.png",requestFile);
                            break;
                        case 3:
                            bodyMap.put("file4\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+j+"_small.png",requestFile);
                            break;
                        case 4:
                            bodyMap.put("file5\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+j+"_small.png",requestFile);
                            break;
                        case 5:
                            bodyMap.put("file6\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+j+"_small.png",requestFile);
                            break;
                        case 6:
                            bodyMap.put("file7\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+j+"_small.png",requestFile);
                            break;
                        case 7:
                            bodyMap.put("file8\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+j+"_small.png",requestFile);
                            break;
                    }
                }
        return bodyMap;
    }
    //更改背景
    public static Map<String, RequestBody> uploadBackgroudImage(File bgFile){
        Map<String, RequestBody> bodyMap = new HashMap<>();
        String userId = DataManager.getInstance().getLoginInfo().getUserId();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), bgFile);
        bodyMap.put("bgImage\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_big.png",requestFile);
        return bodyMap;
    }
    //更改照片墙
    public static Map<String, RequestBody> upPhotoWall(File photo, int num){
        Map<String, RequestBody> bodyMap = new HashMap<>();
        String userId = DataManager.getInstance().getLoginInfo().getUserId();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), photo);
        bodyMap.put("file1\"; filename=\"" +DateUtilsl.getCurrentDate()+"_"+userId+"_"+num+"_small.png",requestFile);
        return bodyMap ;
    }
    //上传工厂
    public static Map<String,RequestBody> editFactoryDetail(ArrayList<String> semblanceImagList,
                                                            File sem5, File faceFile, File backFile, ArrayList<String> otherImageList){
        File file = null;
        Map<String, RequestBody> bodyMap = new HashMap<>();
        String userId = DataManager.getInstance().getLoginInfo().getUserId();
        for (int i = 0 ;i <semblanceImagList.size();i++){
            file = new File(semblanceImagList.get(i));
            int j = i + 1;
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            switch (i) {
                case 0:
                    bodyMap.put("semblanceImage1\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_" + j + "_big.png", requestFile);
                    break;
                case 1:
                    bodyMap.put("semblanceImage2\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_" + j + "_big.png", requestFile);
                    break;
                case 2:
                    bodyMap.put("semblanceImage3\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_" + j + "_big.png", requestFile);
                    break;
                case 3:
                    bodyMap.put("semblanceImage4\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_" + j + "_big.png", requestFile);
                    break;
            }
        }
        bodyMap.put("blicence5\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId  + "_blicence.png", RequestBody.create(MediaType.parse("multipart/form-data"), sem5));
        bodyMap.put("lpersonfaceImg\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_"+1+"_lperson.png", RequestBody.create(MediaType.parse("multipart/form-data"), faceFile));
        bodyMap.put("lpersonbackImg\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_"+2+"_lperson.png", RequestBody.create(MediaType.parse("multipart/form-data"), backFile));
        for (int i = 0 ;i <otherImageList.size();i++){
            file = new File(otherImageList.get(i));
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            switch (i) {
                case 0:
                    bodyMap.put("otherImage1\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_" + 8 + "_small.png", requestFile);
                    break;
                case 1:
                    bodyMap.put("otherImage2\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_" + 9 + "_small.png", requestFile);
                    break;
                case 2:
                    bodyMap.put("otherImage3\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_" + 10 + "_small.png", requestFile);
                    break;
                case 3:
                    bodyMap.put("otherImage4\"; filename=\"" + DateUtilsl.getCurrentDate() + "_" + userId + "_" + 11 + "_small.png", requestFile);
                    break;
            }
        }

        return bodyMap;
    }

}
