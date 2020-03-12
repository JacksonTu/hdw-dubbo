package com.hdw.common.result;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;
import com.hdw.common.constants.ErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @Description 响应结果
 * @Author TuMingLong
 * @Date 2019/10/19 22:42
 */
@ApiModel(value = "响应结果")
public class CommonResult<T> implements Serializable {

    /**
     * 响应编码
     */
    @ApiModelProperty(value = "响应编码：0-请求处理成功")
    private int code = 0;

    /**
     * 提示消息
     */
    @ApiModelProperty(value = "提示消息")
    private String msg;

    /**
     * 响应数据
     */
    @ApiModelProperty(value = "响应数据")
    private T data;

    /**
     * 请求路径
     */
    @ApiModelProperty(value = "请求路径")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path;

    /**
     * http状态码
     */
    @ApiModelProperty(value = "http状态码")
    private int httpStatus;

    /**
     * 附加数据
     */
    @ApiModelProperty(value = "附加数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> extra;

    /**
     * 响应时间
     */
    @ApiModelProperty(value = "响应时间")
    private long timestamp = System.currentTimeMillis();

    public CommonResult() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public CommonResult code(int code) {
        this.code = code;
        return this;
    }

    public CommonResult msg(String message) {
        //this.msg = i18n(ErrorCode.getHttpCode(this.code).getMsg(), message);
        this.msg = message;
        return this;
    }

    public CommonResult data(T data) {
        this.data = data;
        return this;
    }

    public CommonResult path(String path) {
        this.path = path;
        return this;
    }

    public CommonResult httpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public CommonResult put(String key, Object value) {
        if (this.extra == null) {
            extra = Maps.newConcurrentMap();
        }
        this.extra.put(key, value);
        return this;
    }

    public static CommonResult ok() {
        return new CommonResult()
                .code(ErrorCode.SUCCESS.getCode())
                .msg(ErrorCode.SUCCESS.getMsg());

    }

    public static CommonResult fail() {
        return new CommonResult()
                .code(ErrorCode.FAIL.getCode())
                .msg(ErrorCode.FAIL.getMsg());

    }

    /**
     * 错误信息配置
     */
    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("error");

    /**
     * 提示信息国际化
     *
     * @param message
     * @param defaultMessage
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    private static String i18n(String message, String defaultMessage) {
        return resourceBundle.containsKey(message) ? resourceBundle.getString(message) : defaultMessage;
    }
}
