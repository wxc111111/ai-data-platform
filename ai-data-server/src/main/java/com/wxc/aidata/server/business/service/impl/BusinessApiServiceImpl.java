package com.wxc.aidata.server.business.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.business.entity.BizApi;
import com.wxc.aidata.server.business.entity.BizApiParameter;
import com.wxc.aidata.server.business.entity.BizSystem;
import com.wxc.aidata.server.business.mapper.BusinessApiMapper;
import com.wxc.aidata.server.business.mapper.BusinessApiParameterMapper;
import com.wxc.aidata.server.business.mapper.BusinessSystemMapper;
import com.wxc.aidata.server.business.model.BusinessApiCreateCommand;
import com.wxc.aidata.server.business.model.BusinessApiPageQuery;
import com.wxc.aidata.server.business.model.BusinessApiParameterCommand;
import com.wxc.aidata.server.business.model.BusinessApiTestCommand;
import com.wxc.aidata.server.business.model.BusinessApiUpdateCommand;
import com.wxc.aidata.server.business.response.BusinessApiParameterResponse;
import com.wxc.aidata.server.business.response.BusinessApiResponse;
import com.wxc.aidata.server.business.response.BusinessApiTestResponse;
import com.wxc.aidata.server.business.service.BusinessApiHttpClient;
import com.wxc.aidata.server.business.service.BusinessApiHttpRequest;
import com.wxc.aidata.server.business.service.BusinessApiHttpResponse;
import com.wxc.aidata.server.business.service.BusinessApiService;
import com.wxc.aidata.server.common.id.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 业务接口管理服务实现，负责接口 CRUD、参数维护和在线测试请求组装。
 */
@Service
public class BusinessApiServiceImpl implements BusinessApiService {

    private static final int BUSINESS_API_ERROR_CODE = 12002;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final Set<String> SUPPORTED_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH");
    private static final Set<String> SUPPORTED_LOCATIONS = Set.of("PATH", "QUERY", "HEADER", "BODY");
    private static final Set<String> SUPPORTED_TYPES = Set.of("STRING", "INTEGER", "LONG", "DECIMAL", "BOOLEAN", "DATE", "DATETIME", "ARRAY", "OBJECT");

    private final BusinessApiMapper businessApiMapper;
    private final BusinessApiParameterMapper parameterMapper;
    private final BusinessSystemMapper businessSystemMapper;
    private final IdGenerator idGenerator;
    private final BusinessApiHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 注入业务接口、参数、业务系统、ID 和 HTTP 客户端组件。
     */
    public BusinessApiServiceImpl(
            BusinessApiMapper businessApiMapper,
            BusinessApiParameterMapper parameterMapper,
            BusinessSystemMapper businessSystemMapper,
            IdGenerator idGenerator,
            BusinessApiHttpClient httpClient) {

        this.businessApiMapper = businessApiMapper;
        this.parameterMapper = parameterMapper;
        this.businessSystemMapper = businessSystemMapper;
        this.idGenerator = idGenerator;
        this.httpClient = httpClient;
    }

    /**
     * 分页查询业务接口列表，参数定义只在详情中返回。
     */
    @Override
    public PageResult<BusinessApiResponse> pageBusinessApis(BusinessApiPageQuery query) {
        BusinessApiPageQuery safeQuery = query == null ? new BusinessApiPageQuery(1, 10, null, null, null, null) : query;
        PageHelper.startPage(safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize());
        List<BusinessApiMapper.BusinessApiRow> rows = businessApiMapper.findBusinessApis(safeQuery);
        PageInfo<BusinessApiMapper.BusinessApiRow> pageInfo = new PageInfo<>(rows);
        List<BusinessApiResponse> records = rows.stream().map(this::toListResponse).toList();
        return PageResult.of(pageInfo.getTotal(), safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize(), records);
    }

    /**
     * 查询业务接口详情并携带参数定义。
     */
    @Override
    public BusinessApiResponse getBusinessApi(Long id) {
        BizApi api = getApiOrThrow(id);
        BizSystem system = getSystemByIdOrThrow(api.getSystemId());
        return toResponse(api, system, parameterMapper.findByApiId(api.getId()));
    }

    /**
     * 创建业务接口和参数定义。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBusinessApi(BusinessApiCreateCommand command) {
        validateCreateCommand(command);
        BizSystem system = getSystemOrThrow(command.systemId());
        String apiCode = command.apiCode().trim();
        if (businessApiMapper.existsByApiCode(apiCode)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "接口编码已存在");
        }

        Long apiId = idGenerator.nextId();
        LocalDateTime now = LocalDateTime.now();
        businessApiMapper.insert(toEntity(
                apiId,
                command.systemId(),
                apiCode,
                command.apiName(),
                command.requestPath(),
                command.requestMethod(),
                command.contentType(),
                command.connectTimeout(),
                command.readTimeout(),
                command.responseDataPath(),
                command.status(),
                command.description(),
                now,
                now
        ));
        saveParameters(apiId, command.parameters(), now);
    }

    /**
     * 更新业务接口和参数定义，参数采用整体覆盖方式保存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBusinessApi(BusinessApiUpdateCommand command) {
        if (command == null || command.id() == null) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口ID不能为空");
        }
        validateUpdateCommand(command);
        BizApi existing = getApiOrThrow(command.id());
        getSystemOrThrow(command.systemId());
        String apiCode = command.apiCode().trim();
        if (businessApiMapper.existsByApiCodeExcludeId(apiCode, command.id())) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "接口编码已存在");
        }

        BizApi api = toEntity(
                command.id(),
                command.systemId(),
                apiCode,
                command.apiName(),
                command.requestPath(),
                command.requestMethod(),
                command.contentType(),
                command.connectTimeout(),
                command.readTimeout(),
                command.responseDataPath(),
                command.status(),
                command.description(),
                existing.getCreatedTime(),
                LocalDateTime.now()
        );
        int updated = businessApiMapper.updateById(api);
        if (updated == 0) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口不存在");
        }
        parameterMapper.deleteByApiId(command.id());
        saveParameters(command.id(), command.parameters(), LocalDateTime.now());
    }

    /**
     * 启用或禁用业务接口。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        BizApi existing = getApiOrThrow(id);
        existing.setStatus(normalizeStatus(status));
        existing.setUpdatedTime(LocalDateTime.now());
        int updated = businessApiMapper.updateById(existing);
        if (updated == 0) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口不存在");
        }
    }

    /**
     * 删除业务接口及其参数定义。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBusinessApi(Long id) {
        validateId(id);
        parameterMapper.deleteByApiId(id);
        int deleted = businessApiMapper.deleteById(id);
        if (deleted == 0) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口不存在");
        }
    }

    /**
     * 按业务接口配置和测试参数发起 HTTP 请求。
     */
    @Override
    public BusinessApiTestResponse testBusinessApi(Long id, BusinessApiTestCommand command) {
        BizApi api = getApiOrThrow(id);
        if (!Integer.valueOf(1).equals(api.getStatus())) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口已禁用");
        }
        BizSystem system = getSystemOrThrow(api.getSystemId());
        List<BizApiParameter> parameters = parameterMapper.findByApiId(api.getId());
        Map<String, Object> values = command == null || command.parameterValues() == null ? Map.of() : command.parameterValues();
        BusinessApiHttpRequest request = buildHttpRequest(system, api, parameters, values);
        BusinessApiHttpResponse response = httpClient.exchange(request);
        return new BusinessApiTestResponse(
                response.statusCode(),
                response.headers(),
                response.body(),
                extractData(response.body(), api.getResponseDataPath()),
                response.costMs()
        );
    }

    /**
     * 保存参数定义，先做字段校验再按排序号写入。
     */
    private void saveParameters(Long apiId, List<BusinessApiParameterCommand> parameters, LocalDateTime now) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        List<BusinessApiParameterCommand> sorted = parameters.stream()
                .sorted(Comparator.comparing(item -> item.sortNo() == null ? 0 : item.sortNo()))
                .toList();
        for (BusinessApiParameterCommand parameter : sorted) {
            validateParameter(parameter);
            parameterMapper.insert(new BizApiParameter(
                    idGenerator.nextId(),
                    apiId,
                    parameter.parameterName().trim(),
                    normalizeLocation(parameter.parameterLocation()),
                    normalizeType(parameter.parameterType()),
                    normalizeRequired(parameter.required()),
                    trimToNull(parameter.defaultValue()),
                    trimToNull(parameter.description()),
                    parameter.sortNo() == null ? 0 : parameter.sortNo(),
                    now,
                    now
            ));
        }
    }

    /**
     * 组装在线测试 HTTP 请求，包括 URL、认证 Header 和请求体。
     */
    private BusinessApiHttpRequest buildHttpRequest(
            BizSystem system,
            BizApi api,
            List<BizApiParameter> parameters,
            Map<String, Object> values) {

        Map<String, String> headers = new LinkedHashMap<>();
        applyAuthHeaders(system, headers);
        Map<String, Object> bodyValues = new LinkedHashMap<>();
        String path = api.getRequestPath();
        List<String> queryItems = new ArrayList<>();

        for (BizApiParameter parameter : parameters) {
            Object value = resolveParameterValue(parameter, values);
            if (value == null || String.valueOf(value).isBlank()) {
                continue;
            }
            String name = parameter.getParameterName();
            String encodedValue = urlEncode(String.valueOf(value));
            switch (parameter.getParameterLocation()) {
                case "PATH" -> path = path.replace("{" + name + "}", encodedValue);
                case "QUERY" -> queryItems.add(urlEncode(name) + "=" + encodedValue);
                case "HEADER" -> headers.put(name, String.valueOf(value));
                case "BODY" -> bodyValues.put(name, value);
                default -> throw new BusinessException(BUSINESS_API_ERROR_CODE, "参数位置不合法");
            }
        }

        String url = combineUrl(system.getBaseUrl(), path);
        if (!queryItems.isEmpty()) {
            url = url + (url.contains("?") ? "&" : "?") + String.join("&", queryItems);
        }
        String body = buildRequestBody(api, headers, bodyValues);
        return new BusinessApiHttpRequest(
                api.getRequestMethod(),
                url,
                headers,
                body,
                effectiveTimeout(api.getConnectTimeout(), system.getConnectTimeout(), DEFAULT_CONNECT_TIMEOUT),
                effectiveTimeout(api.getReadTimeout(), system.getReadTimeout(), DEFAULT_READ_TIMEOUT)
        );
    }

    /**
     * 解析参数值，未传值时使用默认值，必填参数缺失时阻断测试。
     */
    private Object resolveParameterValue(BizApiParameter parameter, Map<String, Object> values) {
        Object value = values.get(parameter.getParameterName());
        if ((value == null || String.valueOf(value).isBlank()) && parameter.getDefaultValue() != null) {
            value = parameter.getDefaultValue();
        }
        if ((value == null || String.valueOf(value).isBlank()) && Integer.valueOf(1).equals(parameter.getRequired())) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "参数 " + parameter.getParameterName() + " 不能为空");
        }
        return value;
    }

    /**
     * 按业务系统认证配置补充请求头。
     */
    private void applyAuthHeaders(BizSystem system, Map<String, String> headers) {
        Map<String, String> config = parseAuthConfig(system.getAuthConfig());
        switch (system.getAuthType()) {
            case "API_KEY" -> headers.put(valueOrDefault(config.get("headerName"), "X-API-Key"), valueOrDefault(config.get("apiKey"), ""));
            case "BEARER_TOKEN" -> headers.put("Authorization", "Bearer " + valueOrDefault(config.get("token"), ""));
            case "BASIC" -> {
                String raw = valueOrDefault(config.get("username"), "") + ":" + valueOrDefault(config.get("password"), "");
                headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
            }
            case "CUSTOM_HEADER" -> headers.put(valueOrDefault(config.get("headerName"), ""), valueOrDefault(config.get("headerValue"), ""));
            case "NONE" -> {
            }
            default -> throw new BusinessException(BUSINESS_API_ERROR_CODE, "认证类型不合法");
        }
        headers.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isBlank());
    }

    /**
     * 构造 JSON 请求体，GET/DELETE 默认不发送 Body。
     */
    private String buildRequestBody(BizApi api, Map<String, String> headers, Map<String, Object> bodyValues) {
        if (bodyValues.isEmpty() || Set.of("GET", "DELETE").contains(api.getRequestMethod())) {
            return null;
        }
        String contentType = api.getContentType() == null ? "application/json" : api.getContentType();
        headers.putIfAbsent("Content-Type", contentType);
        try {
            return objectMapper.writeValueAsString(bodyValues);
        } catch (RuntimeException | java.io.IOException exception) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "请求体组装失败");
        }
    }

    /**
     * 从 JSON 响应中按点号路径提取数据，解析失败时返回 null。
     */
    private Object extractData(String body, String responseDataPath) {
        if (body == null || body.isBlank() || responseDataPath == null || responseDataPath.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            for (String path : responseDataPath.split("\\.")) {
                node = node.path(path);
            }
            return node.isMissingNode() ? null : objectMapper.convertValue(node, Object.class);
        } catch (RuntimeException | java.io.IOException exception) {
            return null;
        }
    }

    /**
     * 解析业务系统认证配置 JSON，格式异常时视为空配置。
     */
    private Map<String, String> parseAuthConfig(String authConfig) {
        if (authConfig == null || authConfig.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(authConfig, new TypeReference<>() {
            });
        } catch (RuntimeException | java.io.IOException exception) {
            return Map.of();
        }
    }

    /**
     * 查询业务接口，不存在时抛出业务异常。
     */
    private BizApi getApiOrThrow(Long id) {
        validateId(id);
        BizApi api = businessApiMapper.selectById(id);
        if (api == null) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口不存在");
        }
        return api;
    }

    /**
     * 查询启用的业务系统，不存在或禁用时阻断接口配置和测试。
     */
    private BizSystem getSystemOrThrow(Long systemId) {
        BizSystem system = getSystemByIdOrThrow(systemId);
        if (!Integer.valueOf(1).equals(system.getStatus())) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务系统已禁用");
        }
        return system;
    }

    /**
     * 查询业务系统基础配置，详情展示需要名称和基础地址，禁用系统也允许回显。
     */
    private BizSystem getSystemByIdOrThrow(Long systemId) {
        if (systemId == null) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "所属业务系统不能为空");
        }
        BizSystem system = businessSystemMapper.selectById(systemId);
        if (system == null) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务系统不存在");
        }
        return system;
    }

    /**
     * 校验新增命令。
     */
    private void validateCreateCommand(BusinessApiCreateCommand command) {
        if (command == null) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口参数不能为空");
        }
        validateCommonFields(command.systemId(), command.apiCode(), command.apiName(), command.requestPath(), command.requestMethod(), command.status());
    }

    /**
     * 校验更新命令。
     */
    private void validateUpdateCommand(BusinessApiUpdateCommand command) {
        validateCommonFields(command.systemId(), command.apiCode(), command.apiName(), command.requestPath(), command.requestMethod(), command.status());
    }

    /**
     * 校验接口基础字段。
     */
    private void validateCommonFields(Long systemId, String apiCode, String apiName, String requestPath, String requestMethod, Integer status) {
        if (systemId == null) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "所属业务系统不能为空");
        }
        if (isBlank(apiCode)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "接口编码不能为空");
        }
        if (!CODE_PATTERN.matcher(apiCode.trim()).matches()) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "接口编码只允许字母、数字、下划线和短横线");
        }
        if (isBlank(apiName)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "接口名称不能为空");
        }
        if (isBlank(requestPath)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "请求路径不能为空");
        }
        normalizeMethod(requestMethod);
        normalizeStatus(status);
    }

    /**
     * 校验参数定义字段。
     */
    private void validateParameter(BusinessApiParameterCommand parameter) {
        if (parameter == null || isBlank(parameter.parameterName())) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "参数名称不能为空");
        }
        normalizeLocation(parameter.parameterLocation());
        normalizeType(parameter.parameterType());
        normalizeRequired(parameter.required());
    }

    /**
     * 构造业务接口实体。
     */
    private BizApi toEntity(
            Long id,
            Long systemId,
            String apiCode,
            String apiName,
            String requestPath,
            String requestMethod,
            String contentType,
            Integer connectTimeout,
            Integer readTimeout,
            String responseDataPath,
            Integer status,
            String description,
            LocalDateTime createdTime,
            LocalDateTime updatedTime) {

        return new BizApi(
                id,
                systemId,
                apiCode,
                apiName.trim(),
                requestPath.trim(),
                normalizeMethod(requestMethod),
                trimToNull(contentType),
                normalizeNullableTimeout(connectTimeout, "连接超时时间必须大于 0"),
                normalizeNullableTimeout(readTimeout, "读取超时时间必须大于 0"),
                trimToNull(responseDataPath),
                normalizeStatus(status),
                trimToNull(description),
                createdTime,
                updatedTime
        );
    }

    /**
     * 将分页行转换为列表响应。
     */
    private BusinessApiResponse toListResponse(BusinessApiMapper.BusinessApiRow row) {
        return new BusinessApiResponse(
                row.id(),
                row.systemId(),
                row.systemName(),
                row.systemBaseUrl(),
                combineUrl(row.systemBaseUrl(), row.requestPath()),
                row.apiCode(),
                row.apiName(),
                row.requestPath(),
                row.requestMethod(),
                row.contentType(),
                row.connectTimeout(),
                row.readTimeout(),
                row.responseDataPath(),
                row.status(),
                row.description(),
                row.createdTime(),
                row.updatedTime(),
                List.of()
        );
    }

    /**
     * 将实体转换为详情响应。
     */
    private BusinessApiResponse toResponse(BizApi api, BizSystem system, List<BizApiParameter> parameters) {
        return new BusinessApiResponse(
                api.getId(),
                api.getSystemId(),
                system.getSystemName(),
                system.getBaseUrl(),
                combineUrl(system.getBaseUrl(), api.getRequestPath()),
                api.getApiCode(),
                api.getApiName(),
                api.getRequestPath(),
                api.getRequestMethod(),
                api.getContentType(),
                api.getConnectTimeout(),
                api.getReadTimeout(),
                api.getResponseDataPath(),
                api.getStatus(),
                api.getDescription(),
                api.getCreatedTime(),
                api.getUpdatedTime(),
                parameters.stream().map(this::toParameterResponse).toList()
        );
    }

    /**
     * 将参数实体转换成响应对象。
     */
    private BusinessApiParameterResponse toParameterResponse(BizApiParameter parameter) {
        return new BusinessApiParameterResponse(
                parameter.getId(),
                parameter.getApiId(),
                parameter.getParameterName(),
                parameter.getParameterLocation(),
                parameter.getParameterType(),
                parameter.getRequired(),
                parameter.getDefaultValue(),
                parameter.getDescription(),
                parameter.getSortNo(),
                parameter.getCreatedTime(),
                parameter.getUpdatedTime()
        );
    }

    private String normalizeMethod(String method) {
        if (isBlank(method)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "请求方法不能为空");
        }
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_METHODS.contains(normalized)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "请求方法不合法");
        }
        return normalized;
    }

    private String normalizeLocation(String location) {
        if (isBlank(location)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "参数位置不能为空");
        }
        String normalized = location.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_LOCATIONS.contains(normalized)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "参数位置不合法");
        }
        return normalized;
    }

    private String normalizeType(String type) {
        if (isBlank(type)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "参数类型不能为空");
        }
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_TYPES.contains(normalized)) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "参数类型不合法");
        }
        return normalized;
    }

    private Integer normalizeRequired(Integer required) {
        if (required == null) {
            return 0;
        }
        if (required != 0 && required != 1) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "参数必填标记不合法");
        }
        return required;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口状态不合法");
        }
        return status;
    }

    private Integer normalizeNullableTimeout(Integer timeout, String message) {
        if (timeout == null) {
            return null;
        }
        if (timeout <= 0) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, message);
        }
        return timeout;
    }

    private Integer effectiveTimeout(Integer apiTimeout, Integer systemTimeout, Integer defaultTimeout) {
        if (apiTimeout != null && apiTimeout > 0) {
            return apiTimeout;
        }
        if (systemTimeout != null && systemTimeout > 0) {
            return systemTimeout;
        }
        return defaultTimeout;
    }

    /**
     * 拼接业务系统基础地址和接口相对路径，避免前端重复实现 URL 规则。
     */
    private String combineUrl(String baseUrl, String path) {
        if (isBlank(baseUrl)) {
            return path;
        }
        if (isBlank(path)) {
            return baseUrl;
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new BusinessException(BUSINESS_API_ERROR_CODE, "业务接口ID不能为空");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
