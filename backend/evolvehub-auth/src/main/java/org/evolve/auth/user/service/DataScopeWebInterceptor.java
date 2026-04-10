package org.evolve.auth.user.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.evolve.auth.user.infra.*;
import org.evolve.auth.user.model.DeptEntity;
import org.evolve.auth.user.model.RoleDataScopeEntity;
import org.evolve.auth.user.model.RolesEntity;
import org.evolve.auth.user.model.UserRolesEntity;
import org.evolve.auth.user.model.UsersEntity;
import org.evolve.common.base.CurrentUserHolder;
import org.evolve.common.datascope.DataScopeContext;
import org.evolve.common.datascope.DataScopeContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据权限 Web 拦截器
 * <p>
 * 在每次请求进入时，根据当前登录用户的角色 dataScope 计算可见部门集合，
 * 放入 {@link DataScopeContextHolder}，供 MyBatis 拦截器使用。
 * 请求结束后自动清除 ThreadLocal，防止内存泄漏。
 * </p>
 * <p>
 * 多角色策略：取所有角色中最大权限（最小 scopeType 值，1=全部 > 其他）。
 * </p>
 *
 * @author zhao
 */
@Component
public class DataScopeWebInterceptor implements HandlerInterceptor {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private RoleDataScopeInfra roleDataScopeInfra;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 未登录时跳过（网关已做登录校验，走到这里一般是已登录状态）
        if (!StpUtil.isLogin()) {
            return true;
        }

        long userId = StpUtil.getLoginIdAsLong();
        CurrentUserHolder.set(userId);
        UsersEntity user = usersInfra.getUserById(userId);
        if (user == null) {
            return true;
        }

        // 查询用户所有角色
        List<UserRolesEntity> userRoles = userRolesInfra.listByUserId(userId);
        if (userRoles.isEmpty()) {
            // 无角色，默认仅本人数据
            DataScopeContextHolder.set(DataScopeContext.builder()
                    .userId(userId)
                    .deptId(user.getDeptId())
                    .scopeType(4)
                    .build());
            return true;
        }

        // 多角色取最大权限（scopeType 越小权限越大）
        int finalScopeType = Integer.MAX_VALUE;
        Set<Long> allVisibleDeptIds = new HashSet<>();

        for (UserRolesEntity userRole : userRoles) {
            RolesEntity role = rolesInfra.getRoleById(userRole.getRoleId());
            if (role == null || role.getStatus() != 1) {
                continue;
            }

            int scopeType = role.getDataScope();

            if (scopeType == 1) {
                // 全部数据权限，直接返回
                DataScopeContextHolder.set(DataScopeContext.builder()
                        .userId(userId)
                        .deptId(user.getDeptId())
                        .scopeType(1)
                        .build());
                return true;
            }

            if (scopeType < finalScopeType) {
                finalScopeType = scopeType;
            }

            // 收集可见部门
            switch (scopeType) {
                case 2 -> {
                    // 本部门及子部门
                    allVisibleDeptIds.add(user.getDeptId());
                    collectChildDeptIds(user.getDeptId(), allVisibleDeptIds);
                }
                case 3 -> {
                    // 仅本部门
                    allVisibleDeptIds.add(user.getDeptId());
                }
                case 5 -> {
                    // 自定义部门
                    List<RoleDataScopeEntity> scopes = roleDataScopeInfra.listByRoleId(role.getId());
                    for (RoleDataScopeEntity scope : scopes) {
                        allVisibleDeptIds.add(scope.getDeptId());
                    }
                }
                // case 4 (仅本人) 不需要收集部门
            }
        }

        DataScopeContextHolder.set(DataScopeContext.builder()
                .userId(userId)
                .deptId(user.getDeptId())
                .scopeType(finalScopeType)
                .visibleDeptIds(allVisibleDeptIds.isEmpty() ? null : allVisibleDeptIds)
                .build());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清除 ThreadLocal，防止内存泄漏
        DataScopeContextHolder.clear();
        CurrentUserHolder.clear();
    }

    /**
     * 递归收集所有子部门 ID
     *
     * @param parentId      父部门 ID
     * @param collectedIds  已收集的部门 ID 集合
     */
    private void collectChildDeptIds(Long parentId, Set<Long> collectedIds) {
        List<DeptEntity> children = deptInfra.lambdaQuery()
                .eq(DeptEntity::getParentId, parentId)
                .eq(DeptEntity::getStatus, 1)
                .list();
        for (DeptEntity child : children) {
            collectedIds.add(child.getId());
            collectChildDeptIds(child.getId(), collectedIds);
        }
    }
}
