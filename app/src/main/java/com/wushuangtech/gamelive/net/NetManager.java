package com.wushuangtech.gamelive.net;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by 刘景 on 2017/05/11.
 */

public class NetManager {
    private static final int CONNECT_TIME_OUT = 60;
    private static final int WRITE_TIME_OUT = 60;
    private static final int READ_TIME_OUT = 60;

    public static NetManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private static class SingletonHolder {
        private static final NetManager INSTANCE = new NetManager();
    }
    private NetManager() { }

    public <S> S create(Class<S> service){
        return create(service, Constants.MAIN_HOST_URL);
    }

    public <S> S create(Class<S> service,String url){
        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient.Builder clientBuilder = okHttpClient.newBuilder()
                //添加网络通用请求信息, see http://stackoverflow.com/a/33667739
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder().build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT,TimeUnit.SECONDS)
                .readTimeout(READ_TIME_OUT,TimeUnit.SECONDS);

        okHttpClient = clientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(service);
    }


    public static Uri wrapPathToUri(@NonNull String path) {
        return Uri.parse(wrapPath(path));
    }
    public static String wrapPath(String path) {
        if (!path.startsWith("http")) {
            path = Constants.MAIN_HOST_URL + path;
        }
        return path;
    }
    public static Uri wrapPathWx(@NonNull String path){
        return Uri.parse(path);
    }
}
