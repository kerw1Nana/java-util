package org.kerw1n.javautil.idgen;

import org.kerw1n.javautil.format.MessageFormatter;

import java.io.Serializable;

/**
 * Twitter 的 Snowflake 算法
 *
 * @author kerw1n
 * @see "http://www.cnblogs.com/relucent/p/4955340.html"
 */
public class Snowflake implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 开始时间(2015-01-01)
     */
    private final long twepoch = 1420041600000L;
    private final long workerIdBits = 5L;
    private final long dataCenterIdBits = 5L;
    /**
     * 最大支持机器节点数 0~31，一共 32 个
     */
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    /**
     * 最大支持数据中心节点数 0~31，一共 32 个
     */
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);
    /**
     * 序列号 12 位
     */
    private final long sequenceBits = 12L;
    /**
     * 机器节点左移 12 位
     */
    private final long workerIdShift = sequenceBits;
    /**
     * 数据中心节点左移 17 位
     */
    private final long dataCenterIdShift = sequenceBits + workerIdBits;
    /**
     * 时间毫秒数左移 22 位
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    /**
     * 序列掩码 4095
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 单机部署可固定
     */
    private final long workerId = 1;
    private final long dataCenterId = 1;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private Snowflake() {
    }

    /**
     * 获取默认实例
     *
     * @return
     */
    public static Snowflake getInstance() {
        return InstanceHolder.INSTANCE;
    }

    static class InstanceHolder {
        static final Snowflake INSTANCE = new Snowflake();
    }

    /*private Snowflake(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(MessageFormatter.format("worker Id can't be greater than {} or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(MessageFormatter.format("datacenter Id can't be greater than {} or less than 0", maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }*/

    /**
     * 根据Snowflake的ID，获取机器id
     *
     * @param id snowflake算法生成的id
     * @return 所属机器的id
     */
    public long getWorkerId(long id) {
        return id >> workerIdShift & ~(-1L << workerIdBits);
    }

    /**
     * 根据Snowflake的ID，获取数据中心id
     *
     * @param id snowflake算法生成的id
     * @return 所属数据中心
     */
    public long getDataCenterId(long id) {
        return id >> dataCenterIdShift & ~(-1L << dataCenterIdBits);
    }

    /**
     * 根据Snowflake的ID，获取生成时间
     *
     * @param id snowflake算法生成的id
     * @return 生成的时间
     */
    public long getGenerateDateTime(long id) {
        return (id >> timestampLeftShift & ~(-1L << 41L)) + twepoch;
    }

    /**
     * 获得下一个 ID
     *
     * @return ID
     */
    public synchronized long nextId() {
        long timestamp = genTime();
        if (timestamp < lastTimestamp) {
            if (lastTimestamp - timestamp < 2000) {
                // 容忍2秒内的回拨，避免NTP校时造成的异常
                timestamp = lastTimestamp;
            } else {
                // 如果服务器时间有问题(时钟后退) 报错。
                throw new IllegalStateException(MessageFormatter.format("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp));
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (dataCenterId << dataCenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    /**
     * 下一个ID（字符串形式）
     *
     * @return ID 字符串形式
     */
    public String nextIdStr() {
        return Long.toString(nextId());
    }


    /**
     * 循环等待下一个时间
     *
     * @param lastTimestamp 上次记录的时间
     * @return 下一个时间
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = genTime();
        // 循环直到操作系统时间戳变化
        while (timestamp == lastTimestamp) {
            timestamp = genTime();
        }
        if (timestamp < lastTimestamp) {
            // 如果发现新的时间戳比上次记录的时间戳数值小，说明操作系统时间发生了倒退，报错
            throw new IllegalStateException(
                    MessageFormatter.format("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp));
        }
        return timestamp;
    }

    /**
     * 生成时间戳
     *
     * @return 时间戳
     */
    private long genTime() {
        return System.currentTimeMillis();
    }
}
