/**
 * @description:
 * @author: 29874
 * @date: 2025/11/16 11:33
 */

package com.yue.infrastructure.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String username;
    private String password;

    private List<Integer> roles;
    private List<Integer> permissions;
}
