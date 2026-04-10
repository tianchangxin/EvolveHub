package org.evolve.auth.user.service;

import cn.dev33.satoken.stp.StpInterface;
import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.PermissionsInfra;
import org.evolve.auth.user.infra.RolePermissionsInfra;
import org.evolve.auth.user.infra.RolesInfra;
import org.evolve.auth.user.infra.UserRolesInfra;
import org.evolve.auth.user.model.PermissionsEntity;
import org.evolve.auth.user.model.RolePermissionsEntity;
import org.evolve.auth.user.model.RolesEntity;
import org.evolve.auth.user.model.UserRolesEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色数据提供者
 * <p>
 * Sa-Token 在调用 {@code StpUtil.checkPermission("xxx")} 或 {@code StpUtil.checkRole("xxx")} 时，
 * 会通过此接口获取当前登录用户持有的权限码列表和角色码列表。
 * <p>
 * 查询链路：userId → t_user_role → roleId → t_role_permission → permissionId → t_permission.permCode
 *
 * @author zhao
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserRolesInfra userRolesInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Resource
    private PermissionsInfra permissionsInfra;

    /**
     * 获取用户持有的所有权限码
     * <p>查询链路: userId → userRole → rolePermission → permission.permCode</p>
     *
     * @param loginId   登录用户ID
     * @param loginType 登录类型（默认 "login"）
     * @return 权限码列表，如 ["user:list", "user:create", "role:list"]
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        long userId = Long.parseLong(loginId.toString());
        List<String> permCodes = new ArrayList<>();

        // 1. 查询用户持有的所有角色
        List<UserRolesEntity> userRoles = userRolesInfra.listByUserId(userId);

        for (UserRolesEntity userRole : userRoles) {
            // 2. 查询每个角色持有的所有权限关联
            List<RolePermissionsEntity> rolePerms = rolePermissionsInfra.listByRoleId(userRole.getRoleId());

            for (RolePermissionsEntity rolePerm : rolePerms) {
                // 3. 查询权限实体，获取 permCode
                PermissionsEntity perm = permissionsInfra.getPermissionById(rolePerm.getPermissionId());
                if (perm != null && perm.getStatus() == 1) {
                    permCodes.add(perm.getPermCode());
                }
            }
        }

        return permCodes.stream().distinct().toList();
    }

    /**
     * 获取用户持有的所有角色码
     *
     * @param loginId   登录用户ID
     * @param loginType 登录类型（默认 "login"）
     * @return 角色码列表，如 ["ROLE_ADMIN", "ROLE_USER"]
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        long userId = Long.parseLong(loginId.toString());
        List<String> roleCodes = new ArrayList<>();

        List<UserRolesEntity> userRoles = userRolesInfra.listByUserId(userId);
        for (UserRolesEntity userRole : userRoles) {
            RolesEntity role = rolesInfra.getRoleById(userRole.getRoleId());
            if (role != null && role.getStatus() == 1) {
                roleCodes.add(role.getRoleCode());
            }
        }

        return roleCodes;
    }
}
