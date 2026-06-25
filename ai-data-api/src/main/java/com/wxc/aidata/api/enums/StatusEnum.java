package com.wxc.aidata.api.enums;

public enum StatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String description;

    StatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
