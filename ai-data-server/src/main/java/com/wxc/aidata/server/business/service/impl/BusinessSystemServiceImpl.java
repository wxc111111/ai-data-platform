package com.wxc.aidata.server.business.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.business.entity.BizSystem;
import com.wxc.aidata.server.business.mapper.BusinessSystemMapper;
import com.wxc.aidata.server.business.model.BusinessSystemCreateCommand;
import com.wxc.aidata.server.business.model.BusinessSystemPageQuery;
import com.wxc.aidata.server.business.model.BusinessSystemUpdateCommand;
import com.wxc.aidata.server.business.response.BusinessSystemResponse;
import com.wxc.aidata.server.business.service.BusinessSystemService;
import com.wxc.aidata.server.common.id.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 业务系统管理服务实现，负责业务系统 CRUD、认证配置保存和基础字段校验。
 */
@Service
public class BusinessSystemServiceImpl implements BusinessSystemService {

    private static final int BUSINESS_SYSTEM_ERROR_CODE = 12001;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final Pattern SYSTEM_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final Set<String> SUPPORTED_AUTH_TYPES = Set.of("NONE", "API_KEY", "BEARER_TOKEN", "BASIC", "CUSTOM_HEADER");

    private final BusinessSystemMapper businessSystemMapper;
    private final IdGenerator idGenerator;

    /**
     * 注入业务系统 Mapper 和 ID 生成器。
     */
    public BusinessSystemServiceImpl(BusinessSystemMapper businessSystemMapper, IdGenerator idGenerator) {
        this.businessSystemMapper = businessSystemMapper;
        this.idGenerator = idGenerator;
    }

    /**
     * 分页查询业务系统列表，复杂条件查询使用 XML SQL，分页由 PageHelper 接管。
     */
    @Override
    public PageResult<BusinessSystemResponse> pageBusinessSystems(BusinessSystemPageQuery query) {
        BusinessSystemPageQuery safeQuery = query == null ? new BusinessSystemPageQuery(1, 10, null, null, null) : query;
        PageHelper.startPage(safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize());
        List<BizSystem> rows = businessSystemMapper.findBusinessSystems(safeQuery);
        PageInfo<BizSystem> pageInfo = new PageInfo<>(rows);
        List<BusinessSystemResponse> records = rows.stream().map(this::toResponse).toList();
        return PageResult.of(pageInfo.getTotal(), safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize(), records);
    }

    /**
     * 通过 MyBatis-Plus 主键查询获取业务系统详情。
     */
    @Override
    public BusinessSystemResponse getBusinessSystem(Long id) {
        validateId(id);
        BizSystem system = businessSystemMapper.selectById(id);
        if (system == null) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统不存在");
        }
        return toResponse(system);
    }

    /**
     * 创建业务系统，简单新增使用 MyBatis-Plus insert 方法。
     */
    @Override
    public void createBusinessSystem(BusinessSystemCreateCommand command) {
        validateCreateCommand(command);
        String systemCode = command.systemCode().trim();
        if (businessSystemMapper.existsBySystemCode(systemCode)) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "系统编码已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        BizSystem system = new BizSystem(
                idGenerator.nextId(),
                systemCode,
                command.systemName().trim(),
                command.baseUrl().trim(),
                normalizeAuthType(command.authType()),
                trimToNull(command.authConfig()),
                normalizeTimeout(command.connectTimeout(), DEFAULT_CONNECT_TIMEOUT, "连接超时时间必须大于 0"),
                normalizeTimeout(command.readTimeout(), DEFAULT_READ_TIMEOUT, "读取超时时间必须大于 0"),
                normalizeStatus(command.status()),
                trimToNull(command.description()),
                now,
                now
        );
        businessSystemMapper.insert(system);
    }

    /**
     * 更新业务系统，先校验记录存在和编码唯一性，再使用 MyBatis-Plus updateById。
     */
    @Override
    public void updateBusinessSystem(BusinessSystemUpdateCommand command) {
        if (command == null || command.id() == null) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统ID不能为空");
        }
        validateUpdateCommand(command);
        BizSystem existing = businessSystemMapper.selectById(command.id());
        if (existing == null) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统不存在");
        }

        String systemCode = command.systemCode().trim();
        if (businessSystemMapper.existsBySystemCodeExcludeId(systemCode, command.id())) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "系统编码已存在");
        }

        BizSystem system = new BizSystem(
                command.id(),
                systemCode,
                command.systemName().trim(),
                command.baseUrl().trim(),
                normalizeAuthType(command.authType()),
                trimToNull(command.authConfig()),
                normalizeTimeout(command.connectTimeout(), DEFAULT_CONNECT_TIMEOUT, "连接超时时间必须大于 0"),
                normalizeTimeout(command.readTimeout(), DEFAULT_READ_TIMEOUT, "读取超时时间必须大于 0"),
                normalizeStatus(command.status()),
                trimToNull(command.description()),
                existing.getCreatedTime(),
                LocalDateTime.now()
        );
        int updated = businessSystemMapper.updateById(system);
        if (updated == 0) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统不存在");
        }
    }

    /**
     * 启用或禁用业务系统，状态更新属于简单主键更新，使用 MyBatis-Plus updateById。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        validateId(id);
        BizSystem existing = businessSystemMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统不存在");
        }
        existing.setStatus(normalizeStatus(status));
        existing.setUpdatedTime(LocalDateTime.now());
        int updated = businessSystemMapper.updateById(existing);
        if (updated == 0) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统不存在");
        }
    }

    /**
     * 删除业务系统，第一版尚无业务接口引用，直接按主键删除。
     */
    @Override
    public void deleteBusinessSystem(Long id) {
        validateId(id);
        int deleted = businessSystemMapper.deleteById(id);
        if (deleted == 0) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统不存在");
        }
    }

    /**
     * 校验创建命令必填字段和字段格式。
     */
    private void validateCreateCommand(BusinessSystemCreateCommand command) {
        if (command == null) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统参数不能为空");
        }
        validateCommonFields(command.systemCode(), command.systemName(), command.baseUrl(), command.authType());
        normalizeTimeout(command.connectTimeout(), DEFAULT_CONNECT_TIMEOUT, "连接超时时间必须大于 0");
        normalizeTimeout(command.readTimeout(), DEFAULT_READ_TIMEOUT, "读取超时时间必须大于 0");
        normalizeStatus(command.status());
    }

    /**
     * 校验更新命令必填字段和字段格式。
     */
    private void validateUpdateCommand(BusinessSystemUpdateCommand command) {
        validateCommonFields(command.systemCode(), command.systemName(), command.baseUrl(), command.authType());
        normalizeTimeout(command.connectTimeout(), DEFAULT_CONNECT_TIMEOUT, "连接超时时间必须大于 0");
        normalizeTimeout(command.readTimeout(), DEFAULT_READ_TIMEOUT, "读取超时时间必须大于 0");
        normalizeStatus(command.status());
    }

    /**
     * 校验新增和编辑共享的业务字段。
     */
    private void validateCommonFields(String systemCode, String systemName, String baseUrl, String authType) {
        if (isBlank(systemCode)) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "系统编码不能为空");
        }
        if (!SYSTEM_CODE_PATTERN.matcher(systemCode.trim()).matches()) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "系统编码只允许字母、数字、下划线和短横线");
        }
        if (isBlank(systemName)) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "系统名称不能为空");
        }
        if (isBlank(baseUrl)) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "基础地址不能为空");
        }
        String normalizedBaseUrl = baseUrl.trim();
        if (!normalizedBaseUrl.startsWith("http://") && !normalizedBaseUrl.startsWith("https://")) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "基础地址必须以 http:// 或 https:// 开头");
        }
        normalizeAuthType(authType);
    }

    /**
     * 认证方式为空时默认无认证，其余值必须属于系统支持范围。
     */
    private String normalizeAuthType(String authType) {
        String normalized = isBlank(authType) ? "NONE" : authType.trim().toUpperCase();
        if (!SUPPORTED_AUTH_TYPES.contains(normalized)) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "认证类型不合法");
        }
        return normalized;
    }

    /**
     * 超时时间为空时使用默认值，非空时必须为正数。
     */
    private Integer normalizeTimeout(Integer timeout, Integer defaultValue, String message) {
        if (timeout == null) {
            return defaultValue;
        }
        if (timeout <= 0) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, message);
        }
        return timeout;
    }

    /**
     * 状态为空时默认启用，只允许启用或禁用。
     */
    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统状态不合法");
        }
        return status;
    }

    /**
     * 校验主键不能为空。
     */
    private void validateId(Long id) {
        if (id == null) {
            throw new BusinessException(BUSINESS_SYSTEM_ERROR_CODE, "业务系统ID不能为空");
        }
    }

    /**
     * 空白字符串统一转为 null，减少无意义空字符串入库。
     */
    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 判断字符串是否为空白。
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 将业务系统实体转换成接口响应对象。
     */
    private BusinessSystemResponse toResponse(BizSystem system) {
        return new BusinessSystemResponse(
                system.getId(),
                system.getSystemCode(),
                system.getSystemName(),
                system.getBaseUrl(),
                system.getAuthType(),
                system.getAuthConfig(),
                system.getConnectTimeout(),
                system.getReadTimeout(),
                system.getStatus(),
                system.getDescription(),
                system.getCreatedTime(),
                system.getUpdatedTime()
        );
    }
}
