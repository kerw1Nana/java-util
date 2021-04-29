package org.kerw1n.javautil.file;

import org.kerw1n.javautil.constant.BaseConst;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * IO 操作工具类
 *
 * @author kerw1n
 */
public class IoUtil {

    private IoUtil() {
    }

    /**
     * 关闭
     *
     * @param closeable 被关闭的对象
     */
    public static void close(Closeable... closeable) {
        if (closeable != null) {
            try {
                for (Closeable item : closeable) {
                    if (item != null) {
                        item.close();
                    }
                }
            } catch (IOException e) {
                // 静默关闭
            }
        }
    }

    /**
     * 从缓存中刷出数据
     *
     * @param flushable
     */
    public static void flush(Flushable flushable) {
        if (null != flushable) {
            try {
                flushable.flush();
            } catch (Exception e) {
                // 静默刷出
            }
        }
    }

    public static void serverResponse(HttpServletResponse response, String json) throws IOException {
        serverResponse(response, HttpStatus.OK.value(), json);
    }

    public static void serverResponse(HttpServletResponse response, int status, String json) throws IOException {
        Assert.isTrue(response != null, "response require not null.");
        response.setStatus(status);
        response.setCharacterEncoding(BaseConst.CHARSET_UTF8_VALUE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OutputStream writer = response.getOutputStream();
        writer.write(json.getBytes());
        writer.flush();
        writer.close();
    }
}