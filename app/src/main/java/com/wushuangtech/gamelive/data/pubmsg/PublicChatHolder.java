package com.wushuangtech.gamelive.data.pubmsg;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.websocket.data.LightHeartMsg;
import com.wushuangtech.gamelive.websocket.data.RoomPublicMsg;
import com.wushuangtech.gamelive.websocket.data.SendGiftMsg;
import com.wushuangtech.gamelive.websocket.data.SystemMsg;
import com.wushuangtech.gamelive.websocket.data.SystemWelcome;
import com.wushuangtech.gamelive.websocket.data.UserPublicMsg;


public class PublicChatHolder extends SimpleRecyclerHolder<RoomPublicMsg> {

    private TextView tvContent;

    public PublicChatHolder(View itemView) {
        super(itemView);
        tvContent = (TextView) itemView.findViewById(R.id.item_public_chat_tv);
    }

    @Override
    public void displayData(RoomPublicMsg data) {
        CharSequence sequence = null;
//        L.i("Sequence", "data class=" + data.getClass().getSimpleName());
        if (data instanceof UserPublicMsg) {
            sequence = buildUserChatSequence((UserPublicMsg)data);
        }
        else if (data instanceof LightHeartMsg){
            sequence = buildLightHeartSequence((LightHeartMsg)data);
        }
//        else if (data instanceof SendGiftMsg){
//            sequence = buildGiftSequence((SendGiftMsg)data);
//        }
        else if (data instanceof SystemMsg){
            sequence = buildSysChatSequence((SystemMsg)data);
        }else if (data instanceof SystemWelcome){
            sequence = buildWelcomeChatSequence((SystemWelcome)data);
        }
        else {
//            L.e("Sequence", "Unsupported type!");
        }
        if (!TextUtils.isEmpty(sequence)) {
            tvContent.setText(sequence);
        }
    }


    private CharSequence buildUserChatSequence(UserPublicMsg msg){

        MsgUtils utils = MsgUtils.getInstance();
        CharSequence level = utils.buildLevel(1);
        //if level is not legal
        if (level == null) {
            level = "";
        }
        return TextUtils.concat(
                level,
                utils.buildimUserName(msg.getData().getNickName()),
                utils.buildPublicMsgContent(msg.getMessage()));
    }

    //    系统警告信息显示
    private CharSequence buildSysChatSequence(SystemMsg msg){
        MsgUtils utils = MsgUtils.getInstance();
//        CharSequence level = utils.buildLevel(msg.getLevel());
//        //if level is not legal
//        if (level == null) {
//            level = "";
//        }
//      这里可能会用到
        return TextUtils.concat(
                utils.buildPublicSysMsgContent(msg.getContent()));
    }

    //    系统欢迎信息显示
    private CharSequence buildWelcomeChatSequence(SystemWelcome msg){
        MsgUtils utils = MsgUtils.getInstance();
        CharSequence level = utils.buildLevel(msg.getData().getLevel());
        //if level is not legal
        if (level == null) {
            level = "";
        }
        return TextUtils.concat(
                utils.buildPublicSysMsgWelcome(""),
                level,
                utils.buildPublicSysMsgName(msg.getData().getNickName()),
                utils.buildPublicSysMsgWelcome("  来了"));
    }


    private CharSequence buildLightHeartSequence(LightHeartMsg msg){
        MsgUtils utils = MsgUtils.getInstance();
        CharSequence level = utils.buildLevel(msg.getLevel());
        //if level is not legal
        if (level == null) {
            level = "";
        }
        return TextUtils.concat(
                level,
                utils.buildUserName(msg.getFromClientName()),
                utils.buildPublicMsgContent("我点亮了"),
                utils.buildHeart(msg.getColor())
        );
    }

    private CharSequence buildGiftSequence(SendGiftMsg msg){
        MsgUtils utils = MsgUtils.getInstance();
        CharSequence level = utils.buildLevel(Integer.parseInt(msg.getData().getLevel()));
        //if level is not legal
        if (level == null) {
            level = "";
        }
        return TextUtils.concat(
                level,
                utils.buildUserName(msg.getData().getNickName()),
                utils.buildPublicMsgContent("送出"+msg.getData().getNum()+"个"),
                msg.getData().getGiftName()
        );
    }

}
