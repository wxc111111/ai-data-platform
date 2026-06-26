package com.wxc.aidata.server.common.id;

/**
 * ID 生成器，统一生成业务表主键。
 */
public interface IdGenerator {

    /**
     * 生成下一个 Long 类型主键。
     */
    Long nextId();
}
