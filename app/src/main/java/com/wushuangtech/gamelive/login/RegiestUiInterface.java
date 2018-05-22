package com.wushuangtech.gamelive.login;

import com.wushuangtech.gamelive.base.BaseUiInterface;
import com.wushuangtech.gamelive.data.CodeBean;

/**
 * Created by Iverson on 2016/12/27 下午1:55
 * 此类用于：
 */

public interface RegiestUiInterface extends BaseUiInterface {

    void getSendCodeSuccess(CodeBean codeBean);

    void regiestSuccess();
}
