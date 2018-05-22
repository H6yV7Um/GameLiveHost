package com.wushuangtech.gamelive.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.base.BaseActivity;
import com.wushuangtech.gamelive.net.Constants;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Iverson on 2016/12/27 下午3:03
 * 此类用于：忘记密码之后重置密码
 */

public class ResetPwdActivity extends BaseActivity implements ResetPwdUiInterface {


    public final static String RESET_PHONE_NUMBER = "Phone";
    private ResetPwdPresenter presenter;
    private String inputonce ;
    private String phone ;
    private EditText etResetPassword;
    private EditText etResetPasswordComfirm;
    private Button mBtComfirm;
    private ImageView mIvBack;
    private RelativeLayout mRlresetComfirm;
    private RelativeLayout mRlresetPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }
    public static Intent createIntent(Context context, String phone) {
        Intent intent = new Intent(context,ResetPwdActivity.class);
        intent.putExtra(RESET_PHONE_NUMBER,phone);
        return intent;
    }

    @Override
    protected void parseIntentData(Intent intent, boolean isFrom) {
        super.parseIntentData(intent, isFrom);
        phone = intent.getStringExtra(RESET_PHONE_NUMBER);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_reset_password;
    }

    @Override
    protected void findViews() {
        presenter = new ResetPwdPresenter(this);
        etResetPassword = findViewById(R.id.et_reset_password);
        mRlresetPassword = findViewById(R.id.rl_reset_phone_status);
        etResetPasswordComfirm = findViewById(R.id.et_reset_password_comfirm);
        mRlresetComfirm = findViewById(R.id.rl_reset_confirm_status);
        mBtComfirm = findViewById(R.id.bt_reset_comfirm);
        mIvBack = findViewById(R.id.iv_forget_2_back);


        etResetPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mRlresetPassword.setPressed(true);
                }else {
                    mRlresetPassword.setPressed(false);
                }
            }
        });

        etResetPasswordComfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mRlresetComfirm.setPressed(true);
                }else {
                    mRlresetComfirm.setPressed(false);
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

        RxView.clicks(mBtComfirm).throttleFirst(Constants.VIEW_THROTTLE_TIME_SHORT, TimeUnit.SECONDS)
                .filter(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        inputonce =   etResetPassword.getText().toString();
                        String inputtwo = etResetPasswordComfirm.getText().toString();
                        if(TextUtils.isEmpty(inputonce)){
                            toastShort("输入新密码不能为空！");
                            return false;
                        }else if(TextUtils.isEmpty(inputtwo)){
                            toastShort("输入确认密码不能为空！");
                            return false;
                        }else if(!inputtwo.equals(inputonce)){
                            toastShort("两次输入密码不一致！");
                            return false;
                        }else if(etResetPassword.getText().length()<6||etResetPassword.getText().length()>18){
                            toastShort("密码长度不能少于6位或者大于18位");
                            return false;
                        }
                        return true;
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        presenter.ForgetNextStep(phone,inputonce);
                    }
                });
    }

    @Override
    public void resetPwdSuccess() {
        toastShort("重置密码成功");
        finishActivityByName(ForgetPasswordActivity.class);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unsubscribeTasks();
    }

}
