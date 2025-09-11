package com.codeit.findex.entity;

public enum ResultType {
    SUCCESS(true),
    FAIL(false);

    private final boolean value;

    ResultType(boolean value) {
        this.value = value;
    }

    public boolean toBoolean() {
        return value;
    }

    public static ResultType fromBoolean(Boolean value) {
        return (value != null && value) ? SUCCESS : FAIL;
    }
}
