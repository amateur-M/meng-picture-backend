package com.meng.mengpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @description:  用户角色枚举类
 *
 * @throws:
 * @return:
 */
@Getter
public enum UserRoleEnum {

    USER("user", "用户", 1),
    ADMIN("admin", "管理员", 999);

    private final String text;

    private final String value;

    // 用户角色权限优先级 方便后续扩展
    private final int level;

    UserRoleEnum(String value, String text, int level) {
        this.value = value;
        this.text = text;
        this.level = level;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)){
            return null;
        }

        for (UserRoleEnum roleEnum : values()) {
            if (roleEnum.value.equals(value)) {
                return roleEnum;
            }
        }
        return null;
    }

}
