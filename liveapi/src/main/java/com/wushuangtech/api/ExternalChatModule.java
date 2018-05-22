package com.wushuangtech.api;

import android.content.Context;
import android.os.Environment;

import com.wushuangtech.library.chatlibrary.AliOss;
import com.wushuangtech.library.chatlibrary.MediaRecorderHelper;
import com.wushuangtech.library.chatlibrary.SpeechRecognition;

import java.io.File;

/**
 * Created by wangzhiguo on 18/2/7.
 */

public class ExternalChatModule {

    public static final int ACTION_ALIOSS_INIT = 0;
    public static final int ACTION_ALIOSS_DOWNLOAD = 1;

    public static final int ACTION_SPEECH_RECOGNITION = 2;

    public static final int ACTION_MEDIARECORD_INIT = 7;
    public static final int ACTION_MEDIARECORD_START_RECORD = 3;
    public static final int ACTION_MEDIARECORD_STOP_RECORD = 4;
    public static final int ACTION_MEDIARECORD_FILE_PATH = 5;
    public static final int ACTION_MEDIARECORD_CANNEL = 6;
    private static ExternalChatModule holder;
    private AliOss mAliOss;
    private MediaRecorderHelper mMediaRecorderHelper;

    private ExternalChatModule() {
    }

    public static ExternalChatModule getInstance() {
        if (holder == null) {
            synchronized (ExternalChatModule.class) {
                if (holder == null) {
                    holder = new ExternalChatModule();
                }
            }
        }
        return holder;
    }

    public Object handleActionEvent(int actionType, Object... objs) {
        switch (actionType) {
            case ACTION_ALIOSS_INIT:
                initAliOss((Context) objs[0]);
                break;
            case ACTION_MEDIARECORD_INIT:
                initMediaRecorder((Context) objs[0]);
                break;
            case ACTION_ALIOSS_DOWNLOAD:
                download((Long) objs[0], (String) objs[1], (String) objs[2]);
                break;
            case ACTION_SPEECH_RECOGNITION:
                speechRecognition((Context) objs[0], (String) objs[1]);
                break;
            case ACTION_MEDIARECORD_START_RECORD:
                startRecord();
                break;
            case ACTION_MEDIARECORD_STOP_RECORD:
                return stopAndRelease((Long) objs[0], (Long) objs[1]);
            case ACTION_MEDIARECORD_FILE_PATH:
                return getCurrentFilePath();
            case ACTION_MEDIARECORD_CANNEL:
                cancel();
                break;
        }
        return null;
    }

    private void initAliOss(Context context) {
        mAliOss = new AliOss(context);
    }

    private void initMediaRecorder(Context context) {
        String path = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = context.getExternalCacheDir().getAbsolutePath();
        } else {
            path = context.getCacheDir().getAbsolutePath();
        }
        path = path + File.separator + "Recorder";
        mMediaRecorderHelper = new MediaRecorderHelper(context, path);
    }

    private void download(long nSrcUserID, String sSeqID, String s) {
        mAliOss.download(nSrcUserID, sSeqID, s);
    }

    private void speechRecognition(Context context, String path) {
        SpeechRecognition speechRecognition = new SpeechRecognition(context);
        speechRecognition.startRecognition(path);
    }

    private void startRecord(){
        if (mMediaRecorderHelper != null)
            mMediaRecorderHelper.startRecord();
    }

    private int stopAndRelease(long mConfId, long nDstUserID){
        if (mMediaRecorderHelper != null)
            return mMediaRecorderHelper.stopAndRelease(mConfId, nDstUserID);
        return -1;
    }

    private String getCurrentFilePath(){
        if (mMediaRecorderHelper != null)
            return mMediaRecorderHelper.getCurrentFilePath();
        return null;
    }

    private void cancel(){
        if (mMediaRecorderHelper != null)
            mMediaRecorderHelper.cancel();
    }
}
