package com.hdw.server.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hdw.api.notice.dto.NoticeSendDto;
import com.hdw.api.notice.entity.SysNotice;
import com.hdw.api.notice.entity.SysNoticeSend;
import com.hdw.api.notice.service.ISysNoticeSendService;
import com.hdw.common.core.vo.PageVo;
import com.hdw.server.notice.mapper.SysNoticeSendMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 用户通告阅读标记表 Service实现
 *
 * @author JacksonTu
 * @date 2020-10-12 15:03:04
 */
@DubboService
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class SysNoticeSendServiceImpl extends ServiceImpl<SysNoticeSendMapper, SysNoticeSend> implements ISysNoticeSendService {

    @Override
    public PageVo<SysNoticeSend> pageSysNoticeSend(NoticeSendDto noticeSendDto) {
        Page page = new Page();
        // 设置当前页码
        page.setCurrent(noticeSendDto.getPage());
        // 设置页大小
        page.setSize(noticeSendDto.getLimit());

        LambdaQueryWrapper<SysNoticeSend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(noticeSendDto.getNoticeId() != null, SysNoticeSend::getNoticeId, noticeSendDto.getNoticeId())
                .eq(noticeSendDto.getUserId() != null, SysNoticeSend::getUserId, noticeSendDto.getUserId())
                .orderByDesc(true, SysNoticeSend::getCreateTime);

        return new PageVo<>(this.baseMapper.selectPage(page, wrapper));
    }

    @Override
    public List<SysNoticeSend> listSysNoticeSend(NoticeSendDto noticeSendDto) {
        LambdaQueryWrapper<SysNoticeSend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(noticeSendDto.getNoticeId() != null, SysNoticeSend::getNoticeId, noticeSendDto.getNoticeId())
                .eq(noticeSendDto.getUserId() != null, SysNoticeSend::getUserId, noticeSendDto.getUserId())
                .orderByDesc(true, SysNoticeSend::getCreateTime);
        // TODO 设置查询条件
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSysNoticeSend(SysNoticeSend sysNoticeSend) {
        this.save(sysNoticeSend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSysNoticeSend(SysNoticeSend sysNoticeSend) {
        this.saveOrUpdate(sysNoticeSend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSysNoticeSend(String[] ids) {
        List<String> list = Arrays.asList(ids);
        this.removeByIds(list);
    }

    @Override
    public List<Long> listByUserId(Long userId) {
        return this.baseMapper.listByUserId(userId);
    }

    @Override
    public SysNoticeSend findByNoticeIdAndUserId(Long noticeId, Long userId) {
        LambdaQueryWrapper<SysNoticeSend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(noticeId != null, SysNoticeSend::getNoticeId, noticeId)
                .eq(userId != null, SysNoticeSend::getUserId, userId);
        return this.baseMapper.selectOne(wrapper);
    }

    @Override
    public PageVo<SysNotice> pageMyNoticeSend(NoticeSendDto noticeSendDto) {
        Page page = new Page();
        // 设置当前页码
        page.setCurrent(noticeSendDto.getPage());
        // 设置页大小
        page.setSize(noticeSendDto.getLimit());
        return new PageVo<>(baseMapper.pageMyNoticeSend(page, noticeSendDto));
    }

    @Override
    public void updateByUserIdAndNoticeId(SysNoticeSend sysNoticeSend) {
        UpdateWrapper<SysNoticeSend> updateWrapper = new UpdateWrapper();
        if (sysNoticeSend != null) {
            Long noticeId = sysNoticeSend.getNoticeId();
            Long userId = sysNoticeSend.getUserId();
            if (noticeId != null && userId != null) {
                updateWrapper.last("where notice_id =" + noticeId + " and user_id =" + userId);
                this.update(sysNoticeSend, updateWrapper);
            }
            if (userId != null) {
                updateWrapper.last("where user_id =" + userId);
                this.update(sysNoticeSend, updateWrapper);
            }
        }

    }

    @Override
    public void deleteByNoticeId(String[] noticeIds) {
        LambdaQueryWrapper<SysNoticeSend> wrapper = new LambdaQueryWrapper<>();
        if (noticeIds != null) {
            for (String noticeId : noticeIds) {
                wrapper.eq(StringUtils.isNoneBlank(noticeId), SysNoticeSend::getNoticeId, noticeId);
                this.remove(wrapper);
            }
        }
    }
}