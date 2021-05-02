package com.hdw.web.base.shiro.jwt.config;

import com.hdw.web.base.shiro.cache.ShiroRedisCacheManager;
import com.hdw.web.base.shiro.jwt.JwtRealm;
import com.hdw.web.base.shiro.jwt.filter.JwtFilter;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description shiro jwt配置
 * @Author JacksonTu
 * @Date 2020/4/5 10:45
 */
@Configuration
public class ShiroJwtConfig {

    @Resource(name = "shiroRedisTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 全局缓存时间，单位为秒
     */
    @Value("${hdw.jwt.expiration}")
    private int cacheLive;

    /**
     * 全局缓存名称前缀，默认为应用名
     */
    @Value("${spring.application.name}")
    private String cacheKeyPrefix;

    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * Filter Chain定义说明
     * <p>
     * 1、一个URL可以配置多个Filter，使用逗号分隔
     * 2、当设置多个过滤器时，全部验证通过，才视为通过
     * 3、部分过滤器可指定参数，如perms，roles
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        //TODO:配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/sys/captcha", "anon"); //登录验证码接口排除
        filterChainDefinitionMap.put("/sys/login", "anon"); //登录接口排除
        filterChainDefinitionMap.put("/sys/logout", "anon"); //登出接口排除
        filterChainDefinitionMap.put("/sys/encrypt", "anon");//加密
        filterChainDefinitionMap.put("/api/**", "anon");// API接口

        //TODO:开放的静态资源
        filterChainDefinitionMap.put("/favicon.ico", "anon");// 网站图标
        filterChainDefinitionMap.put("/bootstrap/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/font/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/plugins/**", "anon");
        filterChainDefinitionMap.put("/upload/**", "anon");
        filterChainDefinitionMap.put("/qr/**", "anon");

        filterChainDefinitionMap.put("/**/*.js", "anon");
        filterChainDefinitionMap.put("/**/*.css", "anon");
        filterChainDefinitionMap.put("/**/*.html", "anon");
        filterChainDefinitionMap.put("/**/*.svg", "anon");
        filterChainDefinitionMap.put("/**/*.pdf", "anon");
        filterChainDefinitionMap.put("/**/*.jpg", "anon");
        filterChainDefinitionMap.put("/**/*.png", "anon");
        filterChainDefinitionMap.put("/**/*.ico", "anon");

        //TODO:排除字体格式的后缀
        filterChainDefinitionMap.put("/**/*.ttf", "anon");
        filterChainDefinitionMap.put("/**/*.woff", "anon");
        filterChainDefinitionMap.put("/**/*.woff2", "anon");

        filterChainDefinitionMap.put("/druid/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger**/**", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v2/**", "anon");
        filterChainDefinitionMap.put("/doc.html", "anon");

        //TODO:性能监控
        filterChainDefinitionMap.put("/actuator/**", "anon");

        //TODO:测试示例
        filterChainDefinitionMap.put("/test/**", "anon"); //模板页面

        //TODO:websocket排除
        filterChainDefinitionMap.put("/ws/**", "anon");

        //TODO:添加自己的过滤器并且取名为jwt
        Map<String, Filter> filterMap = new HashMap<String, Filter>(1);
        filterMap.put("jwt", new JwtFilter());
        shiroFilterFactoryBean.setFilters(filterMap);
        //TODO:过滤链定义，从上向下顺序执行，一般将/**放在最为下边
        filterChainDefinitionMap.put("/**", "jwt");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 安全管理器配置
     *
     * @return
     */
    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setCacheManager(shiroRedisCacheManager());
        securityManager.setRealm(jwtRealm());
        securityManager.setSubjectDAO(subjectDAO());
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    @Bean
    public Realm jwtRealm() {
        JwtRealm jwtRealm = new JwtRealm();
        jwtRealm.setCacheManager(shiroRedisCacheManager());
        jwtRealm.setCachingEnabled(true);
        return jwtRealm;
    }

    /**
     * 缓存管理器
     *
     * @return
     */
    @Bean
    public CacheManager shiroRedisCacheManager() {
        ShiroRedisCacheManager redisCacheManager = new ShiroRedisCacheManager(cacheLive * 1000, cacheKeyPrefix + ":shiro-cache:", redisTemplate);
        return redisCacheManager;

    }

    /**
     * 会话管理器
     *
     * @return
     */
    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();

        sessionManager.setCacheManager(shiroRedisCacheManager());
        /**
         * 会话验证
         * Shiro提供了会话验证调度器，用于定期的验证会话是否已过期，如果过期将停止会话；
         * 出于性能考虑，一般情况下都是获取会话时来验证会话是否过期并停止会话的；
         * 但是如在web环境中，如果用户不主动退出是不知道会话是否过期的，因此需要定期的检测会话是否过期，
         * Shiro提供了会话验证调度器SessionValidationScheduler来做这件事情。
         */
        //TODO:单位为毫秒（1秒=1000毫秒）设置为7天
        sessionManager.setSessionValidationInterval(1000 * cacheLive);
        //TODO: 设置全局session超时时间
        sessionManager.setGlobalSessionTimeout(1000 * cacheLive);
        //TODO:删除过期session
        sessionManager.setDeleteInvalidSessions(true);
        //TODO: 开启/禁用绘画验证
        sessionManager.setSessionValidationSchedulerEnabled(false);

        SimpleCookie cookie = new SimpleCookie();
        //TODO:设置Cookie名字，默认为JSESSIONID；
        cookie.setName(cacheKeyPrefix + "-");
        //TODO:设置Cookie的域名，默认空，即当前访问的域名；
        cookie.setDomain("");
        //TODO:设置Cookie的路径，默认空，即存储在域名根下；
        cookie.setPath("");
        //TODO:设置Cookie的过期时间，单位为秒，默认-1表示关闭浏览器时过期Cookie；
        cookie.setMaxAge(cacheLive);
        //TODO:如果设置为true，则客户端不会暴露给客户端脚本代码，使用HttpOnly cookie有助于减少某些类型的跨站点脚本攻击
        cookie.setHttpOnly(true);
        //TODO:sessionManager创建会话Cookie的模板
        sessionManager.setSessionIdCookie(cookie);
        //TODO:是否启用/禁用Session Id Cookie，默认是启用的；如果禁用后将不会设置Session Id Cookie，即默认使用了Servlet容器的JSESSIONID，且通过URL重写（URL中的“;JSESSIONID=id”部分）保存Session Id。
        sessionManager.setSessionIdCookieEnabled(false);
        return sessionManager;
    }

    /**
     * 关闭Shiro自带的Session,详见文档
     * http://shiro.apache.org/session-management.html#SessionManagement
     *
     * @return
     */
    @Bean
    public DefaultSubjectDAO subjectDAO() {
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        return subjectDAO;
    }

    /**
     * 下面的代码是添加注解支持
     *
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }


}
