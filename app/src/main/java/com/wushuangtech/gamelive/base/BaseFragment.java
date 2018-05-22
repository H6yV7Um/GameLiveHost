package com.wushuangtech.gamelive.base;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wushuangtech.gamelive.R;
import com.wushuangtech.gamelive.util.CustomToast;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by Iverson on 2016/12/23 下午10:16
 * 此类用于：Fragment的基类
 */
@SuppressWarnings("unused")
public abstract class BaseFragment extends Fragment implements BaseUiInterface {

    private String LOG_TAG = this.getClass().getSimpleName();
    private ProgressDialog mProgressDialog;
    protected FragmentActivity mContext;
    protected float mDensity;
    protected int mDensityDpi;
    protected int mWidth;
    protected int mAvatarSize;

    /**
     * 初始化title的布局
     */
    protected ImageButton mBtTitleLeft;
    protected ImageButton mBtTitleRight;
    protected TextView mTvTitleName;
    protected TextView mTvTitleLeft;
    protected TextView mTvTitleRight;

    private Dialog dialog;

    private Activity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();

        //订阅接收消息,子类只要重写onEvent就能收到消息
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mWidth = dm.widthPixels;
        mAvatarSize = (int) (50 * mDensity);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(getLayoutId(),null);
        initViews(view);
        return view;

    }

    @SuppressWarnings("unchecked")
    protected final <T extends View> T $(@NonNull View view, @IdRes int id) {
        return (T) (view.findViewById(id));
    }


    protected abstract int getLayoutId();

    protected abstract void initViews(View view);

    @Override
    public void showDataException(String msg) {
        toastShort(msg);
    }

    @Override
    public void showNetworkException() {
        toastShort(R.string.msg_network_error);
    }

    @Override
    public void showUnknownException() {
        toastShort(R.string.msg_unknown_error);
    }

    @Override
    public void showLoadingComplete() {
        //Empty implementation
        dismissLoadingDialog();
    }

    public Dialog showLoadingDialog() {
        if (mProgressDialog!=null && mProgressDialog.isShowing()){
            Log.e(LOG_TAG, "Call show loading dialog while dialog is still showing, is there a bug?");
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.Please_wait), true, false);
        return mProgressDialog;
    }

    @Override
    public void dismissLoadingDialog() {
        if (mProgressDialog==null || (!mProgressDialog.isShowing())){
//            L.e(LOG_TAG, "Try to dismiss a dialog but dialog is null or already dismiss!");
            return ;
        }
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }

    protected void toastShort(@StringRes int msg){
        if (getActivity()!=null) {
            CustomToast.makeCustomText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    protected void toastShort(@NonNull String msg){
        if (getActivity()!=null) {
            CustomToast.makeCustomText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    protected void toastLong(@NonNull String msg){
        if(getActivity() != null){
            CustomToast.makeCustomText(getActivity(), msg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
