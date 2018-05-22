package com.wushuangtech.gamelive.room;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wushuangtech.gamelive.R;


/**
 * Created by Iverson on 2016/12/30 下午2:16
 * 此类用于：一般适用的对话框
 */

public class MessageDialog extends Dialog implements View.OnClickListener {
    private TextView mContent;
    private Button mCancel, mCommit;
    private String mContentStr;
    private String mLeftText;
    private String mRightText;
    private MessageDialogListener listener;

    public MessageDialog(Context context) {
        super(context, R.style.DialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_message);
        findView();
        init();
    }

    public void setContent(int strId) {
        mContentStr = this.getContext().getResources().getString(strId);
    }

    public void setContent(String str,String leftS,String rightS) {
        mContentStr = str;
        mLeftText = leftS;
        mRightText = rightS;
    }

    public MessageDialog hideCancelOption() {
        mCancel.setVisibility(View.GONE);
        return this;
    }

    public void setMessageDialogListener(MessageDialogListener listener) {
        this.listener = listener;
    }

    private void init() {
        mContent.setText(mContentStr + "");
        mCancel.setText(mRightText + "");
        mCommit.setText(mLeftText + "");
        mCancel.setOnClickListener(this);
        mCommit.setOnClickListener(this);
    }

    private void findView() {
        mContent = (TextView) findViewById(R.id.dialog_message_content);
        mCancel = (Button) findViewById(R.id.dialog_message_cancel);
        mCommit = (Button) findViewById(R.id.dialog_message_commit);
    }

    @Override
    public void onClick(View v) {
        if (v == mCancel) {
            if (listener != null) {
                listener.onCancelClick(this);
            }
        } else {
            if (listener != null) {
                listener.onCommitClick(this);
            }
        }
    }

    public interface MessageDialogListener {
        void onCancelClick(MessageDialog dialog);

        void onCommitClick(MessageDialog dialog);
    }
}
