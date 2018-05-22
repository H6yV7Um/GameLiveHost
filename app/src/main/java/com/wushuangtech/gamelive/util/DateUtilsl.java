package com.wushuangtech.gamelive.util;

import android.content.Context;
import android.util.Log;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lw on 2016/7/26.
 */

public class DateUtilsl {
    private SimpleDateFormat sf = null;
    /*获取系统时间 格式为："yyyy/MM/dd "*/
    public static String getCurrentDate() {
        Date d = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sf.format(d);
        }
    /*时间戳转换成字符窜*/
    public static String getDateToString(String time) {
//        Date d = new Date(time);
//        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
//        return sf.format(d);
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        long lcc_time = Long.valueOf(time);
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));
        return re_StrTime;
    }

    /*时间戳转换成字符窜*/
    public static String getDateToStringLong(String time) {
//        Date d = new Date(time);
//        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
//        return sf.format(d);
        String re_StrTime = null;
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lcc_time = Long.valueOf(time);
        re_StrTime = sdf.format(new Date(lcc_time));
        return re_StrTime;
    }

    /*将字符串转为时间戳*/
    public static long getStringToDate(String time) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date();
        try{
            date = sf.parse(time);
            } catch(ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            }
        return date.getTime();
        }
    static SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 将时间戳转换为"距离现在多久"的字符串
     */
    public static String getDateToToday(String timeServer) {

        long timeStart = 0;
        try {
            timeStart = sdf.parse(timeServer).getTime();
            //获取当前时间与获取时间的差值
            long newTime = System.currentTimeMillis() - (timeStart);
            //获取天数
            long day = newTime / 24 / 60 / 60 / 1000;
            //获取小时值
            long hour = (newTime - day * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            //获取分值
            long minute = (newTime - day * (1000 * 60 * 60 * 24) - hour * (1000 * 60 * 60)) / (1000 * 60);
            if (day >= 1) {
                return day + "天" + hour + "小时" + minute + "分钟前";
            } else {
                if (hour >= 1) {
                    return hour + "小时" + minute + "分钟前";
                } else {
                    if (minute >= 1) {
                        return minute + "分钟前";
                    } else {
                        return "刚刚";
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }

    }


    /**
     * 支付尾款剩余时间的字符串
     */
    public static String getVoucherTimeString(Context context, int voucherTime) {

        try {

            int day = voucherTime/(60*60*24);//日
            int hours = (voucherTime - day*60*60*24)/(60*60);//时
            Integer minutes = (int)(voucherTime/60 - hours*60 - day * 24 * 60);//分
            Integer seconds = (int)(voucherTime-minutes*60-hours*60*60 - day * 24 * 60 * 60);//秒

            Log.e("VoucherTimeString","time："+day+"天   --"+hours+"小时"+" "+minutes+"分钟 "+seconds+"秒");

            if(day != 0){
           //    return context.getString(R.string.voucher_time_type1,""+day);
                return  "剩余支付时间"+day+"天 "+hours+"小时 "+minutes+"分钟 "+seconds+"秒";
            }else if(hours != 0){
                //return context.getString(R.string.voucher_time_type2,""+hours);
                return  "剩余支付时间"+day+"天 "+hours+"小时 "+minutes+"分钟 "+seconds+"秒";
            }else {
             //   return context.getString(R.string.voucher_time_type3,""+voucherTime/60);
                return  "剩余支付时间"+day+"天 "+hours+"小时 "+minutes+"分钟 "+seconds+"秒";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
