package com.hdw.web.base.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @Description 上传文件类型拦截器
 * @Author JacksonTu
 * @Date 2019/11/1 13:54
 */
@Slf4j
@Component
public class UploadInterceptor extends HandlerInterceptorAdapter {
    @Resource
    private InterceptorProperties interceptorProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        boolean flag = true;
        List<String> suffixList = interceptorProperties.getUpload().getAllowFileExtensions();

        if (request instanceof MultipartHttpServletRequest) {
            AtomicBoolean suffixFlag = new AtomicBoolean(true);
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> files = multipartRequest.getFileMap();
            files.entrySet().forEach((entry) -> {
                MultipartFile multipartFile = entry.getValue();
                String fileName = multipartFile.getOriginalFilename();
                System.out.println("fileName:" + fileName);
                suffixFlag.set(checkFile(fileName, suffixList));
            });
            flag = suffixFlag.get();
        }
        return flag;
    }

    /**
     * 判断是否为允许的上传文件类型,true表示允许
     */
    private boolean checkFile(String fileName, List<String> list) {
        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        list.stream().filter(s -> equals(suffix)).collect(Collectors.toList());
        return !list.isEmpty() && list.size() > 0;
    }
}
