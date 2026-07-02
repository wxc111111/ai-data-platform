package com.wxc.aidata.server.skill.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.business.model.BusinessApiTestCommand;
import com.wxc.aidata.server.business.response.BusinessApiResponse;
import com.wxc.aidata.server.business.response.BusinessApiTestResponse;
import com.wxc.aidata.server.business.service.BusinessApiService;
import com.wxc.aidata.server.common.id.IdGenerator;
import com.wxc.aidata.server.permission.service.CurrentUserAccessService;
import com.wxc.aidata.server.skill.entity.AiSkill;
import com.wxc.aidata.server.skill.entity.AiSkillParameter;
import com.wxc.aidata.server.skill.mapper.SkillMapper;
import com.wxc.aidata.server.skill.mapper.SkillParameterMapper;
import com.wxc.aidata.server.skill.model.SkillCreateCommand;
import com.wxc.aidata.server.skill.model.SkillPageQuery;
import com.wxc.aidata.server.skill.model.SkillParameterCommand;
import com.wxc.aidata.server.skill.model.SkillTestCommand;
import com.wxc.aidata.server.skill.model.SkillUpdateCommand;
import com.wxc.aidata.server.skill.response.SkillParameterResponse;
import com.wxc.aidata.server.skill.response.SkillResponse;
import com.wxc.aidata.server.skill.service.SkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Skill 管理服务实现，第一版按“一个 Skill 对应一个业务接口”管理和测试。
 */
@Service
public class SkillServiceImpl implements SkillService {

    private static final int SKILL_ERROR_CODE = 3001;
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final Set<String> SUPPORTED_TYPES = Set.of("STRING", "INTEGER", "LONG", "DECIMAL", "BOOLEAN", "DATE", "DATETIME", "ARRAY", "OBJECT");
    private static final Set<String> SUPPORTED_VALUE_SOURCES = Set.of("CALLER", "CONSTANT");
    private static final Set<String> SUPPORTED_VISIBILITIES = Set.of("PRIVATE", "PUBLIC");

    private final SkillMapper skillMapper;
    private final SkillParameterMapper parameterMapper;
    private final BusinessApiService businessApiService;
    private final IdGenerator idGenerator;
    private final CurrentUserAccessService currentUserAccessService;

    /**
     * 注入 Skill 数据访问、业务接口服务和 ID 生成器。
     */
    public SkillServiceImpl(SkillMapper skillMapper, SkillParameterMapper parameterMapper,
                            BusinessApiService businessApiService, IdGenerator idGenerator,
                            CurrentUserAccessService currentUserAccessService) {
        this.skillMapper = skillMapper;
        this.parameterMapper = parameterMapper;
        this.businessApiService = businessApiService;
        this.idGenerator = idGenerator;
        this.currentUserAccessService = currentUserAccessService;
    }

    /**
     * 分页查询 Skill 列表，参数映射只在详情中返回。
     */
    @Override
    public PageResult<SkillResponse> pageSkills(SkillPageQuery query) {
        SkillPageQuery safeQuery = query == null ? new SkillPageQuery(1, 10, null, null, null) : query;
        SkillPageQuery scopedQuery = safeQuery.withAccessScope(currentUserAccessService.currentRoleIds(), currentUserAccessService.currentUserIsAdmin());
        PageHelper.startPage(scopedQuery.normalizedPageNo(), scopedQuery.normalizedPageSize());
        List<SkillMapper.SkillRow> rows = skillMapper.findSkills(scopedQuery);
        PageInfo<SkillMapper.SkillRow> pageInfo = new PageInfo<>(rows);
        List<SkillResponse> records = rows.stream().map(this::toListResponse).toList();
        return PageResult.of(pageInfo.getTotal(), scopedQuery.normalizedPageNo(), scopedQuery.normalizedPageSize(), records);
    }

    /**
     * 查询 Skill 详情，并补充关联业务接口信息。
     */
    @Override
    public SkillResponse getSkill(Long id) {
        AiSkill skill = getAccessibleSkillOrThrow(id);
        BusinessApiResponse api = businessApiService.getBusinessApi(skill.getApiId());
        return toResponse(skill, api, parameterMapper.findBySkillId(skill.getId()));
    }

    /**
     * 创建 Skill 前校验编码唯一性和关联业务接口有效性。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSkill(SkillCreateCommand command) {
        validateCreateCommand(command);
        businessApiService.getBusinessApi(command.apiId());
        String skillCode = command.skillCode().trim();
        if (skillMapper.existsBySkillCode(skillCode)) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 编码已存在");
        }
        Long skillId = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUserAccessService.currentUserId();
        skillMapper.insert(toEntity(
                skillId,
                skillCode,
                command.skillName(),
                command.description(),
                command.apiId(),
                command.permissionCode(),
                command.visibility(),
                command.timeoutMs(),
                command.maxResultCount(),
                command.status(),
                1,
                userId,
                now,
                userId,
                now
        ));
        saveRoles(skillId, command.roleIds());
        saveParameters(skillId, command.parameters(), now);
    }

    /**
     * 更新 Skill 时同步覆盖参数映射，并递增版本号。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkill(SkillUpdateCommand command) {
        if (command == null || command.id() == null) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill ID 不能为空");
        }
        validateUpdateCommand(command);
        AiSkill existing = getAccessibleSkillOrThrow(command.id());
        businessApiService.getBusinessApi(command.apiId());
        String skillCode = command.skillCode().trim();
        if (skillMapper.existsBySkillCodeExcludeId(skillCode, command.id())) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 编码已存在");
        }
        skillMapper.updateById(toEntity(
                command.id(),
                skillCode,
                command.skillName(),
                command.description(),
                command.apiId(),
                command.permissionCode(),
                command.visibility(),
                command.timeoutMs(),
                command.maxResultCount(),
                command.status(),
                existing.getVersionNo() + 1,
                existing.getCreatedBy(),
                existing.getCreatedTime(),
                currentUserAccessService.currentUserId(),
                LocalDateTime.now()
        ));
        saveRoles(command.id(), command.roleIds());
        parameterMapper.deleteBySkillId(command.id());
        saveParameters(command.id(), command.parameters(), LocalDateTime.now());
    }

    /**
     * 只更新 Skill 状态，不影响版本号和参数映射。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        AiSkill skill = getAccessibleSkillOrThrow(id);
        skill.setStatus(normalizeStatus(status));
        skill.setUpdatedBy(currentUserAccessService.currentUserId());
        skill.setUpdatedTime(LocalDateTime.now());
        skillMapper.updateById(skill);
    }

    /**
     * 删除 Skill 前先清理参数映射。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long id) {
        getAccessibleSkillOrThrow(id);
        skillMapper.deleteRolesBySkillId(id);
        parameterMapper.deleteBySkillId(id);
        int deleted = skillMapper.deleteById(id);
        if (deleted == 0) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 不存在");
        }
    }

    /**
     * 将 Skill 参数值映射成业务接口参数值，然后复用业务接口在线测试能力。
     */
    @Override
    public BusinessApiTestResponse testSkill(Long id, SkillTestCommand command) {
        AiSkill skill = getAccessibleSkillOrThrow(id);
        if (!Integer.valueOf(1).equals(skill.getStatus())) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 已禁用");
        }
        List<AiSkillParameter> parameters = parameterMapper.findBySkillId(skill.getId());
        Map<String, Object> values = command == null || command.parameterValues() == null ? Map.of() : command.parameterValues();
        Map<String, Object> apiValues = new LinkedHashMap<>();
        for (AiSkillParameter parameter : parameters) {
            Object value = resolveSkillParameter(parameter, values);
            if (value != null && !String.valueOf(value).isBlank()) {
                apiValues.put(parameter.getApiParameterName(), value);
            }
        }
        return businessApiService.testBusinessApi(skill.getApiId(), new BusinessApiTestCommand(apiValues));
    }

    /**
     * 保存参数映射，先排序再校验，保证列表展示和执行顺序稳定。
     */
    private void saveParameters(Long skillId, List<SkillParameterCommand> parameters, LocalDateTime now) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        List<SkillParameterCommand> sorted = parameters.stream()
                .sorted(Comparator.comparing(item -> item.sortNo() == null ? 0 : item.sortNo()))
                .toList();
        for (SkillParameterCommand parameter : sorted) {
            validateParameter(parameter);
            parameterMapper.insert(new AiSkillParameter(
                    idGenerator.nextId(),
                    skillId,
                    parameter.parameterName().trim(),
                    normalizeType(parameter.parameterType()),
                    normalizeRequired(parameter.required()),
                    trimToNull(parameter.description()),
                    parameter.apiParameterName().trim(),
                    trimToNull(parameter.defaultValue()),
                    normalizeValueSource(parameter.valueSource()),
                    null,
                    parameter.sortNo() == null ? 0 : parameter.sortNo(),
                    now,
                    now
            ));
        }
    }

    /**
     * 解析 Skill 参数值，CALLER 来源使用调用方输入，CONSTANT 来源使用默认值。
     */
    private Object resolveSkillParameter(AiSkillParameter parameter, Map<String, Object> values) {
        Object value = "CONSTANT".equals(parameter.getValueSource()) ? parameter.getDefaultValue() : values.get(parameter.getParameterName());
        if ((value == null || String.valueOf(value).isBlank()) && parameter.getDefaultValue() != null) {
            value = parameter.getDefaultValue();
        }
        if ((value == null || String.valueOf(value).isBlank()) && Integer.valueOf(1).equals(parameter.getRequired())) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 参数 " + parameter.getParameterName() + " 不能为空");
        }
        return value;
    }

    /**
     * 查询 Skill，不存在时抛出业务异常。
     */
    private AiSkill getSkillOrThrow(Long id) {
        validateId(id);
        AiSkill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 不存在");
        }
        return skill;
    }

    /**
     * 查询 Skill 并校验当前用户角色范围，避免绕过列表直接访问。
     */
    private AiSkill getAccessibleSkillOrThrow(Long id) {
        AiSkill skill = getSkillOrThrow(id);
        if (!currentUserAccessService.canAccessResource(skillMapper.findRoleIdsBySkillId(id))) {
            throw new BusinessException(SKILL_ERROR_CODE, "无权访问该 Skill");
        }
        return skill;
    }

    /**
     * 校验新增命令。
     */
    private void validateCreateCommand(SkillCreateCommand command) {
        if (command == null) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 参数不能为空");
        }
        validateCommonFields(command.skillCode(), command.skillName(), command.description(), command.apiId(), command.status());
    }

    /**
     * 校验更新命令。
     */
    private void validateUpdateCommand(SkillUpdateCommand command) {
        validateCommonFields(command.skillCode(), command.skillName(), command.description(), command.apiId(), command.status());
    }

    /**
     * 校验 Skill 基础字段。
     */
    private void validateCommonFields(String skillCode, String skillName, String description, Long apiId, Integer status) {
        if (isBlank(skillCode) || !CODE_PATTERN.matcher(skillCode.trim()).matches()) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 编码只能包含字母、数字、下划线和短横线");
        }
        if (isBlank(skillName)) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 名称不能为空");
        }
        if (isBlank(description)) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 能力说明不能为空");
        }
        if (apiId == null) {
            throw new BusinessException(SKILL_ERROR_CODE, "关联业务接口不能为空");
        }
        normalizeStatus(status);
    }

    /**
     * 校验参数映射字段。
     */
    private void validateParameter(SkillParameterCommand parameter) {
        if (parameter == null || isBlank(parameter.parameterName())) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 参数名称不能为空");
        }
        if (isBlank(parameter.apiParameterName())) {
            throw new BusinessException(SKILL_ERROR_CODE, "业务接口参数名称不能为空");
        }
        normalizeType(parameter.parameterType());
        normalizeRequired(parameter.required());
        normalizeValueSource(parameter.valueSource());
    }

    /**
     * 构造 Skill 实体。
     */
    private AiSkill toEntity(Long id, String skillCode, String skillName, String description, Long apiId,
                             String permissionCode, String visibility, Integer timeoutMs, Integer maxResultCount, Integer status,
                             Integer versionNo, Long createdBy, LocalDateTime createdTime, Long updatedBy, LocalDateTime updatedTime) {
        return new AiSkill(
                id,
                skillCode,
                skillName.trim(),
                description.trim(),
                apiId,
                trimToNull(permissionCode),
                normalizeVisibility(visibility),
                timeoutMs == null || timeoutMs < 1 ? 10000 : timeoutMs,
                maxResultCount == null || maxResultCount < 1 ? 100 : maxResultCount,
                normalizeStatus(status),
                versionNo,
                createdBy,
                createdTime,
                updatedBy,
                updatedTime
        );
    }

    /**
     * 覆盖保存 Skill 角色范围，空角色列表表示仅 admin 可见。
     */
    private void saveRoles(Long skillId, List<Long> roleIds) {
        skillMapper.deleteRolesBySkillId(skillId);
        List<Long> distinctRoleIds = distinctIds(roleIds);
        if (!distinctRoleIds.isEmpty()) {
            skillMapper.insertSkillRoles(skillId, distinctRoleIds);
        }
    }

    /**
     * 去重并过滤空角色 ID。
     */
    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(ids.stream().filter(id -> id != null).toList()));
    }

    /**
     * 将列表行转换为响应。
     */
    private SkillResponse toListResponse(SkillMapper.SkillRow row) {
        return new SkillResponse(row.id(), row.skillCode(), row.skillName(), row.description(), row.apiId(), row.apiName(),
                row.apiCode(), row.permissionCode(), row.visibility(), row.timeoutMs(), row.maxResultCount(), row.status(), row.versionNo(),
                row.createdBy(), row.createdTime(), row.updatedBy(), row.updatedTime(), skillMapper.findRoleIdsBySkillId(row.id()), List.of());
    }

    /**
     * 将详情实体转换为响应。
     */
    private SkillResponse toResponse(AiSkill skill, BusinessApiResponse api, List<AiSkillParameter> parameters) {
        return new SkillResponse(skill.getId(), skill.getSkillCode(), skill.getSkillName(), skill.getDescription(),
                skill.getApiId(), api.apiName(), api.apiCode(), skill.getPermissionCode(), skill.getVisibility(), skill.getTimeoutMs(),
                skill.getMaxResultCount(), skill.getStatus(), skill.getVersionNo(), skill.getCreatedBy(), skill.getCreatedTime(),
                skill.getUpdatedBy(), skill.getUpdatedTime(), skillMapper.findRoleIdsBySkillId(skill.getId()),
                parameters.stream().map(this::toParameterResponse).toList());
    }

    private SkillParameterResponse toParameterResponse(AiSkillParameter parameter) {
        return new SkillParameterResponse(parameter.getId(), parameter.getSkillId(), parameter.getParameterName(),
                parameter.getParameterType(), parameter.getRequired(), parameter.getDescription(), parameter.getApiParameterName(),
                parameter.getDefaultValue(), parameter.getValueSource(), parameter.getSortNo(), parameter.getCreatedTime(),
                parameter.getUpdatedTime());
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new BusinessException(SKILL_ERROR_CODE, "ID 不能为空");
        }
    }

    private Integer normalizeStatus(Integer status) {
        return Integer.valueOf(0).equals(status) ? 0 : 1;
    }

    private Integer normalizeRequired(Integer required) {
        return Integer.valueOf(1).equals(required) ? 1 : 0;
    }

    private String normalizeType(String type) {
        if (isBlank(type)) {
            throw new BusinessException(SKILL_ERROR_CODE, "参数类型不能为空");
        }
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_TYPES.contains(normalized)) {
            throw new BusinessException(SKILL_ERROR_CODE, "参数类型不合法");
        }
        return normalized;
    }

    private String normalizeValueSource(String valueSource) {
        String normalized = isBlank(valueSource) ? "CALLER" : valueSource.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_VALUE_SOURCES.contains(normalized)) {
            throw new BusinessException(SKILL_ERROR_CODE, "参数值来源仅支持 CALLER 或 CONSTANT");
        }
        return normalized;
    }

    /**
     * 规范化 Skill 类型，空值默认私有，避免兼容旧请求时暴露给公共范围。
     */
    private String normalizeVisibility(String visibility) {
        String normalized = isBlank(visibility) ? "PRIVATE" : visibility.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_VISIBILITIES.contains(normalized)) {
            throw new BusinessException(SKILL_ERROR_CODE, "Skill 类型仅支持 PRIVATE 或 PUBLIC");
        }
        return normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
