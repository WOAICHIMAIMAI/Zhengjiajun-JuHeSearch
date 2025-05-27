package com.zheng.springbootinit.model.dto.search;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String searchText;

    private long current;

    private long pageSize;

    private String type;
}
