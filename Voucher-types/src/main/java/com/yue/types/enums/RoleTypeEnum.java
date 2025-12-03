/**
 * @description:
 * @author: 29874
 * @date: 2025/11/16 16:49
 */

package com.yue.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum RoleTypeEnum {
    ADMIN(1, "admin"),
    USER(2, "user");

    private  Integer code;
    private  String name;


    // 添加这个静态查找方法：
    public static String getNameByCode(Integer code) {
        for (RoleTypeEnum role : RoleTypeEnum.values()) {
            if (role.code.equals(code)) {
                return role.name(); // 返回枚举的名称 (ADMIN, USER)
            }
        }
        return "UNKNOWN"; // 如果找不到，返回未知
    }
}
