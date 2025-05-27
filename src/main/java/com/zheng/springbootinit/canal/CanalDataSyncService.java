package com.zheng.springbootinit.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;

import com.zheng.springbootinit.properties.CanalProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
//@Component
public class CanalDataSyncService implements DisposableBean {

    @Resource
    private CanalConnector canalConnector;
    @Resource
    private CanalProperties canalProperties;
    @Resource
    private ElasticsearchRestTemplate restTemplate;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        // 初始化线程池
        executorService = Executors.newSingleThreadExecutor();
        // 提交任务到线程池
        executorService.submit(this::process);
        log.info("Canal 开始监控数据改变");
    }

    private void process() {
        canalConnector.connect();
        canalConnector.subscribe(canalProperties.getFilter());
        while (true) {
            Message message = canalConnector.get(100);
            List<CanalEntry.Entry> entries = message.getEntries();
            // 处理数据
            handlerEntries(entries);
        }
    }

    @Override
    public void destroy() throws Exception {
        // 关闭CanalConnector连接
        canalConnector.disconnect();
        log.info("CanalConnector已断开连接");

        // 关闭线程池
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
            log.info("线程池已关闭");
        }
    }

    private void handlerEntries(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            log.info("================> binlog[{}:{}] , name[{},{}] , eventType : {}", entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(), entry.getHeader().getSchemaName(), entry.getHeader().getTableName(), eventType);
            String tableName = entry.getHeader().getTableName(); // 表面对应实体类名称
            // 这里利用反射拿到对应的实体类
            Class<?> clazz = null;
            Object syncObj = null;
            try {
                String clazzName = toPascalCase(tableName);
                clazz = Class.forName(canalProperties.getBasePackage() + "." + clazzName + canalProperties.getClazzSuffix());
                log.info("获取到的实体类：{}", clazz);
                syncObj = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("获取实体类失败，表名：{}", tableName, e);
                continue; // 跳过当前条目，继续处理下一个
            }

            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                handleRowData(rowData, eventType, clazz, syncObj);
            }
        }
    }
    /**
     * 处理行数据
     * @param rowData 行数据
     * @param eventType 事件类型
     * @param clazz 反射类
     * @param syncObj 同步实体对象
     */
    private void handleRowData(CanalEntry.RowData rowData, CanalEntry.EventType eventType, Class<?> clazz, Object syncObj) {
        if (eventType == CanalEntry.EventType.DELETE) {
            // 处理删除操作
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            // 只需要拿到对应的id就好
            handlerColumn(beforeColumnsList, clazz, syncObj);
            restTemplate.delete(syncObj);
        } else {
            // 处理插入/更新操作
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            handlerColumn(afterColumnsList, clazz, syncObj);
            restTemplate.save(syncObj);
        }
    }

    /**
     * 处理列数据
     * @param columns 列数据
     * @param clazz 反射类
     * @param syncObj 同步实体对象
     */
    private void handlerColumn(List<CanalEntry.Column> columns, Class<?> clazz, Object syncObj) {
        for (CanalEntry.Column column : columns) {
            log.info("{} : {} update={}", column.getName(), column.getValue(), column.getUpdated());
            try {
                Field field = clazz.getDeclaredField(toCamelCase(column.getName()));
                field.setAccessible(true);
                // 根据字段类型设置值
                setFieldValue(field, syncObj, column.getValue());
            } catch (Exception e) {
                log.error("设置实体类属性失败，字段名：{}", column.getName(), e);
            }
        }
    }
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        String[] parts = input.split("[_\\s]");

        // 第一个单词全小写
        if (parts.length > 0 && !parts[0].isEmpty()) {
            result.append(parts[0].toLowerCase());
        }

        // 后续单词首字母大写
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(parts[i].substring(0, 1).toUpperCase())
                        .append(parts[i].substring(1).toLowerCase());
            }
        }

        return result.toString();
    }
    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        // 按分隔符(下划线或空格)拆分
        String[] parts = input.split("[_\\s]");

        for (String part : parts) {
            if (!part.isEmpty()) {
                // 首字母大写，其余小写
                result.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    /**
     * 处理字段映射
     * @param field 字段
     * @param obj 实体对象
     * @param value 字段值
     * @throws IllegalAccessException
     */
    private static void setFieldValue(Field field, Object obj, String value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            field.set(obj, value);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            field.set(obj, Integer.parseInt(value));
        } else if (fieldType == long.class || fieldType == Long.class) {
            field.set(obj, Long.parseLong(value));
        } else if (fieldType == double.class || fieldType == Double.class) {
            field.set(obj, Double.parseDouble(value));
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            field.set(obj, Boolean.parseBoolean(value));
        } else if (fieldType == Date.class) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                field.set(obj, dateFormat.parse(value));
            } catch (ParseException e) {
                log.error("无法解析日期值: {}", value, e);
                throw new IllegalArgumentException("无法解析日期值: " + value, e);
            }
        } else if (fieldType == BigDecimal.class) {
            field.set(obj, new BigDecimal(value));
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }
    }

}
