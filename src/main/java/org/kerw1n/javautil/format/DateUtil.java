package org.kerw1n.javautil.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期格式化工具类.
 *
 * @author : kerw1n
 **/
public class DateUtil {

    /**
     * 1分钟的秒数
     */
    public static final int MINUTE_SECONDS = 60;
    /**
     * 1小时的秒数
     */
    public static final int HOUR_SECONDS = MINUTE_SECONDS * 60;
    /**
     * 1天的秒数
     */
    public static final int DAY_SECONDS = HOUR_SECONDS * 24;

    /**
     * 1秒的毫秒数
     */
    public static final long SECONDS_MILLS = 1000;

    private DateUtil() {
    }

    /**
     * 获取当前日期.
     *
     * @return
     */
    public static Date getCurrentDate() {
        return Calendar.getInstance(Locale.CHINA).getTime();
    }

    /**
     * 日期格式化字符串.
     *
     * @param date   日期
     * @param format 格式
     * @return
     */
    public static String formatDate(Date date, String format) {
        return SimpleDateFormatFactory.getInstance(format).format(date);
    }

    /**
     * 字符串转日期.
     *
     * @param dateStr 日期字符串
     * @param format  格式
     * @return
     */
    public static Date parseDate(String dateStr, String format) throws ParseException {
        return SimpleDateFormatFactory.getInstance(format).parse(dateStr);
    }

    /**
     * 获取指定日期当月的月初日期
     *
     * @param date
     * @return
     */
    public static Date getFirstDateOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getBeginDate(calendar.getTime());
    }

    /**
     * 获取指定日期当月的月末日期
     *
     * @param date
     * @return
     */
    public static Date getLastDateOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return getEndDate(calendar.getTime());
    }

    /**
     * 获取指定日期当天开始的日期(00:00:00.0)
     *
     * @param date
     * @return
     */
    public static Date getBeginDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取指定日期当天最后的日期(23:59:59.999)
     *
     * @param date
     * @return
     */
    public static Date getEndDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * 获取日期间隔秒数
     *
     * @param begin
     * @param end
     * @return 间隔秒数
     */
    public static long getIntervalSeconds(Date begin, Date end) {
        if (begin == null || end == null) {
            return 0L;
        }
        return (begin.getTime() - end.getTime()) / 1000;
    }

    /**
     * 获取日期间隔分钟
     *
     * @param begin
     * @param end
     * @return 间隔分钟
     */
    public static long getIntervalMinutes(Date begin, Date end) {
        return getIntervalSeconds(begin, end) / MINUTE_SECONDS;
    }

    /**
     * 获取日期间隔小时
     *
     * @param begin
     * @param end
     * @return 间隔小时
     */
    public static long getIntervalHour(Date begin, Date end) {
        return getIntervalSeconds(begin, end) / HOUR_SECONDS;
    }

    /**
     * 获取日期间隔天数
     *
     * @param begin
     * @param end
     * @return 间隔天数
     */
    public static long getIntervalDays(Date begin, Date end) {
        return getIntervalSeconds(begin, end) / DAY_SECONDS;
    }

    /**
     * 获取n秒前/后的日期
     *
     * @param date
     * @param second 秒数;如需按分钟数获取,转换为秒即可
     * @return
     */
    public static Date rollSecond(Date date, int second) {
        return rollByFiled(date, Calendar.SECOND, second);
    }

    /**
     * 获取n小时前/后的日期
     *
     * @param date
     * @param hour
     * @return
     */
    public static Date rollHour(Date date, int hour) {
        return rollByFiled(date, Calendar.HOUR_OF_DAY, hour);
    }

    /**
     * 获取n天前/后的日期
     *
     * @param date
     * @param day
     * @return
     */
    public static Date rollDay(Date date, int day) {
        return rollByFiled(date, Calendar.DAY_OF_MONTH, day);
    }

    /**
     * 获取n月前/后的日期
     *
     * @param date
     * @param month
     * @return
     */
    public static Date rollMonth(Date date, int month) {
        return rollByFiled(date, Calendar.MONTH, month);
    }

    /**
     * 获取n年前/后的日期
     *
     * @param date
     * @param year
     * @return
     */
    public static Date rollYear(Date date, int year) {
        return rollByFiled(date, Calendar.YEAR, year);
    }

    /**
     * 处理日期字段值
     *
     * @param date   日期
     * @param field  Calendar 字段
     * @param amount 字段要添加的值
     * @return
     */
    private static Date rollByFiled(Date date, int field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);
        return cal.getTime();
    }

    /**
     * 比较日期
     *
     * @param date1 日期字符串,格式{@link Format#FORMAT_03}
     * @param date2
     * @return 1:date1>date2,-1:date1<date2,0:date1==date2
     * @throws ParseException 日期解析异常
     */
    public static int compareDateStr(String date1, String date2) throws ParseException {
        return compareDateStr(date1, date2, Format.FORMAT_03);
    }

    /**
     * 比较日期
     *
     * @param date1  日期字符串
     * @param date2
     * @param format 字符串格式
     * @return 1:date1>date2,-1:date1<date2,0:date1==date2
     * @throws ParseException 日期解析异常
     */
    public static int compareDateStr(String date1, String date2, String format) throws ParseException {
        SimpleDateFormat sdf = SimpleDateFormatFactory.getInstance(format);
        Date d1 = sdf.parse(date1), d2 = sdf.parse(date2);
        return d1.after(d2) ? 1 : (d1.before(d2) ? -1 : 0);
    }

    /**
     * 枚举常用日期格式.
     *
     * @author kerw1n
     */
    public static class Format {
        public static final String FORMAT_01 = "yyyy-MM";
        public static final String FORMAT_02 = "yyyy-MM-dd";
        public static final String FORMAT_03 = "yyyy-MM-dd HH:mm:ss";
        public static final String FORMAT_04 = "yyyyMMdd";
        public static final String FORMAT_05 = "HH:mm:ss";
        public static final String FORMAT_06 = "HHmmss";
        public static final String FORMAT_07 = "yyyyMMddHHmmss";
        public static final String FORMAT_08 = "yyyyMMddHHmmssSSS";
        public static final String FORMAT_09 = "yyyy年MM月dd日 HH时mm分ss秒";

        public static final String CRON_01 = "ss mm HH dd MM ? yyyy";
        public static final String CRON_02 = "ss mm HH dd MM ?";
    }

}
