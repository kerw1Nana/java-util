package org.kerw1n.javautil.format;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * SimpleDateFormat 工厂.
 *
 * @author : kerw1n
 **/
public class SimpleDateFormatFactory {

    private static final Map<String, ThreadLocal<SimpleDateFormat>> FORMAT_MAP = new HashMap<>();

    public static SimpleDateFormat getInstance() {
        return getInstance(DateUtil.Format.FORMAT_03);
    }

    public static SimpleDateFormat getInstance(String format) {
        ThreadLocal<SimpleDateFormat> sdf = FORMAT_MAP.get(format);
        if (null == sdf) {
            sdf = new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat(format);
                }
            };
            FORMAT_MAP.put(format, sdf);
        }
        return sdf.get();
    }
}
