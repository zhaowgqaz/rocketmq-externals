package org.apache.rocketmq.spring.starter.utils;

import java.net.InetAddress;

import org.springframework.util.StringUtils;

/**
 * Copyright: Copyright (c) 2017  zteits
 *
 * @ClassName: com.clouds.constants.utils
 * @Description: IP地址工具类
 * @version: v1.0.0
 * @author: atao
 * @date: 2017/4/26   上午9:25
 * Modification History:
 * Date         Author          Version      Description
 * ---------------------------------------------------------*
 * 2017/4/26      atao          v1.0.0          创建
 */
public class IPUtil {
    private static String localHost;
    private static String localHostName;

    public static String getLocalHost() {
        if (StringUtils.isEmpty(localHost)) {
            getLocalHostInfo();
        }
        return localHost;
    }

    public static String getLocalHostNome() {
        if (StringUtils.isEmpty(localHostName)) {
            getLocalHostInfo();
        }
        return localHostName;
    }

    private static void getLocalHostInfo() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            localHostName = ia.getHostName();
            localHost = ia.getHostAddress();
        } catch (Exception e) {
            //获取当前地址失败
            e.printStackTrace();
        }
    }

}
