package com.hdw.server.base.enterprise.service.impl;

import com.hdw.api.base.enterprise.entity.Enterprise;
import com.hdw.api.base.enterprise.service.IEnterpriseService;
import com.hdw.common.core.service.impl.BaseServiceImpl;
import com.hdw.server.base.enterprise.mapper.EnterpriseMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 企业信息表
 *
 * @author JacksonTu
 * @date 2018-12-11 13:49:00
 */
@Slf4j
@DubboService(interfaceName = "IEnterpriseService")
@Transactional(rollbackFor = Exception.class)
public class EnterpriseServiceImpl extends BaseServiceImpl<EnterpriseMapper, Enterprise> implements IEnterpriseService {

    @Override
    public List<Map<String, Object>> selectEnterpriseList(Map<String, Object> par) {
        return this.baseMapper.selectEnterpriseList(par);
    }

    @Override
    public Enterprise saveEnterprise(Enterprise enterprise) {
        boolean flag = this.save(enterprise);
        if (flag) {
            return enterprise;
        }
        return null;
    }
}
