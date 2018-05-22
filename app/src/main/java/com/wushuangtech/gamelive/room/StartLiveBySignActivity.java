package com.wushuangtech.gamelive.room;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding.view.RxView;
import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.base.BaseActivity;
import com.wushuangtech.gamelive.data.LoginInfo;
import com.wushuangtech.gamelive.data.PublishRoomIdBean;
import com.wushuangtech.gamelive.domin.DataManager;
import com.wushuangtech.gamelive.util.CreateReuqestBodyUtil;
import com.wushuangtech.gamelive.util.FileUtils;
import com.wushuangtech.gamelive.websocket.Networks;
import com.yalantis.ucrop.UCrop;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.RequestBody;
import rx.functions.Action1;

/**
 * Created by Iverson on 2017/2/7 上午10:49
 * 此类用于：签约主播开播界面
 */

public class StartLiveBySignActivity extends BaseActivity implements View.OnClickListener,PublishUiInterface {

    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择

    private int IMGTYPE;
    private final int IMGPHOPOBG = 0;

    private PopupWindow mImgPopup;
    private PickPhotoUtil mPickUtil;
    private TextView mpopuProfileDis;
    private File tempFile;

    private PublishFragmentPresenter presenter;
    private String liveTitle;
    private LoginInfo loginInfo;

    public static String strland = "1";
    private SimpleDraweeView mSimple;
    private TextView mTextview_live;
    LinearLayout mLLRootView;
    Button mBtStartLive;
    private EditText mEtLiveTitle;
    private FrameLayout upImageLive;

    private Map<String, RequestBody> bodyMapFile;
    private File mTxFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_by_sign;
    }


    public static Intent CreateIntent(Context context){
        Intent intent = new Intent(context,StartLiveBySignActivity.class);
        return intent;
    }

    @Override
    protected void findViews() {
        mSimple = findViewById(R.id.live_bg_sdv);
        mTextview_live = findViewById(R.id.tv_live_log);
        mLLRootView = findViewById(R.id.ll_live_start_root);
        mBtStartLive = findViewById(R.id.bt_start_live_by_sign);
        mEtLiveTitle = findViewById(R.id.et_live_title_by_sign);
        upImageLive = findViewById(R.id.up_image_live);


        setTvGlobalTitleName(getString(R.string.go_to_live));
        setTvGlobalRight("退出");
        loginInfo = DataManager.getInstance().getLoginInfo();
        mPickUtil = new PickPhotoUtil(this);
        tempFile = new File(this.getExternalCacheDir(), getPhotoFileName());
        //mSimple.setImageURI(Uri.parse(("res:///" + R.drawable.start_live_bg)));
        presenter = new PublishFragmentPresenter(this);
    }
    // 使用系统当前日期加以调整作为照片的名称
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }
    @Override
    protected void init() {

        subscribeClick(mTvTitleRight, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                finish();
            }
        });
        upImageLive.setOnClickListener(this);

        mEtLiveTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().contains(" ")){
                    String[] str = s.toString().split(" ");
                    String str1 = "";
                    for (int i = 0; i < str.length; i++) {
                        str1 += str[i];
                    }
                    mEtLiveTitle.setText(str1);
                    mEtLiveTitle.setSelection(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        RxView.clicks(mBtStartLive)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        startLive();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.up_image_live:
                showUPImgPop(IMGPHOPOBG);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //无论修改哪一项，只要成功了就认为返回时需要刷新
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
        }


        switch (requestCode) {
            case PHOTO_REQUEST_TAKEPHOTO:
                if (tempFile.exists()) {
                    try {
                        mPickUtil.startCropActivity(Uri.fromFile(tempFile));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PHOTO_REQUEST_GALLERY:
                if (data != null && data.getData() != null) {
                    Uri selectedUri = data.getData();
                    if (selectedUri != null) {
                        mPickUtil.startCropActivity(data.getData());
                    }
                }
                break;


            case UCrop.REQUEST_CROP:
                if (data != null) {
                    Uri uri = UCrop.getOutput(data);
                    if (IMGTYPE == IMGPHOPOBG) {
                        mTextview_live.setVisibility(View.GONE);
                        String path = uri.getPath();
                        mSimple.setImageURI(uri);
                        mTxFile = new File(path);
                        bodyMapFile = createBgImageFile(mTxFile);
                    }
                }
        }

    }

    //    泡泡的
    private Button popuPhoto, popuCamera, popuCancel;

    private void showUPImgPop(int type) {
        if (type == IMGPHOPOBG) {
            IMGTYPE = IMGPHOPOBG;
        }
        View view = LayoutInflater.from(this).inflate(R.layout.edit_profile_up_avatar, null);
        mImgPopup = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 使其聚集
        mImgPopup.setFocusable(true);
        // 设置允许在外点击消失
        mImgPopup.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        mImgPopup.setBackgroundDrawable(new BitmapDrawable());
        mImgPopup.setAnimationStyle(R.style.popwin_anim_style_authen);
        mpopuProfileDis = (TextView) view.findViewById(R.id.tv_bg_record_profile);
        mpopuProfileDis.setOnClickListener(listener);
        popuCamera = (Button) view.findViewById(R.id.profile_popu_camera);
        popuCamera.setOnClickListener(listener);
        popuPhoto = (Button) view.findViewById(R.id.profile_popu_photo);
        popuPhoto.setOnClickListener(listener);
        popuCancel = (Button) view.findViewById(R.id.profile_popu_cancel);
        popuCancel.setOnClickListener(listener);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_live_by_sign, null);
        mImgPopup.showAtLocation(root, Gravity.BOTTOM, 0, 0);
    }


    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

              case R.id.tv_bg_record_profile:
                    mImgPopup.dismiss();
                    break;
                case R.id.profile_popu_camera://拍照
                    try {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri u = Uri.fromFile(tempFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
                        startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
                    } catch (Exception e) {
                        toastShort(getString(R.string.start_camera_failed));
                    }
                    mImgPopup.dismiss();
                    break;
                case R.id.profile_popu_photo://选择照片
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    startActivityForResult(i, PHOTO_REQUEST_GALLERY);
                    mImgPopup.dismiss();
                    break;

                case R.id.profile_popu_cancel:
                    mImgPopup.dismiss();
                    break;
            }
        }
    };


    @Override
    public void upImageSuccess() {

        if(tempFile != null){
            FileUtils.deleteDir();
        }

        if(mTxFile!=null){
            FileUtils.deleteDir();
        }

        startActivity(RoomLiveActivity.createIntent(this,  new Bundle()));
    }


    private Map<String,RequestBody> createBgImageFile(File mTxFile) {
        Map<String, RequestBody> bodyMap = CreateReuqestBodyUtil.uploadBackgroudImage(mTxFile);
        return  bodyMap ;
    }


    /**
     * 直播开始
     */
    private void startLive(){
        liveTitle = mEtLiveTitle.getText().toString();
        if(TextUtils.isEmpty(liveTitle)||"".equals(liveTitle.trim())){
            toastShort(getString(R.string.The_theme_cannot_be_empty));
            return;
        }
        //开直播
        if(!Networks.isNetworkConnected(StartLiveBySignActivity.this)){
            toastShort(getString(R.string.network_disconnected_hint));
            return;
        }

        if(bodyMapFile==null){
            toastShort("请上传一张背景图片");
            return;
        }



        presenter.isOpenLive(loginInfo.getUserId(),liveTitle,bodyMapFile,liveTitle);
    }
}
