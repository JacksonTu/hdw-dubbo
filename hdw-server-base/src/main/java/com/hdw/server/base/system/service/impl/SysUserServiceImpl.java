package com.hdw.server.base.system.service.impl;


import com.hdw.api.base.system.entity.SysResource;
import com.hdw.api.base.system.entity.SysUser;
import com.hdw.api.base.system.service.ISysResourceService;
import com.hdw.api.base.system.service.ISysUserEnterpriseService;
import com.hdw.api.base.system.service.ISysUserRoleService;
import com.hdw.api.base.system.service.ISysUserService;
import com.hdw.common.core.constant.CommonConstant;
import com.hdw.common.core.exception.BaseException;
import com.hdw.common.core.service.impl.BaseServiceImpl;
import com.hdw.common.core.vo.LoginUserVo;
import com.hdw.server.base.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 用户表
 *
 * @author JacksonTu
 * @date 2018-12-11 11:35:15
 */
@Slf4j
@DubboService(interfaceName = "ISysUserService")
public class SysUserServiceImpl extends BaseServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private ISysUserRoleService sysUserRoleService;
    @Autowired
    private ISysUserEnterpriseService sysUserEnterpriseService;
    @Autowired
    private ISysResourceService sysResourceService;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public SysUser selectByLoginName(String loginName) {
        SysUser sysUser = this.baseMapper.selectByLoginName(loginName);
        if (ObjectUtils.isEmpty(sysUser)) {
            return null;
        }
        return sysUser;
    }

    @Override
    public LoginUserVo selectLoginUserVoByLoginName(String loginName) {
        SysUser sysUser = this.baseMapper.selectByLoginName(loginName);
        if (ObjectUtils.isEmpty(sysUser)) {
            return null;
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtils.copyProperties(sysUser, loginUserVo);
        List<Long> enterpriseIds = sysUserEnterpriseService.selectEnterpriseIdByUserId(sysUser.getId());
        loginUserVo.setEnterpriseIdList(enterpriseIds);
        return loginUserVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveByVo(SysUser user) {

        this.baseMapper.insert(user);
        //检查角色是否越权
        checkRole(user);

        try {
            threadPoolExecutor.submit(() -> {
                //保存用户与角色关系
                sysUserRoleService.saveOrUpdateUserRole(user.getId(), user.getRoleIdList());
            });
            threadPoolExecutor.submit(() -> {
                //保存用户与企业关系关系
                sysUserEnterpriseService.saveOrUpdateUserEnterprise(user.getId(), user.getEnterpriseIdList());
            });
        } finally {

        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateByVo(SysUser user) {

        this.updateById(user);
        //检查角色是否越权
        checkRole(user);

        try {
            threadPoolExecutor.submit(() -> {
                //保存用户与角色关系
                sysUserRoleService.saveOrUpdateUserRole(user.getId(), user.getRoleIdList());
            });
            threadPoolExecutor.submit(() -> {
                //保存用户与企业关系关系
                sysUserEnterpriseService.saveOrUpdateUserEnterprise(user.getId(), user.getEnterpriseIdList());
            });
        } finally {

        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBatch(Long[] userIds) {
        this.removeByIds(Arrays.asList(userIds));
        //删除用户与角色关系
        sysUserRoleService.deleteBatchByUserIds(userIds);
        //删除监管用户与企业关系
        sysUserEnterpriseService.deleteBatchByUserIds(userIds);
    }

    @Override
    public Set<String> selectUserPermissions(long userId) {
        List<String> permsList;
        //系统管理员，拥有最高权限
        if (userId == CommonConstant.SUPER_ADMIN) {
            List<SysResource> menuList = sysResourceService.list();
            permsList = new ArrayList<>(menuList.size());
            for (SysResource menu : menuList) {
                permsList.add(menu.getUrl());
            }
        } else {
            permsList = this.baseMapper.selectPerms(userId);
        }
        return permsList.stream().collect(Collectors.toSet());
    }

    @Override
    public Set<String> selectUserRoles(long userId) {
        List<String> roleList = this.baseMapper.selectRoles(userId);
        return roleList.stream().collect(Collectors.toSet());
    }

    @Override
    public List<Long> selectResourceIdListByUserId(Long userId) {
        return this.baseMapper.selectResourceIdListByUserId(userId);
    }

    /**
     * 检查角色是否越权
     */
    private void checkRole(SysUser user) {
        if (user.getRoleIdList() == null || user.getRoleIdList().size() == 0) {
            return;
        }
        //如果不是超级管理员，则需要判断用户的角色是否自己创建
        if (user.getCreateUserId() == CommonConstant.SUPER_ADMIN) {
            return;
        }
        //查询用户创建的角色列表
        List<Long> roleIdList = sysUserRoleService.selectRoleIdListByUserId(user.getCreateUserId());

        //判断是否越权
        if (!roleIdList.containsAll(user.getRoleIdList())) {
            throw new BaseException("新增用户所选角色，不是本人创建");
        }
    }
}
