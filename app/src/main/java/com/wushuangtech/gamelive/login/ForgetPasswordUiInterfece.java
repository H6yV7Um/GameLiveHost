package com.wushuangtech.gamelive.login;

import com.wushuangtech.gamelive.base.BaseUiInterface;
import com.wushuangtech.gamelive.data.CodeBean;

/**
 * Created by Iverson on 2016/12/27 下午3:07
 * 此类用于：
 */

public interface ForgetPasswordUiInterfece extends BaseUiInterface {

    void getSendCodeSuccess(CodeBean bean);

    void  isSucess(String mobile);
}
