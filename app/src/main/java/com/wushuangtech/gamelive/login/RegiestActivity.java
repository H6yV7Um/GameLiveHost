package com.wushuangtech.gamelive.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import com.jakewharton.rxbinding.view.RxView;
import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.base.BaseActivity;
import com.wushuangtech.gamelive.data.CodeBean;
import com.wushuangtech.gamelive.data.LoginInfo;
import com.wushuangtech.gamelive.domin.DataManager;
import com.wushuangtech.gamelive.net.Constants;
import com.wushuangtech.gamelive.util.NumberUtils;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Iverson on 2016/12/24 下午9:10
 * 此类用于：注册页面
 */

public class RegiestActivity extends BaseActivity implements RegiestUiInterface {

    private CountDownTimer countDownTimer;
    RegiestPresenter presenter ;
    private EditText etRegiestPhone;
    private TextView tvGetCheckCode;
    private EditText etRegiestCheckCode;
    private EditText etRegiestPassword;
    private Button btNextStep;
    private ImageView mIvBack;
    private EditText mEtComfirmPassword;
    private RelativeLayout mRlPhoneStatus;
    private RelativeLayout mRlPasswordStatus;
    private RelativeLayout mRlComfirmPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, RegiestActivity.class);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_regiest;
    }

    @Override
    protected void findViews() {
//        setBtGlobalLeft(R.mipmap.ic_global_nav_back);
        presenter = new RegiestPresenter(this);
        etRegiestPhone = findViewById(R.id.et_regiest_phone);
        tvGetCheckCode = findViewById(R.id.tv_get_check_code);
        etRegiestCheckCode = findViewById(R.id.et_regiest_check_code);
        etRegiestPassword = findViewById(R.id.et_regiest_password);
        btNextStep = findViewById(R.id.bt_next_step);
        mIvBack = findViewById(R.id.iv_regiest_back);

        mEtComfirmPassword = findViewById(R.id.et_regiest_password_confirm);
        mRlPhoneStatus = findViewById(R.id.rl_phone_status);
        mRlPasswordStatus = findViewById(R.id.rl_password_status);
        mRlComfirmPassword = findViewById(R.id.rl_password_confirm_status);


        etRegiestPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mRlPhoneStatus.setPressed(true);
                }else {
                    mRlPhoneStatus.setPressed(false);
                }
            }
        });

        etRegiestPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mRlPasswordStatus.setPressed(true);
                }else {
                    mRlPasswordStatus.setPressed(false);
                }
            }
        });


        mEtComfirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mRlComfirmPassword.setPressed(true);
                }else {
                    mRlComfirmPassword.setPressed(false);
                }
            }
        });

    }

    @Override
    protected void init() {

        RxView.clicks(tvGetCheckCode).throttleFirst(Constants.VIEW_THROTTLE_TIME_SHORT, TimeUnit.SECONDS)
                .filter(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        if(!NumberUtils.isMobileNO(etRegiestPhone.getText().toString())){
                            toastShort(R.string.login_error_phone_number);
                            return false;
                        }
                        return countDownTimer == null;
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        presenter.getCheckCode(etRegiestPhone.getText().toString().trim());
                    }
                });

        RxView.clicks(btNextStep).throttleFirst(Constants.VIEW_THROTTLE_TIME_SHORT, TimeUnit.SECONDS)
                .filter(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        if(!NumberUtils.isMobileNO(etRegiestPhone.getText().toString())){
                            toastShort(R.string.login_error_phone_number);
                            return false;
                        }else if(TextUtils.isEmpty(etRegiestCheckCode.getText().toString())){
                            toastShort(R.string.login_check_code_password);
                            return false;
                        }else if(TextUtils.isEmpty(mEtComfirmPassword.getText().toString())){
                            toastShort("确认密码不能为空");
                            return false;
                        }else if(TextUtils.isEmpty(etRegiestPassword.getText().toString())){
                            toastShort(R.string.login_error_password);
                            return false;
                        }else if(etRegiestPassword.getText().length()<6||etRegiestPassword.getText().length()>12){
                            toastShort("密码不能少于6位或者大于12位");
                            return false;
                        }else if(!etRegiestPassword.getText().toString().equals(mEtComfirmPassword.getText().toString())){
                            toastShort("两次输入的密码不一致");
                            return false;
                        }else if (!etRegiestCheckCode.getText().toString().trim().equals(validation_code)){
                            toastShort(R.string.code_error);
                            return false;
                        }
                        return true;
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        String phoneNumber = etRegiestPhone.getText().toString().trim();
                        String passWord = etRegiestPassword.getText().toString();
                        presenter.regiestUserNow(phoneNumber,passWord);
                    }
                });
        subscribeClick(mIvBack, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onBackPressed();
            }
        });

    }

    private String validation_code;

    @Override
    public void getSendCodeSuccess(CodeBean codeBean) {
        validation_code =codeBean.getCode();
        Log.e("regiestActivity_验证码 " , validation_code);
        tvGetCheckCode.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvGetCheckCode.setText(getString(R.string.regiest_captcha_countdown,
                        millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvGetCheckCode.setText(R.string.regiest_get_check_code);
                tvGetCheckCode.setEnabled(true);
                countDownTimer = null;
            }
        }.start();
    }

    @Override
    public void regiestSuccess() {
        toastShort("注册成功,请登录");
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unsubscribeTasks();
        if(countDownTimer != null){
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

}
