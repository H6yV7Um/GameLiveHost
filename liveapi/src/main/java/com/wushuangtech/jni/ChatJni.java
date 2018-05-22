package com.wushuangtech.jni;

import android.content.Context;
import android.util.Log;

import com.wushuangtech.api.ExternalChatModule;
import com.wushuangtech.library.PviewConferenceRequest;
import com.wushuangtech.utils.PviewLog;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-12-11.
 */

public class ChatJni {

    public static final int CHAT_CHATDATATYPE_TEXT = 1;      //文字聊天
    public static final int CHAT_CHATDATATYPE_PICTURE = 2;   //图片
    public static final int CHAT_CHATDATATYPE_AUDIO = 3;     //音频文件

    private static ChatJni mChatJni;
    private List<WeakReference<PviewConferenceRequest>> mCallBacks;

    private ChatJni() {
        mCallBacks = new ArrayList<>();
    }

    public static synchronized ChatJni getInstance() {
        if (mChatJni == null) {
            synchronized (ChatJni.class) {
                if (mChatJni == null) {
                    mChatJni = new ChatJni();
                    if (!mChatJni.initialize(mChatJni)) {
                        throw new RuntimeException("can't initilaize ChatJni");
                    }
                }
            }
        }
        return mChatJni;
    }

    /**
     * 添加自定义的回调，监听接收到的服务信令
     *
     * @param callback 回调对象
     */
    public void addCallback(PviewConferenceRequest callback) {
        this.mCallBacks.add(new WeakReference<PviewConferenceRequest>(callback));
    }

    /**
     * 移除自定义添加的回调
     *
     * @param callback 回调对象
     */
    public void removeCallback(PviewConferenceRequest callback) {
        for (int i = 0; i < mCallBacks.size(); i++) {
            WeakReference<PviewConferenceRequest> wf = mCallBacks.get(i);
            if (wf != null && wf.get() != null) {
                if (wf.get() == callback) {
                    mCallBacks.remove(wf);
                    return;
                }
            }
        }
    }

    public native boolean initialize(ChatJni request);

    public native void unInitialize();

    public native void enableChat();

    public native void SendChat(long nGroupID, long nDstUserID, int type, String sSeqID, String sData, int nLen);

    public native void enableSignal();

    public native void SendSignal(long nGroupID, long nDstUserID, int type, String sSeqID, String sData, int nLen);

    private void OnChatSend(int type, String sSeqID, int error) {
        PviewLog.jniCall("ChatModule OnSendResult", "Begin");
        Log.d("ChatModule", "OnChatSend: type:" + type + " sSeqID:" + sSeqID + "   error:" + error);
        for (int i = 0; i < mCallBacks.size(); i++) {
            WeakReference<PviewConferenceRequest> wf = mCallBacks.get(i);
            if (wf != null && wf.get() != null) {
                wf.get().OnChatSend(sSeqID, error);
            }
        }
        PviewLog.jniCall("ChatModule OnSendResult", "End");
    }

    private void OnChatRecv(long nSrcUserID, int type, String sSeqID, String strData, int length) {
        PviewLog.jniCall("ChatModule OnRecvResult", "Begin");
        Log.d("ChatModule", "OnChatSend: nSrcUserID:" + nSrcUserID + " type:" + type + "   sSeqID:" + sSeqID + "   strData:" + strData + "  length:" + length);
        if (type == CHAT_CHATDATATYPE_TEXT) {
            for (int i = 0; i < mCallBacks.size(); i++) {
                WeakReference<PviewConferenceRequest> wf = mCallBacks.get(i);
                if (wf != null && wf.get() != null) {
                    try {
                        String message = URLDecoder.decode(strData, "UTF-8");
                        wf.get().OnChatRecv(nSrcUserID, type, sSeqID, message);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
            PviewLog.jniCall("ChatModule OnRecvResult", "End");
        } else {
            ExternalChatModule.getInstance().handleActionEvent(ExternalChatModule.ACTION_ALIOSS_DOWNLOAD , nSrcUserID, sSeqID, String.valueOf(strData));
        }
    }

    public void OnAudioDonwload(long nSrcUserID, int type, String sSeqID, String strData) {
        for (int i = 0; i < mCallBacks.size(); i++) {
            WeakReference<PviewConferenceRequest> wf = mCallBacks.get(i);
            if (wf != null && wf.get() != null) {
                wf.get().OnChatRecv(nSrcUserID, type, sSeqID, strData);
            }
        }
    }

    public void OnPlayCompletion() {
        for (int i = 0; i < mCallBacks.size(); i++) {
            WeakReference<PviewConferenceRequest> wf = mCallBacks.get(i);
            if (wf != null && wf.get() != null) {
                wf.get().onPlayChatAudioCompletion();
            }
        }
    }

    public void OnSpeechRecognized(String str) {
        for (int i = 0; i < mCallBacks.size(); i++) {
            WeakReference<PviewConferenceRequest> wf = mCallBacks.get(i);
            if (wf != null && wf.get() != null) {
                wf.get().onSpeechRecognized(str);
            }
        }
    }

}
