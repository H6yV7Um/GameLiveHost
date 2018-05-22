package com.wushuangtech.gamelive.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.base.BaseActivity;
import com.wushuangtech.gamelive.net.Constants;
import com.wushuangtech.gamelive.room.RoomLiveActivity;
import com.wushuangtech.gamelive.room.StartLiveBySignActivity;
import com.wushuangtech.gamelive.util.NumberUtils;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Iverson on 2016/12/23 下午10:15
 * 此类用于：登录界面
 */

public class LoginActivity extends BaseActivity implements LoginUiinterface{
    LoginPresenter presenter ;
    public  String phoneNumber;
    private EditText mEtLoginInputPhone;
    private EditText mEtLoginInputPassword;
    private TextView mTvLoginForgetPassword;
    private TextView mTvLoginRegiestUser;
    private Button mBtLoginNow;
    private ImageView mIvBack;
    private RelativeLayout mRlPhoneStatus;
    private RelativeLayout mRlPasswordStatus;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void findViews() {
        presenter = new LoginPresenter(this);

        mRlPhoneStatus = findViewById(R.id.rl_phone_status);
        mRlPasswordStatus = findViewById(R.id.rl_password_status);

        mEtLoginInputPhone = findViewById(R.id.et_login_input_phone);
        mEtLoginInputPassword = findViewById(R.id.et_login_input_password);
        mTvLoginForgetPassword = findViewById(R.id.tv_login_forget_password);
        mTvLoginRegiestUser = findViewById(R.id.tv_login_regiest);
        mBtLoginNow = findViewById(R.id.bt_login_now);
        mIvBack = findViewById(R.id.iv_login_back);
    }

    @Override
    protected void init() {
        subscribeClick(mIvBack, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onBackPressed();
            }
        });
        RxView.clicks(mBtLoginNow).throttleFirst(Constants.VIEW_THROTTLE_TIME_SHORT, TimeUnit.SECONDS)
                .filter(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        if(!NumberUtils.isMobileNO(mEtLoginInputPhone.getText().toString())){
                            toastShort(R.string.login_error_phone_number);
                            return false;
                        }else if(TextUtils.isEmpty(mEtLoginInputPassword.getText().toString())){
                            toastShort(R.string.login_error_password);
                            return false;
                        }
                        return true;
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        phoneNumber = mEtLoginInputPhone.getText().toString().trim();
                        //String passWord = MD5Util.string2MD5(mEtLoginInputPassword.getText().toString());
                        String passWord = mEtLoginInputPassword.getText().toString();
                        presenter.loginNow(phoneNumber,passWord);
                    }
                });

        RxView.clicks(mTvLoginRegiestUser).throttleFirst(Constants.VIEW_THROTTLE_TIME_SHORT,
                TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                Intent i = RegiestActivity.createIntent(LoginActivity.this);
                startActivity(i);
            }
        });


        RxView.clicks(mTvLoginForgetPassword).throttleFirst(Constants.VIEW_THROTTLE_TIME_SHORT,
                TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                Intent i = ForgetPasswordActivity.createIntent(LoginActivity.this);
                startActivity(i);
            }
        });

        mEtLoginInputPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mRlPhoneStatus.setPressed(true);
                }else {
                    mRlPhoneStatus.setPressed(false);
                }
            }
        });

        mEtLoginInputPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mRlPasswordStatus.setPressed(true);
                }else {
                    mRlPasswordStatus.setPressed(false);
                }
            }
        });

    }

    @Override
    public void onLoginSuccess() {
//        startActivity(RoomLiveActivity.createIntent(LoginActivity.this,new Bundle()));
        startActivity(new Intent(LoginActivity.this,StartLiveBySignActivity.class));
        finish();
    }

    @Override
    public void onLoginFailuer() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unsubscribeTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dismissLoadingDialog();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
