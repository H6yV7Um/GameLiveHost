package com.wushuangtech.gamelive.data.pubmsg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.SparseArray;

import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.RoomSDK;
import com.wushuangtech.gamelive.util.PicUtil;
import com.wushuangtech.gamelive.util.Spans;
import com.wushuangtech.gamelive.widget.heart.HeartUtil;


/**
 * @author Muyangmin
 * @since 1.0.0
 * 发送的信息工具
 */
public final class MsgUtils {

    public static final String LOG_TAG = "MsgUtils";

    private static MsgUtils instance;

    private SparseArray<CharSequence> levelSequence = new SparseArray<>(128);
    private static final boolean CACH_IMAGE_ENABLED = false;

//   进入提示颜色
    @ColorInt
    private int colorUsername;
    //  聊天 用户名称颜色
    @ColorInt
    private int colorsUsername;
//    用户发送的信息颜色
    @ColorInt
    private int colorPublicMsgContent;
//  系统发出的警告颜色
    @ColorInt
    private int colorPublicSysMsgContent;
    //  系统发出的欢迎颜色
    private int colorPrvMsgContent;
    @ColorInt
    private int colorPublicSysMsgWelcome;

    private int[] heartColorArray;

    private MsgUtils(Context context) {
        colorUsername = getColor(context, R.color.yunkacolor_60);
        colorsUsername= getColor(context,R.color.yunkacolor_h);
        colorPublicMsgContent = getColor(context, R.color.item_public_msg_content);
        colorPublicSysMsgContent=getColor(context, R.color.continue_gift_x_border);
        colorPublicSysMsgWelcome=getColor(context, R.color.yunkacolor_60);
        colorPrvMsgContent = getColor(context, R.color.black);
        heartColorArray = context.getResources().getIntArray(R.array.room_heart_colors);
    }

    @ColorInt
    private int getColor(Context context, @ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    public static MsgUtils getInstance() {
        if (instance == null) {
            synchronized (MsgUtils.class) {
                if (instance == null) {
                    instance = new MsgUtils(RoomSDK.getContext());
                }
            }
        }
        return instance;
    }

    public CharSequence buildUserName(@NonNull String username) {
        return Spans.createSpan("", String.format("%s:", username),
                new ForegroundColorSpan(colorUsername));
    }

    public CharSequence buildimUserName(@NonNull String username) {
        return Spans.createSpan("", String.format("%s:", username),
                new ForegroundColorSpan(colorsUsername));
    }

    public CharSequence buildPublicMsgContent(@NonNull String msg) {
        return Spans.createSpan("", msg, new ForegroundColorSpan(colorPublicMsgContent));
    }

    public CharSequence buildPublicSysMsgContent(@NonNull String msg) {
        return Spans.createSpan("", msg, new ForegroundColorSpan(colorPublicSysMsgContent));
    }

    public CharSequence buildPublicSysMsgWelcome(@NonNull String welcome) {
        return Spans.createSpan("", welcome, new ForegroundColorSpan(colorPublicSysMsgWelcome));
    }

    public CharSequence buildPublicSysMsgName(@NonNull String msg) {
        return Spans.createSpan("", msg, new ForegroundColorSpan(colorUsername));
    }
    public CharSequence buildPrvMsgContent(@NonNull String msg) {
        return Spans.createSpan("", msg, new ForegroundColorSpan(colorPrvMsgContent));
    }

    @Nullable
    @CheckResult
    public CharSequence buildLevel(int level) {
        if (level == 0) {
            //not supported at all
            return null;
        }
        if (CACH_IMAGE_ENABLED){
            CharSequence cached = levelSequence.get(level);
            //Cache hit
            if (cached != null) {
                return cached;
            }
        }

        //not hit
        Context context = RoomSDK.getContext();
        int resId = PicUtil.getLevelImageId(context, level);

        //use customize size
        int width = context.getResources().getDimensionPixelSize(R.dimen.user_level_width);
        int height = context.getResources().getDimensionPixelSize(R.dimen.user_level_height);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        if (bitmap == null) {
            return null;
        }
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        ImageSpan span = new ImageSpan(context, scaledBitmap);
        SpannableStringBuilder ssb = new SpannableStringBuilder(String.valueOf(level));
        ssb.setSpan(span, 0, ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        levelSequence.put(level, ssb);
        return ssb;
    }

    public CharSequence buildHeart(int colorIndex){
        int color = heartColorArray[colorIndex];

        Context context = RoomSDK.getContext();
        int width = context.getResources().getDimensionPixelSize(R.dimen.room_heart_fixed_size);
        int height = context.getResources().getDimensionPixelSize(R.dimen.room_heart_fixed_size);
        //Must use a BitmapConfig with Alpha channel!
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(color);
        HeartUtil.drawHeart(canvas, 0.72F, color);

        ImageSpan span = new ImageSpan(context, bitmap);
        SpannableStringBuilder ssb = new SpannableStringBuilder(String.valueOf(colorIndex));
        ssb.setSpan(span, 0, ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        levelSequence.put(colorIndex, ssb);
        return ssb;
    }
}
