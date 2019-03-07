package com.hdw.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * @Description 生成UUId或随机数字
 * @Author TuMinglong
 * @Date 2018/12/10 16:48
 */
public class UUIDGenerator {

    /**
     * 获取32位随机字符串
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    /**
     * 获取19位订单ID或者table ID
     * @return
     */
    public static String getOrderNo() {
        String numStr = "";
        String trandStr = String.valueOf((Math.random() * 9 + 1) * 1000000);
        String dataStr = new SimpleDateFormat("yyyyMMddHHMMSS").format(new Date());
        numStr = trandStr.toString().substring(0, 5);
        numStr = numStr + dataStr;
        return numStr;
    }
}
