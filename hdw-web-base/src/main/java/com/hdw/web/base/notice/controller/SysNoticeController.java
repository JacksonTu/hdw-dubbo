package com.hdw.web.base.notice.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hdw.api.notice.dto.NoticeDto;
import com.hdw.api.notice.entity.SysNotice;
import com.hdw.api.notice.entity.SysNoticeSend;
import com.hdw.api.notice.service.ISysNoticeSendService;
import com.hdw.api.notice.service.ISysNoticeService;
import com.hdw.common.core.api.CommonResult;
import com.hdw.common.core.constant.WebsocketConstant;
import com.hdw.common.core.vo.LoginUserVo;
import com.hdw.common.core.vo.PageVo;
import com.hdw.common.core.vo.TreeVo;
import com.hdw.web.base.shiro.ShiroUtil;
import com.hdw.web.base.websocket.CommonWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 系统通告表 Controller
 *
 * @author JacksonTu
 * @date 2020-10-12 15:02:54
 */
@Slf4j
@Validated
@RestController
@RequestMapping("notice")
public class SysNoticeController {

    @DubboReference
    private ISysNoticeService sysNoticeService;
    @DubboReference
    private ISysNoticeSendService sysNoticeSendService;
    @Resource
    private CommonWebSocket webSocket;


    @GetMapping
    @RequiresPermissions("notice/list")
    public CommonResult listSysNotice(NoticeDto noticeDto) {
        return CommonResult.success(sysNoticeService.listSysNotice(noticeDto));
    }

    @GetMapping("list")
    @RequiresPermissions("notice/list")
    public CommonResult pageSysNotice(NoticeDto noticeDto) {
        LoginUserVo loginUserVo = ShiroUtil.getUser();
        /**
         * 0：超级管理员，1：企业用户，2：监管用户
         */
        Integer userType = loginUserVo.getUserType();
        if (userType != 0) {
            noticeDto.setCreateUser(loginUserVo.getLoginName());
        }
        return CommonResult.success(this.sysNoticeService.pageSysNotice(noticeDto));
    }

    @GetMapping("info/{id}")
    @RequiresPermissions("notice/info")
    public CommonResult info(@PathVariable("id") Long id) {
        return CommonResult.success(this.sysNoticeService.getById(id));
    }

    @PostMapping("save")
    @RequiresPermissions("notice/save")
    public CommonResult saveSysNotice(@Valid @RequestBody SysNotice sysNotice) {
        try {
            LoginUserVo loginUserVo = ShiroUtil.getUser();
            sysNotice.setCreateUser(loginUserVo.getLoginName());
            sysNotice.setCreateTime(new Date());
            this.sysNoticeService.saveSysNotice(sysNotice);
            return CommonResult.success("添加成功");
        } catch (Exception e) {
            String message = "新增失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    @PostMapping("delete")
    @RequiresPermissions("notice/delete")
    public CommonResult deleteSysNotice(@RequestBody String[] ids) {
        try {
            this.sysNoticeSendService.deleteByNoticeId(ids);
            this.sysNoticeService.deleteSysNotice(ids);
            return CommonResult.success("删除成功");
        } catch (Exception e) {
            String message = "删除失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    @PostMapping("update")
    @RequiresPermissions("notice/update")
    public CommonResult updateSysNotice(@RequestBody SysNotice sysNotice) {
        try {
            LoginUserVo loginUserVo = ShiroUtil.getUser();
            sysNotice.setUpdateUser(loginUserVo.getLoginName());
            sysNotice.setUpdateTime(new Date());
            this.sysNoticeService.updateSysNotice(sysNotice);
            return CommonResult.success("修改成功");
        } catch (Exception e) {
            String message = "修改失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    /**
     * 发布操作
     *
     * @return
     */
    @GetMapping("release/{id}")
    @RequiresPermissions("notice/release")
    public CommonResult doReleaseData(@PathVariable("id") Long id) {
        try {
            LoginUserVo loginUserVo = ShiroUtil.getUser();
            String message = "成功";
            SysNotice notice = this.sysNoticeService.getById(id);
            if (notice == null) {
                return CommonResult.success("运行异常，请联系管理员");
            } else {
                notice.setSendStatus("1");
                notice.setCreateUser(loginUserVo.getLoginName());
                notice.setUpdateUser(loginUserVo.getLoginName());
                notice.setSendTime(new Date());
                notice.setUpdateTime(new Date());
                boolean flag = this.sysNoticeService.updateById(notice);
                if (flag) {
                    message = "成功";
                    // 1.全局推送
                    if (notice.getMsgType().equals(WebsocketConstant.MSG_TYPE_ALL)) {
                        JSONObject obj = new JSONObject();
                        obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_TOPIC);
                        obj.put(WebsocketConstant.MSG_ID, notice.getId());
                        obj.put(WebsocketConstant.MSG_TITLE, notice.getTitle());
                        obj.put(WebsocketConstant.MSG_TXT, notice.getMsgAbstract());
                        webSocket.sendAllMessage(obj.toJSONString());
                    } else {
                        // 2.插入用户通告阅读标记表记录
                        String userId = notice.getUserIds();
                        String[] userIds = userId.substring(0, (userId.length() - 1)).split(",");
                        JSONObject obj = new JSONObject();
                        obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_USER);
                        obj.put(WebsocketConstant.MSG_ID, notice.getId());
                        obj.put(WebsocketConstant.MSG_TITLE, notice.getTitle());
                        obj.put(WebsocketConstant.MSG_TXT, notice.getMsgAbstract());
                        webSocket.sendMoreMessage(userIds, obj.toJSONString());
                    }
                }
            }
            return CommonResult.success(message);
        } catch (Exception e) {
            String message = "失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    /**
     * 撤销操作
     *
     * @return
     */
    @GetMapping("revoke/{id}")
    @RequiresPermissions("notice/revoke")
    public CommonResult doRevokeData(@PathVariable("id") Long id) {
        try {
            LoginUserVo loginUserVo = ShiroUtil.getUser();
            String message = "";
            SysNotice notice = this.sysNoticeService.getById(id);
            if (notice == null) {
                return CommonResult.failed("运行异常，请联系管理员");
            } else {
                notice.setSendStatus("2");
                notice.setCreateUser(loginUserVo.getLoginName());
                notice.setUpdateUser(loginUserVo.getLoginName());
                notice.setCancelTime(new Date());
                notice.setUpdateTime(new Date());
                boolean flag = this.sysNoticeService.updateById(notice);
                if (flag) {
                    message = "成功";
                    JSONObject obj = new JSONObject();
                    obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_REVOKE);
                    webSocket.sendAllMessage(obj.toJSONString());
                }
            }
            return CommonResult.success(message);
        } catch (Exception e) {
            String message = "失败";
            log.error(message, e);
            return CommonResult.failed("运行异常，请联系管理员");
        }
    }

    /**
     * 补充用户数据，并返回系统消息
     *
     * @return
     */
    @GetMapping("listNoticeByUser")
    public CommonResult listNoticeByUser() {
        LoginUserVo loginUserVo = ShiroUtil.getUser();
        Long userId = loginUserVo.getId();
        List<Long> noticeIds = this.sysNoticeSendService.listByUserId(userId);
        // 1.将系统消息补充到用户通告阅读标记表中
        List<SysNotice> notices = this.sysNoticeService.listByCondition(WebsocketConstant.MSG_TYPE_ALL, WebsocketConstant.HAS_SEND, loginUserVo.getCreateTime(), noticeIds);
        if (notices != null && notices.size() > 0) {
            notices.forEach(notice -> {
                SysNoticeSend noticeSend = this.sysNoticeSendService.findByNoticeIdAndUserId(notice.getId(), userId);
                if (noticeSend == null) {
                    SysNoticeSend ns = new SysNoticeSend();
                    ns.setNoticeId(notice.getId());
                    ns.setUserId(userId);
                    ns.setReadFlag(WebsocketConstant.NO_READ_FLAG);
                    ns.setCreateTime(new Date());
                    ns.setCreateUser(loginUserVo.getLoginName());
                    ns.setUpdateTime(new Date());
                    ns.setUpdateUser(loginUserVo.getLoginName());
                    this.sysNoticeSendService.saveSysNoticeSend(ns);
                }
            });
        }
        // 2.将用户消息补充到用户通告阅读标记表中
        List<SysNotice> userNotices = this.sysNoticeService.listByCondition(WebsocketConstant.MSG_TYPE_USER, WebsocketConstant.HAS_SEND, loginUserVo.getCreateTime(), noticeIds);
        if (userNotices != null && userNotices.size() > 0) {
            userNotices.forEach(notice -> {
                SysNoticeSend noticeSend = this.sysNoticeSendService.findByNoticeIdAndUserId(notice.getId(), userId);
                if (noticeSend == null && notice.getUserIds().contains(userId.toString())) {
                    SysNoticeSend ns = new SysNoticeSend();
                    ns.setNoticeId(notice.getId());
                    ns.setUserId(userId);
                    ns.setReadFlag(WebsocketConstant.NO_READ_FLAG);
                    ns.setCreateTime(new Date());
                    ns.setCreateUser(loginUserVo.getLoginName());
                    ns.setUpdateTime(new Date());
                    ns.setUpdateUser(loginUserVo.getLoginName());
                    this.sysNoticeSendService.saveSysNoticeSend(ns);
                }
            });
        }

        // 3.查询用户未读的系统消息
        Map<String, Object> params = Maps.newHashMap();
        NoticeDto noticeDto = new NoticeDto();
        noticeDto.setPage(1L);
        noticeDto.setLimit(5L);

        //通知公告消息
        noticeDto.setUserId(userId);
        noticeDto.setMsgCategory("1");
        PageVo<SysNotice> noticePage = this.sysNoticeService.pageUnreadMsg(noticeDto);
        //系统消息
        noticeDto.setUserId(userId);
        noticeDto.setMsgCategory("2");

        PageVo<SysNotice> msgPage = this.sysNoticeService.pageUnreadMsg(noticeDto);
        params.clear();
        params.put("sysMsgList", msgPage.getList());
        params.put("sysMsgTotal", msgPage.getTotalCount());
        params.put("noticeMsgList", noticePage.getList());
        params.put("noticeMsgTotal", noticePage.getTotalCount());
        return CommonResult.success(params);
    }

    /**
     * 同步消息
     *
     * @param noticeId
     * @return
     */
    @GetMapping("syncNotice/{noticeId}")
    public CommonResult syncNotice(@PathVariable(name = "noticeId") Long noticeId) {
        JSONObject obj = new JSONObject();
        if (noticeId != null) {
            SysNotice notice = this.sysNoticeService.getById(noticeId);
            if (notice == null) {
                return CommonResult.failed("运行异常，请联系管理员");
            } else {
                if (notice.getMsgType().equals(WebsocketConstant.MSG_TYPE_ALL)) {
                    obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_TOPIC);
                    obj.put(WebsocketConstant.MSG_ID, notice.getId());
                    obj.put(WebsocketConstant.MSG_TXT, notice.getTitle());
                    webSocket.sendAllMessage(obj.toJSONString());
                } else {
                    // 2.插入用户通告阅读标记表记录
                    String userId = notice.getUserIds();
                    if (StringUtils.isNoneBlank(userId)) {
                        String[] userIds = userId.substring(0, (userId.length() - 1)).split(",");
                        obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_USER);
                        obj.put(WebsocketConstant.MSG_ID, notice.getId());
                        obj.put(WebsocketConstant.MSG_TXT, notice.getTitle());
                        webSocket.sendMoreMessage(userIds, obj.toJSONString());
                    }
                }
            }
        } else {
            obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_TOPIC);
            obj.put(WebsocketConstant.MSG_TXT, "批量设置已读");
            webSocket.sendAllMessage(obj.toJSONString());
        }
        return CommonResult.success("");
    }


    /**
     * 用户树
     *
     * @return
     */
    @GetMapping("treeUser")
    public CommonResult treeUser() {
        List<TreeVo> selectTreeNodes = Lists.newArrayList();
        LoginUserVo loginUserVo = ShiroUtil.getUser();
        /**
         * 0：超级管理员，1：企业用户，2：监管用户
         */
        Integer userType = loginUserVo.getUserType();
        if (userType == 1) {
            selectTreeNodes = sysNoticeService.treeUser(loginUserVo.getEnterpriseId().toString());
        }
        if (userType == 2) {
            List<Long> enterpriseIds = loginUserVo.getEnterpriseIdList();
            if (enterpriseIds != null && enterpriseIds.size() > 0) {
                List<TreeVo> finalSelectTreeNodes = selectTreeNodes;
                enterpriseIds.forEach(enterpriseId -> {
                    finalSelectTreeNodes.addAll(sysNoticeService.treeUser(enterpriseId.toString()));
                });
            }
        }

        return CommonResult.success(selectTreeNodes);
    }
}
