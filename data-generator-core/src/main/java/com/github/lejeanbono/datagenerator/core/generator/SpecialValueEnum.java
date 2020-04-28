package com.github.lejeanbono.datagenerator.core.generator;

public enum SpecialValueEnum {
    NULL("##NULL##"),
    EMPTY("##EMPTY##");

    private String code;

    SpecialValueEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static SpecialValueEnum get(Object code) {
        for (SpecialValueEnum e : SpecialValueEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
