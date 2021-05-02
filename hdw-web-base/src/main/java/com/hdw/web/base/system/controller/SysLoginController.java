package com.hdw.web.base.system.controller;


import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.hdw.api.base.enterprise.entity.Enterprise;
import com.hdw.api.base.enterprise.service.IEnterpriseService;
import com.hdw.api.base.system.entity.SysUser;
import com.hdw.api.base.system.service.ISysLogService;
import com.hdw.api.base.system.service.ISysUserService;
import com.hdw.common.core.api.CommonResult;
import com.hdw.common.core.constant.CommonConstant;
import com.hdw.common.core.vo.LoginUserVo;
import com.hdw.common.starter.redisson.service.RedisService;
import com.hdw.web.base.shiro.ShiroUtil;
import com.hdw.web.base.shiro.jwt.JwtTokenUtil;
import com.hdw.web.base.system.dto.SysLoginDto;
import com.wf.captcha.SpecCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Description 登录退出接口
 * @Author JacksonTu
 * @Date 2018/6/11 17:07
 */
@Slf4j
@Api(value = "登录退出接口", tags = {" 登录退出接口"})
@RestController
public class SysLoginController {

    @DubboReference
    private ISysUserService userService;

    @DubboReference
    private IEnterpriseService enterpriseService;

    @DubboReference
    private ISysLogService sysLogService;

    @Resource
    private RedisService redisService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 全局缓存名称前缀，默认为应用名
     */
    @Value("${spring.application.name}")
    private String cacheKeyPrefix;

    /**
     * 获取验证码
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/sys/captcha")
    public CommonResult captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.fastUUID();
        // 存入redis并设置过期时间为30分钟
        redisService.set(key, verCode, 1800L);
        // 将key和base64返回给前端
        ConcurrentMap<String, Object> dataMap = new ConcurrentHashMap<>();
        dataMap.put("key", key);
        dataMap.put("image", specCaptcha.toBase64());
        return CommonResult.success(dataMap);
    }

    /**
     * 登录
     */
    @ApiOperation(value = "登录", notes = "登录")
    @PostMapping("/sys/login")
    public CommonResult login(@RequestBody SysLoginDto loginDto) {

        log.info("POST请求登录");
        String username = decrypt(loginDto.getUsername());
        String password = decrypt(loginDto.getPassword());
        String verCode = loginDto.getCaptcha();
        String verKey = loginDto.getCheckKey();

        if (StringUtils.isBlank(username)) {
            return CommonResult.failed("用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            return CommonResult.failed("密码不能为空");
        }
        if (StringUtils.isBlank(verCode)) {
            return CommonResult.failed("验证码不能为空");
        }

        //TODO: 获取redis中的验证码
        String redisCode = (String) redisService.get(verKey);
        //TODO: 判断验证码
        if (org.springframework.util.StringUtils.isEmpty(redisCode) || !redisCode.equals(verCode.trim().toLowerCase())) {
            return CommonResult.failed("验证码不正确");
        }

        SysUser sysUser = userService.selectByLoginName(username);

        if (ObjectUtils.isEmpty(sysUser)) {
            return CommonResult.failed("账号不存在");
        }
        if (!sysUser.getPassword().equals(ShiroUtil.md5(password, username + sysUser.getSalt()))) {
            return CommonResult.failed("密码不正确");
        }
        if (sysUser.getStatus() == 1) {
            return CommonResult.failed("账号被禁用");
        }

        //TODO:当企业不存在或者企业被禁用不允许登录
        if (sysUser.getUserType() == 1) {
            Enterprise sysEnterprise = enterpriseService.getById(sysUser.getEnterpriseId());
            if (null != sysEnterprise && sysEnterprise.getStatus() == 1) {
                return CommonResult.failed("企业被禁用，该账户不允许登录");
            } else if (null == sysEnterprise) {
                return CommonResult.failed("企业不存在，该账户不允许登录");
            }
        }

        //TODO: 生成token
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtils.copyProperties(sysUser, loginUserVo);
        String token = jwtTokenUtil.generateToken(loginUserVo);
        ConcurrentMap<String, Object> concurrentMap = new ConcurrentHashMap<>();
        concurrentMap.put("token", token);
        log.info(" 用户名:  " + loginUserVo.getName() + ",登录成功！ ");
        sysLogService.addLog(loginUserVo.getLoginName(), "用户名: " + loginUserVo.getName() + ",登录成功！", 1, null);
        return CommonResult.success(concurrentMap);
    }

    /**
     * 退出
     */
    @ApiOperation(value = "退出", notes = "退出")
    @PostMapping("/sys/logout")
    public CommonResult logout(HttpServletRequest request, HttpServletResponse response) {
        //用户退出逻辑
        String token = request.getHeader(CommonConstant.JWT_DEFAULT_TOKEN_NAME);
        if (StringUtils.isEmpty(token)) {
            return CommonResult.failed("退出登录失败!");
        }
        String username = jwtTokenUtil.getUserNameFromToken(token);
        LoginUserVo loginUserVo = userService.selectLoginUserVoByLoginName(username);
        if (!org.springframework.util.ObjectUtils.isEmpty(loginUserVo)) {
            //TODO:清空用户登录Shiro权限缓存
            sysLogService.addLog(loginUserVo.getLoginName(), "用户名: " + loginUserVo.getName() + ",退出成功！", 1, null);
            //清空用户登录Shiro权限缓存
            redisService.del(cacheKeyPrefix+":shiro-cache:" + token);
            redisService.del(cacheKeyPrefix+":shiro-cache:" + "com.hdw.shiro.jwt.JwtRealm_0");
            //TODO:调用Shiro的logout
            SecurityUtils.getSubject().logout();
            return CommonResult.success("退出登录成功!");
        } else {
            return CommonResult.failed("Token无效!");
        }
    }

    /**
     * 加密
     *
     * @param data
     * @return
     */
    @ApiOperation(value = "加密", notes = "加密")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "data", value = "待加密字符串", required = false, dataType = "String"),
    })
    @GetMapping("/sys/encrypt")
    public String encrypt(String data) {
        /** AES加解密 */
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, "1234567812345678".getBytes(), "1234567812345678".getBytes());
        // 解密
        String s = aes.encryptBase64(data);
        return s;
    }

    /**
     * 解密
     *
     * @param encrypt
     * @return
     */
    private String decrypt(String encrypt) {
        /** AES加解密 */
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, "1234567812345678".getBytes(), "1234567812345678".getBytes());
        // 解密
        String s = aes.decryptStr(encrypt).replace("\"", "");
        return s;
    }

}
