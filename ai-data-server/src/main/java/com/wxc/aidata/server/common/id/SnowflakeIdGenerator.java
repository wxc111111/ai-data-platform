package com.wxc.aidata.server.common.id;

import org.springframework.stereotype.Component;

/**
 * 雪花 ID 生成器，生成可写入 MySQL BIGINT 的全局趋势递增 ID。
 */
@Component
public class SnowflakeIdGenerator implements IdGenerator {

    /**
     * 自定义纪元：2026-01-01 00:00:00 UTC，减少时间戳位数浪费。
     */
    private static final long EPOCH = 1767225600000L;

    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 第一版单服务部署固定 workerId，后续多实例部署时应改为配置项或由部署平台分配。
     */
    private final long workerId = 1L;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    /**
     * 生成下一个雪花 ID；同步方法保证同一 JVM 内不会产生重复序列。
     */
    @Override
    public synchronized Long nextId() {
        long currentTimestamp = currentTimeMillis();
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("系统时钟回拨，无法生成雪花ID");
        }

        // 同一毫秒内递增序列，序列用尽时等待下一毫秒。
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | ((workerId & MAX_WORKER_ID) << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 获取当前毫秒时间，独立方法便于后续测试或扩展时替换时钟来源。
     */
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 等待进入下一毫秒，处理单毫秒内超过 4096 个 ID 的极端情况。
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }
}
