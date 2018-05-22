package com.wushuangtech.library.chatlibrary;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.google.gson.Gson;
import com.wushuangtech.bean.AudioChatInfo;
import com.wushuangtech.jni.ChatJni;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2017-12-15.
 */

public class AliOss {

    private Context mContext = null;

    private String mAccessKey;
    private String mAccessKeySecret;
    private String mBucket;
    private String mEndPoint;
    private OSS oss = null;
    private OnSendChatListener mOnSendChatListener = null;

    public interface OnSendChatListener {
        void onSendChatAudioStart(String audioPath, long audioTime);
        void onSendChatAudioEnd(int errorcode);
    }

    public AliOss(Context context) {
        mContext = context;

        mAccessKey          = "LTAIIYGRmx5qmxkk";
        mAccessKeySecret   = "AfV0Hy6uII76bc0lEiMCJYgTKDHbN1";
        mBucket             = "videocenter";
        mEndPoint           = "http://oss-cn-beijing.aliyuncs.com";

        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(mAccessKey, mAccessKeySecret);
        oss = new OSSClient(mContext, mEndPoint, credentialProvider);
    }

    public void upload(final long nGroupID, final long nDstUserID, final String key, String path, final int audioTime) {

        if (mOnSendChatListener != null)
            mOnSendChatListener.onSendChatAudioStart(path, audioTime);

        PutObjectRequest put = new PutObjectRequest(mBucket, key, path);
        oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("zhx", "onSuccess: ");
                AudioChatInfo audioChatInfo = new AudioChatInfo();
                audioChatInfo.audioName = key;
                audioChatInfo.audioTime = audioTime;

                Gson gson = new Gson();
                String sData = gson.toJson(audioChatInfo);

                // 上传完成发送消息通知接收者
                ChatJni.getInstance().SendChat(nGroupID, nDstUserID, ChatJni.CHAT_CHATDATATYPE_AUDIO, "0", sData, sData.length());

                if (mOnSendChatListener != null)
                    mOnSendChatListener.onSendChatAudioEnd(0);
            }

            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                Log.d("zhx", "onFailure: ");
                if (mOnSendChatListener != null)
                    mOnSendChatListener.onSendChatAudioEnd(1);
            }
        });
    }

    public void download(final long nSrcUserID, final String sSeqID, String key) {

        final Gson gson = new Gson();
        final AudioChatInfo audioChatInfo = gson.fromJson(new String(key), AudioChatInfo.class);
        GetObjectRequest get = new GetObjectRequest(mBucket, audioChatInfo.audioName);
        oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest getObjectRequest, GetObjectResult getObjectResult) {
                Log.d("zhx", "onSuccess: ");
                File file = new File(getRecorderFilePath() + audioChatInfo.audioName + ".wav");
//                if (!file.exists()) file.mkdirs();
                inputstreamtofile(getObjectResult.getObjectContent(), file);

                audioChatInfo.audioName = file.getAbsolutePath();
                ChatJni.getInstance().OnAudioDonwload(nSrcUserID, ChatJni.CHAT_CHATDATATYPE_AUDIO, sSeqID, gson.toJson(audioChatInfo));
            }

            @Override
            public void onFailure(GetObjectRequest getObjectRequest, ClientException e, ServiceException e1) {
                Log.d("zhx", "onFailure: ");
            }
        });
    }

    public String getRecorderFilePath() {
        String path = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = mContext.getExternalCacheDir().getAbsolutePath();
        } else {
            path = mContext.getCacheDir().getAbsolutePath();
        }
        return path + File.separator + "Recorder" + File.separator;
    }

    public void inputstreamtofile(InputStream ins, File file){
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead = ins.read(buffer, 0, 1024)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
