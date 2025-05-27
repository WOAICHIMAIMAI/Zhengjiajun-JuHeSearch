package com.zheng.springbootinit.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.Date;

@Document(indexName = "product", createIndex = true)
@Data
public class ProductDocument {

    @Id
    private Long id;

    @MultiField(
            mainField = @Field(
                    type = FieldType.Text,
                    analyzer = "ik_max_word",    // 索引时使用细粒度分词
                    searchAnalyzer = "ik_smart"  // 搜索时使用智能分词
            ),
            otherFields = {
                    @InnerField(
                            suffix = "keyword",
                            type = FieldType.Keyword,
                            ignoreAbove = 256        // 超过256字符的keyword会被忽略
                    )
            }
    )
    private String name;

    @MultiField(
            mainField = @Field(
                    type = FieldType.Text,
                    analyzer = "ik_max_word",
                    searchAnalyzer = "ik_smart"
            ),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 256)
            }
    )
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    // 忽略映射
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date updateTime;

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
}
