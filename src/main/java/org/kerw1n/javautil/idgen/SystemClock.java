package org.kerw1n.javautil.idgen;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 系统时钟
 * <p>
 * 高并发场景下 System.currentTimeMillis() 的性能问题的优化
 * 后台定时更新时钟，JVM退出时，线程自动回收
 *
 * @author kerw1n
 */
public class SystemClock {

    /**
     * 时钟更新间隔 ms
     */
    private final long period;
    /**
     * 现在时刻的毫秒数
     */
    private volatile long now;

    /**
     * 构造函数
     *
     * @param period 时钟更新间隔 ms
     */
    public SystemClock(long period) {
        this.period = period;
        this.now = System.currentTimeMillis();
        scheduleClockUpdating();
    }

    /**
     * 开启计时器线程
     */
    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "System Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> now = System.currentTimeMillis(), period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 当前时间毫秒数
     *
     * @return
     */
    private long currentTimeMillis() {
        return now;
    }

    /**
     * 单例
     *
     * @author Looly
     */
    private static class InstanceHolder {
        public static final SystemClock INSTANCE = new SystemClock(1);
    }

    /**
     * 单例实例
     *
     * @return
     */
    private static SystemClock getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 当前时间
     *
     * @return
     */
    public static long now() {
        return getInstance().currentTimeMillis();
    }

    /**
     * 当前时间字符串表现形式
     *
     * @return
     */
    public static String nowDate() {
        return new Timestamp(getInstance().currentTimeMillis()).toString();
    }
}
