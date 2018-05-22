package com.wushuangtech.gamelive.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.base.BaseActivity;
import com.wushuangtech.gamelive.data.CodeBean;
import com.wushuangtech.gamelive.net.Constants;
import com.wushuangtech.gamelive.util.NumberUtils;


import java.util.concurrent.TimeUnit;

import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Iverson on 2016/12/27 下午2:40
 * 此类用于：忘记密码界面
 */

public class ForgetPasswordActivity extends BaseActivity implements ForgetPasswordUiInterfece {

    private String mCheckCode ;
    private CountDownTimer countDownTimer;
    private ForgetPasswordPresenter presenter;
    private EditText etForgetPhone;
    private TextView tvForgetCheckCode;
    private EditText etForgetCheckCode;
    private Button btForgetNextStep;
    private ImageView mIvBack;
    private RelativeLayout mRlPhoneStatus;

    public static Intent createIntent(Context context) {
        return new Intent(context, ForgetPasswordActivity.class);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_froget_password;
    }

    @Override
    protected void findViews() {
        presenter = new ForgetPasswordPresenter(this);
        etForgetPhone = findViewById(R.id.et_forget_phone);
        mRlPhoneStatus = findViewById(R.id.rl_phone_status);

        tvForgetCheckCode = findViewById(R.id.tv_forget_check_code);
        etForgetCheckCode = findViewById(R.id.et_forget_check_code);
        btForgetNextStep = findViewById(R.id.bt_forget_next_step);
        mIvBack = findViewById(R.id.iv_forget_1_back);


        etForgetPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mRlPhoneStatus.setPressed(true);
                }else {
                    mRlPhoneStatus.setPressed(false);
                }
            }
        });
    }


    @Override
    protected void init() {
        subscribeClick(mIvBack, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onBackPressed();
            }
        });

        RxView.clicks(tvForgetCheckCode).throttleFirst(Constants.VIEW_THROTTLE_TIME_SHORT, TimeUnit.SECONDS)
                .filter(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        if (!NumberUtils.isMobileNO(etForgetPhone.getText().toString())) {
                            toastShort(R.string.login_error_phone_number);
                            return false;
                        }
                        return countDownTimer == null;
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        presenter.getCheckCode(etForgetPhone.getText().toString().trim());
                    }
                });

        RxView.clicks(btForgetNextStep).throttleFirst(Constants.VIEW_THROTTLE_TIME_SHORT, TimeUnit.SECONDS)
                .filter(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        if (!NumberUtils.isMobileNO(etForgetPhone.getText().toString())) {
                            toastShort(R.string.login_error_phone_number);
                            return false;
                        } else if (TextUtils.isEmpty(etForgetCheckCode.getText().toString())) {
                            toastShort(R.string.login_check_code_password);
                            return false;
                        }else if (!etForgetCheckCode.getText().toString().equals(mCheckCode)) {
                            toastShort(R.string.code_error);
                            return false;
                        }
                        return true;
                    }
                })
             .subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                String phone = etForgetPhone.getText().toString();
                presenter.isRegiest(phone);
            }
        });
    }

    @Override
    public void getSendCodeSuccess(CodeBean bean) {
        mCheckCode = bean.getCode();
        tvForgetCheckCode.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvForgetCheckCode.setText(getString(R.string.regiest_captcha_countdown,
                        millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvForgetCheckCode.setText(R.string.regiest_get_check_code);
                tvForgetCheckCode.setEnabled(true);
                countDownTimer = null;
            }
        }.start();
    }

    @Override
    public void isSucess(String mobile) {
        finish();
        Intent intent = ResetPwdActivity.createIntent(ForgetPasswordActivity.this, mobile);
        startActivity(intent);
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
