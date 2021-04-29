package org.kerw1n.javautil.http;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import sun.net.util.IPAddressUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP 工具类
 *
 * @author kerw1n
 */
public class IpUtil {
    private static final Logger LOG = LoggerFactory.getLogger(IpUtil.class);

    private static final String UNKNOWN = "unknown";
    private static final String[] LOCAL = {"0:0:0:0:0:0:0:1", "127.0.0.1"};

    private IpUtil() {
    }

    /**
     * 校验是否是局域网或本机 IP
     *
     * @param ip
     * @return
     */
    public static boolean isIntranetIp(String ip) {
        Assert.notNull(ip, "ip require not null.");
        if (isLocalIp(ip)) {
            return true;
        }
        byte[] bytes = IPAddressUtil.textToNumericFormatV4(ip);
        return isIntranetIp(bytes);
    }

    private static boolean isIntranetIp(byte[] bytes) {
        final byte b0 = bytes[0];
        final byte b1 = bytes[1];
        // 10.x.x.x
        final byte section1 = 0x0A;
        // 172.16.x.x
        final byte section2 = (byte) 0xAC;
        final byte section3 = (byte) 0x10;
        final byte section4 = (byte) 0x1F;
        // 192.168.x.x
        final byte section5 = (byte) 0xC0;
        final byte section6 = (byte) 0xA8;
        switch (b0) {
            case section1:
                return true;
            case section2:
                if (b1 >= section3 && b1 <= section4) {
                    return true;
                }
            case section5:
                if (b1 == section6) {
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * 是否是本机 IP
     *
     * @param ip
     * @return
     */
    private static boolean isLocalIp(String ip) {
        return LOCAL[0].equals(ip) || LOCAL[1].equals(ip);
    }

    /**
     * 获取用户 IP 地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        Assert.notNull(request, "request require not null.");

        String ip = request.getHeader("X-requested-For");
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (isLocalIp(ip)) {
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                }
            }
        }

        if (ip != null && ip.contains(",")) {
            String[] ips = ip.split(",");
            for (String elem : ips) {
                if (!(UNKNOWN.equalsIgnoreCase(elem))) {
                    ip = elem;
                    break;
                }
            }
        }
        return ip;
    }

}
