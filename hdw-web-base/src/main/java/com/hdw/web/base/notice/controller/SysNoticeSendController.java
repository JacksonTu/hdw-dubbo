package com.hdw.web.base.notice.controller;

import com.alibaba.fastjson.JSONObject;
import com.hdw.api.notice.dto.NoticeSendDto;
import com.hdw.api.notice.entity.SysNoticeSend;
import com.hdw.api.notice.service.ISysNoticeSendService;
import com.hdw.common.core.api.CommonResult;
import com.hdw.common.core.constant.WebsocketConstant;
import com.hdw.common.core.vo.LoginUserVo;
import com.hdw.web.base.shiro.ShiroUtil;
import com.hdw.web.base.websocket.CommonWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;


/**
 * 用户通告阅读标记表 Controller
 *
 * @author JacksonTu
 * @date 2020-10-12 15:03:04
 */
@Slf4j
@Validated
@RestController
@RequestMapping("notice/send")
public class SysNoticeSendController {

    @DubboReference
    private ISysNoticeSendService sysNoticeSendService;

    @Resource
    private CommonWebSocket webSocket;

    @GetMapping
    @RequiresPermissions("notice/send/list")
    public CommonResult listSysNoticeSend(NoticeSendDto noticeSendDto) {
        return CommonResult.success(sysNoticeSendService.listSysNoticeSend(noticeSendDto));
    }

    @GetMapping("list")
    @RequiresPermissions("notice/send/list")
    public CommonResult pageSysNoticeSend(NoticeSendDto noticeSendDto) {
        return CommonResult.success(this.sysNoticeSendService.pageSysNoticeSend(noticeSendDto));
    }

    @GetMapping("info/{id}")
    @RequiresPermissions("notice/send/info")
    public CommonResult info(@PathVariable("id") Long id) {
        return CommonResult.success(this.sysNoticeSendService.getById(id));
    }

    @PostMapping("save")
    @RequiresPermissions("notice/send/save")
    public CommonResult saveSysNoticeSend(@Valid @RequestBody SysNoticeSend sysNoticeSend) {
        try {
            this.sysNoticeSendService.saveSysNoticeSend(sysNoticeSend);
            return CommonResult.success("新增成功");
        } catch (Exception e) {
            String message = "新增失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    @PostMapping("delete")
    @RequiresPermissions("notice/send/delete")
    public CommonResult deleteSysNoticeSend(@RequestBody String[] ids) {
        try {
            this.sysNoticeSendService.deleteSysNoticeSend(ids);
            return CommonResult.success("删除成功");
        } catch (Exception e) {
            String message = "删除失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    @PostMapping("update")
    @RequiresPermissions("notice/send/update")
    public CommonResult updateSysNoticeSend(@RequestBody SysNoticeSend sysNoticeSend) {
        try {
            this.sysNoticeSendService.updateSysNoticeSend(sysNoticeSend);
            return CommonResult.success("修改成功");
        } catch (Exception e) {
            String message = "修改失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    /**
     * 更新用户系统消息阅读状态
     *
     * @param noticeId
     * @return
     */
    @GetMapping("editByNoticeIdAndUserId/{noticeId}")
    public CommonResult editByNoticeIdAndUserId(@PathVariable("noticeId") Long noticeId) {
        try {
            LoginUserVo loginUserVo = ShiroUtil.getUser();
            Long userId = loginUserVo.getId();
            SysNoticeSend noticeSend = new SysNoticeSend();
            noticeSend.setUserId(userId);
            noticeSend.setNoticeId(noticeId);
            noticeSend.setCreateUser(loginUserVo.getLoginName());
            noticeSend.setCreateTime(new Date());
            noticeSend.setUpdateUser(loginUserVo.getLoginName());
            noticeSend.setUpdateTime(new Date());
            noticeSend.setReadFlag(WebsocketConstant.HAS_READ_FLAG);
            noticeSend.setReadTime(new Date());
            this.sysNoticeSendService.updateByUserIdAndNoticeId(noticeSend);
            JSONObject obj = new JSONObject();
            obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_REVOKE);
            webSocket.sendAllMessage(obj.toJSONString());
            return CommonResult.success("");
        } catch (Exception e) {
            String message = "更新阅读状态失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    /**
     * 获取我的消息
     *
     * @param noticeSendDto
     * @return
     */
    @GetMapping("getMyNoticeSend")
    public CommonResult getMyNoticeSend(NoticeSendDto noticeSendDto) {
        LoginUserVo loginUserVo = ShiroUtil.getUser();
        Long userId = loginUserVo.getId();
        noticeSendDto.setUserId(userId);
        return CommonResult.success(this.sysNoticeSendService.pageMyNoticeSend(noticeSendDto));
    }

    /**
     * 一键已读
     *
     * @return
     */
    @GetMapping("readAll")
    public CommonResult readAll() {
        try {
            LoginUserVo loginUserVo = ShiroUtil.getUser();
            Long userId = loginUserVo.getId();
            SysNoticeSend noticeSend = new SysNoticeSend();
            noticeSend.setUserId(userId);
            noticeSend.setCreateUser(loginUserVo.getLoginName());
            noticeSend.setCreateTime(new Date());
            noticeSend.setUpdateUser(loginUserVo.getLoginName());
            noticeSend.setUpdateTime(new Date());
            noticeSend.setReadFlag(WebsocketConstant.HAS_READ_FLAG);
            noticeSend.setReadTime(new Date());
            this.sysNoticeSendService.updateByUserIdAndNoticeId(noticeSend);
            JSONObject obj = new JSONObject();
            obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_REVOKE);
            webSocket.sendAllMessage(obj.toJSONString());
            return CommonResult.success("");
        } catch (Exception e) {
            String message = "全部已读失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }
}
