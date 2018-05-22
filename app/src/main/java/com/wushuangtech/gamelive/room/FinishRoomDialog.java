package com.wushuangtech.gamelive.room;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wushuangtech.gamelive.R;

/**
 * Created by mrliu on 2018/4/19.
 * 此类用于:显示房间直播结束信息
 */

public class FinishRoomDialog extends Dialog implements View.OnClickListener{

    private TextView mDurtionTv,mGoldTv,mNumTv;
    private ImageView mCloseIv ;
    private FinishRoomDialogLisnter listener ;
    private String mAudienceNum,mDurtion,mGold ;
    private Context mContext ;

    public FinishRoomDialog(@NonNull Context context, String audienceNum, String durtion, String gold) {
        super(context, R.style.DialogStyle);
        this.mContext = context;
        this.mAudienceNum = audienceNum;
        this.mDurtion = durtion;
        this.mGold = gold;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_room_finish);
        initLayout();
        initView();
    }

    private void initLayout() {
        Window win = getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);
        win.setGravity(Gravity.CENTER);
    }


    private void initView() {
        setCancelable(false);
        mCloseIv = findViewById(R.id.img_clos);
        mNumTv = findViewById(R.id.tv_likenum);
        mDurtionTv = findViewById(R.id.duration_tv);
        mGoldTv = findViewById(R.id.bx_gold);
        showData();
    }

    //展示数据
    private void showData() {
        mNumTv.setText(mAudienceNum);
        mDurtionTv.setText(mDurtion);
        mGoldTv.setText(mGold);
        mCloseIv.setOnClickListener(this);
    }
    // 1 写一个接口,接口里面写调用的方法
    public interface FinishRoomDialogLisnter{
        void onCloseClick(FinishRoomDialog dialog);
    }
    //2 实例化接口对象
    public void setFinishRoomDialogLisnter(FinishRoomDialogLisnter listener){
        this.listener = listener ;
    }
    //3 调用
    @Override
    public void onClick(View v) {
       if(v==mCloseIv){
           if(listener!=null){
               listener.onCloseClick(this);
           }
       }
    }
}
