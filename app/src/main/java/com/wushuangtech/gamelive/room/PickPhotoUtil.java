package com.wushuangtech.gamelive.room;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.base.BaseActivity;
import com.wushuangtech.gamelive.util.CustomToast;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huanzhang on 2016/5/11.
 */
public class PickPhotoUtil {
    private BaseActivity mContext;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "space";
    public PickPhotoUtil(BaseActivity context) {
        mContext = context;
    }
    public void startCropActivity(@NonNull Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME;
                destinationFileName += getPhotoFileName();
        //获取裁剪图片的类
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(mContext.getExternalCacheDir(), destinationFileName)));

        uCrop = basisConfig(uCrop);
        uCrop = advancedConfig(uCrop);

        uCrop.start(mContext);
    }

    /**
     * In most cases you need only to set crop aspect ration and max size for resulting image.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private UCrop basisConfig(@NonNull UCrop uCrop) {
                uCrop = uCrop.withAspectRatio(1, 1);
                    uCrop = uCrop.withMaxResultSize(1024,1024);

          return uCrop;
    }

    /**
     * Sometimes you want to adjust more options, it's done via {@link UCrop.Options} class.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);

//        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(false);

        /*
        If you want to configure how gestures work for all UCropActivity tabs

        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        * */

        /*
        This sets max size for bitmap that will be decoded from source Uri.
        More size - more memory allocation, default implementation uses screen diagonal.

        options.setMaxBitmapSize(640);
        * */


       /*

        Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);
        options.setDimmedLayerColor(Color.CYAN);
        options.setOvalDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setCropGridStrokeWidth(20);
        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(2);
        options.setCropGridRowCount(1);
   */
        // Color palette
        options.setToolbarColor(ContextCompat.getColor(mContext, R.color.colorIcons));
        options.setStatusBarColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        options.setActiveWidgetColor(ContextCompat.getColor(mContext, R.color.colorPrimaryLight));
		options.setToolbarWidgetColor(ContextCompat.getColor(mContext, R.color.txt_color));

        return uCrop.withOptions(options);
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e("huan", "handleCropError: ", cropError);
            CustomToast.makeCustomText(mContext, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            CustomToast.makeCustomText(mContext, "未知异常", Toast.LENGTH_SHORT).show();
        }
    }
    // 使用系统当前日期加以调整作为照片的名称
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }
}
