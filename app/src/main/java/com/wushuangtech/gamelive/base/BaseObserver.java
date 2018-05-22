package com.wushuangtech.gamelive.base;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.wushuangtech.gamelive.util.LogUtil;


import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observer;

/**
 * Created by 刘景 on 2017/05/11.
 */

public abstract class BaseObserver <E extends BaseResponse>implements Observer<E> {
    protected final String LOG_TAG = getClass().getSimpleName();
    private final BaseUiInterface mUiInterface;

    public BaseObserver(BaseUiInterface baseUiInterface) {
        mUiInterface = baseUiInterface;
    }

    @Override
    public void onCompleted() {
        mUiInterface.showLoadingComplete();
    }

    @Override
    public void onError(Throwable e) {
        Log.e("BaseObserver", "Request Error!", e);
        handleError(e, mUiInterface, LOG_TAG);
    }

    public static void handleError(Throwable throwable, BaseUiInterface mUiInterface, String LOG_TAG) {
        mUiInterface.showLoadingComplete();
        if (throwable == null) {
            mUiInterface.showUnknownException();
            return;
        }
        //分为以下几类问题：网络连接，数据解析，客户端出错【空指针等】，服务器内部错误
        if (throwable instanceof SocketTimeoutException || throwable
                instanceof ConnectException || throwable instanceof UnknownHostException) {
            mUiInterface.showNetworkException();

        }else if ((throwable instanceof JsonSyntaxException) || (throwable instanceof
                NumberFormatException) || (throwable instanceof MalformedJsonException)) {
            mUiInterface.showDataException("数据解析出错");
        } else if ((throwable instanceof HttpException)) {
//            mUiInterface.showDataException("服务器错误(" + ((HttpException) throwable).code()+")");
            //自动上报这个异常
        }else if (throwable instanceof NullPointerException) {
            mUiInterface.showDataException("客户端开小差了，攻城狮正在修复中...");
            //自动上报这个异常
        }else {

        }
    }

    @Override
    public void onNext(E response) {
        Log.i("mrl",response.toString());
        if("0".equals(response.getCode())){
            onSuccess(response);
        }else {
            if (mUiInterface instanceof BaseActivity || mUiInterface instanceof BaseFragment){
                final BaseActivity activity;
                if (mUiInterface instanceof BaseFragment){
                    activity = (BaseActivity) ((BaseFragment)mUiInterface).getActivity();
                }else{
                    activity = (BaseActivity)mUiInterface;
                }

                if((response.getMsg().equals("missing token")||response.getMsg().equals("token错误")) ){
                    final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                            .setMessage("您的账号在其他地方已登录！请重新登录")
                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    activity.sendFinishBroadcast(Log.class.getSimpleName());
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                    return;
                }else{
                    onDataFailure(response);
                }
            }
        }
    }

    protected void onDataFailure(E response) {
        String msg = response.getMsg();
        Log.w(LOG_TAG, "request data but get failure:" + msg);
        if (!TextUtils.isEmpty(msg)) {
            mUiInterface.showDataException(response.getMsg());
        }
        else{
            mUiInterface.showUnknownException();
        }
    }
    /**
     * Create a new silence, non-leak observer.
     */
    public static <T> Observer<T> silenceObserver(){
        return new Observer<T>() {
            @Override
            public void onCompleted() {
                //Empty
            }

            @Override
            public void onError(Throwable e) {
                //Empty
            }

            @Override
            public void onNext(T t) {
                //Empty
            }
        };
    }
    public abstract void onSuccess(E response);
}
