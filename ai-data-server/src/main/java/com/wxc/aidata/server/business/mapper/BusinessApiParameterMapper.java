package com.wxc.aidata.server.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxc.aidata.server.business.entity.BizApiParameter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 业务接口参数数据库访问接口，负责参数列表查询和按接口清理参数。
 */
@Mapper
public interface BusinessApiParameterMapper extends BaseMapper<BizApiParameter> {

    /**
     * 查询指定业务接口的参数定义。
     */
    List<BizApiParameter> findByApiId(Long apiId);

    /**
     * 删除指定业务接口的全部参数定义。
     */
    void deleteByApiId(Long apiId);
}
