package com.zheng.springbootinit.model.dto.picture;

import com.zheng.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 创建请求
 *
*
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PictureQueryRequest extends PageRequest implements Serializable {

    private String searchText;

    private static final long serialVersionUID = 1L;
}