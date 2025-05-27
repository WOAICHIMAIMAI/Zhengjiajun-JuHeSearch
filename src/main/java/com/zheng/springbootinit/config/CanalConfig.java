package com.zheng.springbootinit.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.zheng.springbootinit.properties.CanalProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

/**
 * Canal配置
 */
@Slf4j
//@Configuration
public class CanalConfig {

    @Bean(destroyMethod = "disconnect")
    public CanalConnector canalConnector(CanalProperties properties) {
        log.info("Canal配置初始化");
        return CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                properties.getPort()), properties.getDestination(), properties.getUsername(), properties.getPassword());
    }
}
