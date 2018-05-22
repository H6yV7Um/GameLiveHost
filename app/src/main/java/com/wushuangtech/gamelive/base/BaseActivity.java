package com.wushuangtech.gamelive.base;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.wushuangtech.gamelive.Config;
import com.wushuangtech.gamelive.MainApplication;
import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.net.Constants;
import com.wushuangtech.gamelive.util.CustomToast;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;


/**
 * Created by Iverson on 2016/12/23 下午6:06
 * 此类用于：界面activity的基类
 */

public abstract class BaseActivity extends AppCompatActivity implements BaseUiInterface {

    protected Context mContext;
    protected ProgressDialog mProgressDialog;
    /**
     * 初始化title的布局
     */
    protected ImageButton mBtTitleLeft;
    protected ImageButton mBtTitleRight;
    protected TextView mTvTitleName;
    protected TextView mTvTitleLeft;
    protected TextView mTvTitleRight;


    protected abstract int getLayoutId();

    protected abstract void findViews();

    protected abstract void init();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        StatusBarUtils.setStatuBarAndBottomBarTranslucent(this);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        Intent intent = getIntent();
        parseIntentData(intent, false);
        MainApplication.getInstance().addActivity(this);
        mContext = this;
        initTitleView();
        findViews();
        init();

    }

    @Override
    public void showDataException(String msg) {
        toastShort(msg);
    }

    @Override
    public void showNetworkException() {
        //toastShort(R.string.msg_network_error);
    }

    @Override
    public void showUnknownException() {
        toastShort(R.string.msg_unknown_error);
    }

    protected void toastShort(@StringRes int msg){
        CustomToast.makeCustomText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void toastShort(@NonNull String msg){
        CustomToast.makeCustomText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntentData(intent, true);
    }

    protected void parseIntentData(Intent intent, boolean isFrom){

    }

    /**
     * 初始化title的布局
     * 注：这些空间可能有的activity没有，所以不能bufferknife来注解
     */
    protected  void initTitleView(){
        mBtTitleLeft = (ImageButton) findViewById(R.id.bt_global_title_left);
        mBtTitleRight = (ImageButton) findViewById(R.id.bt_global_title_right);
        mTvTitleName = (TextView) findViewById(R.id.tv_global_title_name);
        mTvTitleLeft = (TextView) findViewById(R.id.tv_global_title_left);
        mTvTitleRight = (TextView) findViewById(R.id.tv_global_title_right);
    }
    /**
     * 左边的布局ImageButton
     * @param t
     * @param <T>
     */
    protected <T> void setBtGlobalLeft(T t){
        if(t instanceof Integer){
            mBtTitleLeft.setImageResource((Integer) t);
        }else if(t instanceof Drawable){
            mBtTitleLeft.setImageDrawable((Drawable) t);
        }
        mBtTitleLeft.setVisibility(View.VISIBLE);
    }

    /**
     * 右边的布局ImageButton
     * @param t
     * @param <T>
     */
    protected <T> void setBtGlobalRight(T t){
        if(t instanceof Integer){
            mBtTitleRight.setImageResource((Integer) t);
        }else if(t instanceof Drawable){
            mBtTitleRight.setImageDrawable((Drawable) t);
        }
        mBtTitleRight.setVisibility(View.VISIBLE);
    }

    /**
     * 左边的布局TextView
     * @param t
     * @param <T>
     */
    protected <T> void setTvGlobalLeft(T t){
        if(t instanceof Integer){
            mTvTitleLeft.setText((Integer) t);
        }else if(t instanceof String){
            mTvTitleLeft.setText((String)t);
        }
        mTvTitleLeft.setVisibility(View.VISIBLE);
    }

    /**
     * 右边的布局TextView
     * @param t
     * @param <T>
     */
    protected <T> void setTvGlobalRight(T t){
        if(t instanceof Integer){
            mTvTitleRight.setText((Integer) t);
        }else if(t instanceof String){
            mTvTitleRight.setText((String)t);
        }
        mTvTitleRight.setVisibility(View.VISIBLE);
    }


    /**
     * 中间的布局title的TextView
     * @param t
     * @param <T>
     */
    protected <T> void setTvGlobalTitleName(T t){
        if(t instanceof Integer){
            mTvTitleName.setText((Integer) t);
        }else if(t instanceof String){
            mTvTitleName.setText((String)t);
        }
        mTvTitleName.setVisibility(View.VISIBLE);
    }

    /**
     * 指定销毁activity
     * @param cls
     */
    protected void finishActivityByName(@NonNull Class<? extends Activity> cls){
        for (Activity activity : MainApplication.getInstance().activities){
            if(activity.getClass().equals(cls)){
                activity.finish();
            }
        }
    }

    /**
     * 使用默认的throttle设置来注册点击事件。
     * @see #subscribeClick(View, Action1)
     */
    protected void subscribeClick(@IdRes int id, Action1<Void> action1){
        subscribeClick(findViewById(id), action1);
    }

    /**
     * 使用默认的throttle设置来注册点击事件。
     * @param view 要注册的View
     * @param action1 点击后执行的事件
     */
    protected void subscribeClick(View view, Action1<Void> action1){
        RxView.clicks(view)
                .throttleFirst(Config.VIEW_THROTTLE_TIME, TimeUnit.SECONDS)
                .subscribe(action1);
    }

    @SuppressWarnings("unchecked")
    protected final <T extends View> T $(@IdRes int id) {
        return (T) (findViewById(id));
    }
    /**
     * Convenient call of {@link View#findViewById(int)}, automatically cast the result object.
     *
     * @param view The view object which contains target object.
     * @param id   The aapt-generated unique id.
     * @param <T>  The declared type of this widget.
     * @return The view object, or null if not found.
     */
    @SuppressWarnings("unchecked")
    protected final <T extends View> T $(@NonNull View view, @IdRes int id) {
        return (T) (view.findViewById(id));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApplication.getInstance().deleActvitity(this);
    }

    @Override
    public void showLoadingComplete() {
        //Empty implementation
        dismissLoadingDialog();
    }

    @Override
    public Dialog showLoadingDialog() {
        if (mProgressDialog!=null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.Please_wait), true, false);
        return mProgressDialog;
    }

    @Override
    public void dismissLoadingDialog() {
        if (mProgressDialog==null || (!mProgressDialog.isShowing())){
            return ;
        }
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }


//    protected int getAndoirdHeight(){
//        DisplayMetrics dm = new DisplayMetrics();
//        //取得窗口属性
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        //窗口的宽度
//        return dm.heightPixels;
//    }
//    protected int getWidth(){
//        WindowManager wm = (WindowManager)this
//                .getSystemService(Context.WINDOW_SERVICE);
//        return wm.getDefaultDisplay().getWidth();
//    }



    //-----------------------点击空白区域隐藏键盘-------------------------------
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     *
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
