package com.zheng.springbootinit.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * canal配置
 */
@Component
@Data
@ConfigurationProperties("canal")
public class CanalProperties {

    /**
     * 端口
     */
    private Integer port;
    /**
     * 描述
     */
    private String destination;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 批量大小
     */
    private Integer batchSize;

    /**
     * 过滤
     */
    private String filter;

    /**
     * 实体类包名前缀
     */
    private String basePackage;

    /**
     * 实体类后缀 
     */
    private String clazzSuffix;
}
